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
package me.glindholm.plugin.http4e2.httpclient.core.client.misc;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreContext;
import me.glindholm.plugin.http4e2.httpclient.core.CoreObjects;
import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.client.misc.NativeHttpRequest.MultipartPart;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.AuthItem;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ProxyItem;
import me.glindholm.plugin.http4e2.httpclient.core.misc.CoreException;
import me.glindholm.plugin.http4e2.httpclient.core.util.BaseUtils;
import me.glindholm.plugin.http4e2.httpclient.core.util.SwtUtils;

public class HttpPerformer {

    private final NativeHttpRequest nativeRequest;
    private final ProxyItem proxy;
    private final AuthItem authItem;
    private final String reqPayloadFile;
    private final String respPayloadFile;

    private final StringBuilder requestBuff = new StringBuilder();
    private final StringBuilder respBuff = new StringBuilder();

    private volatile CompletableFuture<HttpResponse<InputStream>> pendingFuture;

    public HttpPerformer(final NativeHttpRequest nativeRequest, final ProxyItem proxy, final AuthItem authItem, final String[] payloadFiles) {
        this.nativeRequest = nativeRequest;
        this.proxy = proxy;
        this.authItem = authItem;
        this.reqPayloadFile = payloadFiles[0];
        this.respPayloadFile = payloadFiles[1];
    }

    public void execute() throws IOException {

        final HttpClient client = buildClient();

        // Build the body publisher and determine multipart boundary if needed
        String multipartBoundary = null;
        BodyPublisher bodyPublisher;

        if (nativeRequest.isMultipart()) {
            multipartBoundary = UUID.randomUUID().toString();
            bodyPublisher = buildMultipartPublisher(nativeRequest.getMultipartParts(), multipartBoundary);
        } else if (nativeRequest.getBody() != null && nativeRequest.getBody().length > 0) {
            bodyPublisher = BodyPublishers.ofByteArray(nativeRequest.getBody());
        } else {
            bodyPublisher = BodyPublishers.noBody();
        }

        // Build HttpRequest
        final HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(nativeRequest.getUrl()))
                .timeout(Duration.ofSeconds(30));

        // Add headers
        for (final Map.Entry<String, List<String>> entry : nativeRequest.getHeaders().entrySet()) {
            final String name = entry.getKey();
            for (final String val : entry.getValue()) {
                try {
                    builder.header(name, val);
                } catch (final IllegalArgumentException e) {
                    // skip restricted/invalid headers
                }
            }
        }

        // For multipart, set Content-Type with boundary
        if (nativeRequest.isMultipart()) {
            builder.header("Content-Type", "multipart/form-data; boundary=" + multipartBoundary);
        }

        // Handle Basic auth by adding header
        if (authItem != null) {
            applyBasicAuth(builder);
            applyBearerAuth(builder);
        }

        // Set method + body
        final String method = nativeRequest.getMethod();
        switch (method) {
        case "GET":
            builder.GET();
            break;
        case "HEAD":
            builder.method("HEAD", BodyPublishers.noBody());
            break;
        case "DELETE":
            if (nativeRequest.getBody() != null && nativeRequest.getBody().length > 0) {
                builder.method("DELETE", bodyPublisher);
            } else {
                builder.DELETE();
            }
            break;
        case "POST":
            builder.POST(bodyPublisher);
            break;
        case "PUT":
            builder.PUT(bodyPublisher);
            break;
        default:
            builder.method(method, bodyPublisher);
            break;
        }

        final HttpRequest httpRequest = builder.build();

        // Build request display text
        buildRequestText(httpRequest, nativeRequest.getBody());

        // Write request to payload file
        writePayloadFile(reqPayloadFile, requestBuff.toString().getBytes(StandardCharsets.UTF_8));

