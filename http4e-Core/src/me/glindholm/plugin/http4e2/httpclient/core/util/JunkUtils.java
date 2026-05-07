/*
 *  Copyright 2017 Eclipse HttpClient (http4e) https://nextinterfaces.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.glindholm.plugin.http4e2.httpclient.core.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.List;

import org.eclipse.swt.custom.StyledText;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.glindholm.plugin.http4e2.crypt.HexUtils;
import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.view.assist.AssistConstants;
import me.glindholm.plugin.http4e2.httpclient.core.misc.CoreException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * A class with misc utils.
 *
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
public class JunkUtils {

    private static String XML_LINE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    private final static ResponseReader responseReader = response -> {
        // no-op
    };

    public static boolean isXwwwFormType(final ItemModel model) {
        final List<String> headers = model.getHeaderValuesIgnoreCase(AssistConstants.HEADER_CONTENT_TYPE);
        if (headers == null) {
            return false;
        }
        try {
            final String lastContType = headers.get(headers.size() - 1);
            return AssistConstants.CONTENT_TYPE_X_WWW_FORM.equalsIgnoreCase(lastContType);

        } catch (final Exception ignore) {
        }

        return false;
    }

    public static boolean isMultiartFormType(final ItemModel model) {
        final List<String> headers = model.getHeaderValuesIgnoreCase(AssistConstants.HEADER_CONTENT_TYPE);
        if (headers == null) {
            return false;
        }
        try {
            final String lastContType = headers.get(headers.size() - 1);
            return AssistConstants.CONTENT_TYPE_MULTIPART.equalsIgnoreCase(lastContType);

        } catch (final Exception ignore) {
        }

        return false;
    }

    public static void hexText(final StyledText styledText, final String text) {
        try {
            styledText.setText(HexUtils.toHex(text.getBytes(CoreConstants.UTF8)));
        } catch (final IOException e) {
            throw CoreException.getInstance(CoreException.UNSUPPORTED_ENCODING, e);
        }
    }

    public static String prettyText(String text) {

        final int inx = text.indexOf(CoreConstants.CRLF + CoreConstants.CRLF);
        if (inx > 5) {
            text = text.substring(inx + 4);
        }

        text = text.trim();

        if (text.startsWith("<?xml")) {
            return prettyXml(text, null);

        } else if (text.startsWith("<")) {
            return prettyXml(text, XML_LINE);

        } else if (text.startsWith("[") || text.startsWith("{")) {
            return jsonText(text, true);

        } else {
            return text;
        }
    }

    public static String jsonText(String txt, final boolean bypassXML) {

        try {
            final boolean isGWTok = txt.startsWith("//OK");
            final boolean isGWTerr = txt.startsWith("//EX");
            if (isGWTok || isGWTerr) {
                txt = txt.substring(4);
            }
            return toJSON(txt, isGWTok, isGWTerr);

        } catch (final JSONException e) {
//         ExceptionHandler.handle(e);
            if (bypassXML) {
                return txt;
            } else {
                final String line = txt.startsWith("<?xml") ? null : XML_LINE;
                return prettyXml(txt, line);
            }

        } catch (final Exception e) {
            ExceptionHandler.handle(e);
            if (bypassXML) {
                return txt;
            } else {
                final String line = txt.startsWith("<?xml") ? null : XML_LINE;
                return prettyXml(txt, line);
            }
        }

    }

    private static String toJSON(final String txt, final boolean isGWTok, final boolean isGWTerr) throws JSONException {

        String res = null;

        if (txt.startsWith("[")) {
            final JSONArray arr = new JSONArray(txt);
            res = arr.toString(4);
        } else {
            final JSONObject obj = new JSONObject(txt);
            res = obj.toString(4);
        }
        if (isGWTok) {
            return "//OK" + res;
        } else if (isGWTerr) {
            return "//EX" + res;
        } else {
            return res;
        }
    }

    public static String prettyXml(final String xml, final String firstLine) {
        try {
            final String input = firstLine != null ? firstLine + xml : xml;
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, firstLine == null ? "no" : "yes");
            final StringWriter writer = new StringWriter();
            transformer.transform(new StreamSource(new StringReader(input)), new StreamResult(writer));
            return writer.toString().trim();
        } catch (final Exception e) {
//         ExceptionHandler.handle(e);
            return xml;
        }
    }

    public static String getHdToken(final String url, final String md) {
        final HttpClient client = HttpClient.newHttpClient();
        try {
            final String formBody = "v=" + java.net.URLEncoder.encode(md, CoreConstants.UTF8);
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("content-type", "application/x-www-form-urlencoded")
                    .POST(BodyPublishers.ofString(formBody))
                    .build();
            final HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            responseReader.read(response);
            final java.util.Optional<String> header = response.headers().firstValue("hd-token");
            if (header.isPresent()) {
                return header.get();
            }
        } catch (final Exception ignore) {
            ExceptionHandler.handle(ignore);
        }
        return null;
    }

}