        // Execute
        try {
            pendingFuture = client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
            final HttpResponse<InputStream> response = pendingFuture.join();

            // Build response headers text
            respBuff.append("HTTP/1.1 ").append(response.statusCode()).append("\r\n");
            for (final Map.Entry<String, List<String>> entry : response.headers().map().entrySet()) {
                final String name = entry.getKey();
                if (name == null) {
                    continue; // status line pseudo-header
                }
                for (final String val : entry.getValue()) {
                    respBuff.append(name).append(CoreConstants._COL).append(CoreConstants._SPACE).append(val);
                    respBuff.append("\r\n");
                }
            }
            respBuff.append("\r\n");

            if (!"HEAD".equals(method)) {
                writeResponse(response.body(), respPayloadFile);
            }

        } catch (final java.util.concurrent.CancellationException e) {
            throw new IOException("Request cancelled", e);
        } catch (final java.util.concurrent.CompletionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof IOException ioe) {
                throw ioe;
            }
            throw new IOException(cause != null ? cause.getMessage() : e.getMessage(), cause);
        }
    }

    private void buildRequestText(final HttpRequest req, final byte[] body) {
        // Request line
        requestBuff.append(req.method()).append(" ");
        requestBuff.append(req.uri().getRawPath());
        final String query = req.uri().getRawQuery();
        if (query != null && !query.isEmpty()) {
            requestBuff.append("?").append(query);
        }
        requestBuff.append(" HTTP/1.1\r\n");

        // Headers
        req.headers().map().forEach((name, values) -> {
            for (final String val : values) {
                requestBuff.append(name).append(": ").append(val).append("\r\n");
            }
        });
        requestBuff.append("\r\n");

        // Body (text portion up to limit)
        if (body != null && body.length > 0) {
            final int maxSize = getSize();
            if (body.length > maxSize) {
                requestBuff.append(new String(body, 0, maxSize, StandardCharsets.UTF_8));
                final String cmd = SwtUtils.isMac() ? "  [CMD + O] to open\n\n" : "";
                requestBuff.append(
                        "...........................................................................\n\n#############################################\n\n" +
                        "  Payload too big. Raw packet saved at \n\n  file:///" + reqPayloadFile + "\n\n" + cmd +
                        "#############################################\n\n");
            } else {
                requestBuff.append(new String(body, StandardCharsets.UTF_8));
            }
        }
    }

    private void writeResponse(final InputStream bodyStream, final String filePath) {
        final int maxSize = getSize();
        final int headerSize = respBuff.length();
        final int maxBodySize = maxSize > headerSize ? maxSize - headerSize : 0;

        final File dir = new File(CoreContext.PRODUCT_USER_DIR);
        dir.mkdirs();

        try (final BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath))) {
            bos.write(respBuff.toString().getBytes(StandardCharsets.UTF_8));

            if (bodyStream == null) {
                return;
            }

            final byte[] buffer = new byte[1024];
            int bytesRead;
            int counter = 0;
            boolean limitReached = false;

            while ((bytesRead = bodyStream.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
                counter += bytesRead;

                if (!limitReached) {
                    if (counter > maxBodySize) {
                        final String cmd = SwtUtils.isMac() ? "  [CMD + O] to open\n\n" : "";
                        respBuff.append(
                                "...........................................................................\n\n#############################################\n\n" +
                                "  Payload too big. Raw packet saved at \n\n  file:///" + filePath + "\n\n" + cmd +
                                "  Increase the view size at (i)/Preferences drop menu \n\n#############################################\n\n");
                        limitReached = true;
                    } else {
                        respBuff.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
                    }
                }
            }

            bos.flush();
        } catch (final Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    private void writePayloadFile(final String filePath, final byte[] data) {
        final File dir = new File(CoreContext.PRODUCT_USER_DIR);
        dir.mkdirs();
        try (final FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(data);
        } catch (final IOException e) {
            ExceptionHandler.handle(e);
        }
    }

    private HttpClient buildClient() {
        final HttpClient.Builder clientBuilder = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(30));

        // SSL - trust all certificates
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] { new X509TrustManager() {
                @Override
                public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
                }

                @Override
                public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            } }, new SecureRandom());
            clientBuilder.sslContext(sslContext);
        } catch (final GeneralSecurityException e) {
            throw new CoreException(CoreException.SSL, e);
        }

        // Proxy
        if (proxy != null && proxy.isProxy()) {
            clientBuilder.proxy(ProxySelector.of(new InetSocketAddress(proxy.getHost(), proxy.getPort())));
        }

        // Digest auth via Authenticator
        if (authItem != null && authItem.isDigest()) {
            final String user = authItem.getUsername();
            final String pass = authItem.getPass();
            if (!BaseUtils.isEmpty(user) && !BaseUtils.isEmpty(pass)) {
                clientBuilder.authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, pass.toCharArray());
                    }
                });
            }
        }

        return clientBuilder.build();
    }

    private void applyBasicAuth(final HttpRequest.Builder builder) {
        if (!authItem.isBasic()) {
            return;
        }
        final String user = authItem.getUsername();
        final String pass = authItem.getPass();
        if (BaseUtils.isEmpty(user) || BaseUtils.isEmpty(pass)) {
            return;
        }
        final String encoded = Base64.getEncoder().encodeToString((user + ":" + pass).getBytes(StandardCharsets.UTF_8));
        builder.header("Authorization", "Basic " + encoded);
    }

    private void applyBearerAuth(final HttpRequest.Builder builder) {
        if (!authItem.isBearer()) {
            return;
        }
        final String token = authItem.getBearerToken();
        if (BaseUtils.isEmpty(token)) {
            return;
        }
        builder.header("Authorization", "Bearer " + token);
    }

    private BodyPublisher buildMultipartPublisher(final List<MultipartPart> parts, final String boundary) throws IOException {
        final List<byte[]> byteArrays = new ArrayList<>();
        final byte[] separator = ("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8);

        for (final MultipartPart part : parts) {
            byteArrays.add(separator);
            if (part.isFile) {
                final String filename = new File(part.filePath).getName();
                final String ct = part.contentType != null ? part.contentType : "application/octet-stream";
                byteArrays.add(("Content-Disposition: form-data; name=\"" + part.name + "\"; filename=\"" + filename + "\"\r\n").getBytes(StandardCharsets.UTF_8));
                byteArrays.add(("Content-Type: " + ct + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                byteArrays.add(Files.readAllBytes(Path.of(part.filePath)));
                byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
            } else {
                byteArrays.add(("Content-Disposition: form-data; name=\"" + part.name + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                byteArrays.add((part.value + "\r\n").getBytes(StandardCharsets.UTF_8));
            }
        }
        byteArrays.add(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        return BodyPublishers.ofByteArrays(byteArrays);
    }

    public void abort() {
        final CompletableFuture<HttpResponse<InputStream>> f = pendingFuture;
        if (f != null) {
            f.cancel(true);
        }
    }

    public String getResponse() {
        return respBuff.toString();
    }

    public String getRequest() {
        return requestBuff.toString();
    }

    private int getSize() {
        Integer maxSize = (Integer) CoreContext.getContext().getObject(CoreObjects.RESPONSE_VIEW_SIZE);
        if (maxSize == null) {
            maxSize = 100;
        }
        return maxSize * 1024;
    }
}