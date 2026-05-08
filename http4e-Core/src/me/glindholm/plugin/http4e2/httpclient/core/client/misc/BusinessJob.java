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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.widgets.Composite;

import me.glindholm.plugin.http4e2.httpclient.core.CoreConstants;
import me.glindholm.plugin.http4e2.httpclient.core.CoreContext;
import me.glindholm.plugin.http4e2.httpclient.core.CoreMessages;
import me.glindholm.plugin.http4e2.httpclient.core.CoreObjects;
import me.glindholm.plugin.http4e2.httpclient.core.ExceptionHandler;
import me.glindholm.plugin.http4e2.httpclient.core.client.misc.NativeHttpRequest.MultipartPart;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.AuthItem;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ItemModel;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ModelEvent;
import me.glindholm.plugin.http4e2.httpclient.core.client.model.ProxyItem;
import me.glindholm.plugin.http4e2.httpclient.core.misc.CoreException;
import me.glindholm.plugin.http4e2.httpclient.core.util.ParseUtils;

/**
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
@SuppressWarnings({ "unchecked" })
public class BusinessJob {

    private ItemModel model;
    private NativeHttpRequest nativeRequest = null;
    private volatile HttpPerformer currentPerformer = null;
    private boolean buzDisposed = false;
    private static Map<String, String> contentTypes;

    public void execute(final ItemModel model, final Composite composite) {
        buzDisposed = false;
        this.model = model;

        final Map<String, String> parameterizedArgs = (Map<String, String>) CoreContext.getContext().getObject(CoreObjects.PARAMETERIZE_ARGS);

        try {
            nativeRequest = new NativeHttpRequest();
            nativeRequest.setMethod(model.getHttpMethod());
            nativeRequest.setUrl(model.getUrl());

            if (CoreConstants.HTTP_GET.equals(model.getHttpMethod())) {
                addHeaders(nativeRequest, parameterizedArgs);

            } else if (CoreConstants.HTTP_POST.equals(model.getHttpMethod())) {
                if (isMultipart(model)) {
                    nativeRequest.setMultipart(true);
                    addHeaders(nativeRequest, parameterizedArgs);
                    addMultipartParts(nativeRequest, parameterizedArgs);
                } else {
                    addHeaders(nativeRequest, parameterizedArgs);
                    addPostParams(nativeRequest, parameterizedArgs);
                    try {
                        nativeRequest.setBody(getBodyBytes(model));
                    } catch (final Exception e) {
                        throw new CoreException(CoreException.UNSUPPORTED_ENCODING, e);
                    }
                }

            } else if (CoreConstants.HTTP_OPTIONS.equals(model.getHttpMethod())) {
                addHeaders(nativeRequest, parameterizedArgs);

            } else {
                throw new CoreException(CoreException.HTTP_METHOD_NOT_IMPLEMENTED, "Method '" + model.getHttpMethod() + "' not implemented.");
            }

            doExecute(nativeRequest, composite);

        } catch (final Throwable e) {
            e.printStackTrace();
            abort(true);
        }
    }

    private boolean isMultipart(final ItemModel model) {
        final List<String> headers = model.getHeaders().get("Content-Type");
        if (headers == null) {
            return false;
        }
        final String hVal = headers.get(0);
        return "multipart/form-data".equalsIgnoreCase(hVal);
    }

    private byte[] getBodyBytes(final ItemModel model) throws Exception {
        final String body = model.getBody();
        if (body != null && body.startsWith(CoreConstants.FILE_PREFIX)) {
            final String file = body.substring(1);
            try (final FileInputStream fis = new FileInputStream(file)) {
                return fis.readAllBytes();
            } catch (final Exception e) {
                ExceptionHandler.handle(e);
                return body.getBytes(CoreConstants.UTF8);
            }
        } else {
            return body != null ? body.getBytes(CoreConstants.UTF8) : new byte[0];
        }
    }

    private void addPostParams(final NativeHttpRequest request, final Map<String, String> parameterizedMap) {
        if (model.getParameters().isEmpty() || (request.getBody() != null && request.getBody().length > 0)) {
            return;
        }
        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (final Entry<String, List<String>> me : model.getParameters().entrySet()) {
            final String key = me.getKey();
            final List<String> values = me.getValue();
            final StringBuilder valSb = new StringBuilder();
            int cnt = 0;
            for (final String val : values) {
                if (cnt != 0) {
                    valSb.append(",");
                }
                valSb.append(val);
                cnt++;
            }
            final String parameterizedVal = ParseUtils.getParametizedArg(valSb.toString(), parameterizedMap);
            if (!first) {
                sb.append("&");
            }
            try {
                sb.append(java.net.URLEncoder.encode(key, CoreConstants.UTF8))
                  .append("=")
                  .append(java.net.URLEncoder.encode(parameterizedVal, CoreConstants.UTF8));
            } catch (final Exception e) {
                sb.append(key).append("=").append(parameterizedVal);
            }
            first = false;
        }
        if (sb.length() > 0) {
            try {
                request.setBody(sb.toString().getBytes(CoreConstants.UTF8));
            } catch (final Exception e) {
                request.setBody(sb.toString().getBytes());
            }
        }
    }

    private void addMultipartParts(final NativeHttpRequest request, final Map<String, String> parameterizedMap) {
        for (final Entry<String, List<String>> me : model.getParameters().entrySet()) {
            final String key = me.getKey();
            final List<String> values = me.getValue();
            final StringBuilder sb = new StringBuilder();
            int cnt = 0;
            for (final String val : values) {
                if (cnt != 0) {
                    sb.append(",");
                }
                sb.append(val);
                cnt++;
            }
            final String parameterizedVal = ParseUtils.getParametizedArg(sb.toString(), parameterizedMap);

            if (parameterizedVal.startsWith("@")) {
                final String path = parameterizedVal.substring(1).trim().replace('\\', '/');
                final String contentType = getContentType(getFileExt(path));
                request.addMultipartPart(new MultipartPart(key, path, contentType));
            } else {
                request.addMultipartPart(new MultipartPart(key, parameterizedVal));
            }
        }
    }

    private void addHeaders(final NativeHttpRequest request, final Map<String, String> parameterizedMap) {
        for (final Entry<String, List<String>> me : model.getHeaders().entrySet()) {
            final String key = me.getKey();
            final List<String> values = me.getValue();
            final StringBuilder sb = new StringBuilder();
            int cnt = 0;
            for (final String val : values) {
                if (cnt != 0) {
                    sb.append(",");
                }
                sb.append(val);
                cnt++;
            }
            final String parameterizedVal = ParseUtils.getParametizedArg(sb.toString(), parameterizedMap);
            request.addHeader(key, parameterizedVal);
        }

        // Add User-Agent if not already present
        final boolean userAgentExist = request.getHeaders().keySet().stream()
                .anyMatch(k -> CoreConstants.HEADER_USER_AGENT.equalsIgnoreCase(k));
        if (!userAgentExist) {
            request.addHeader(CoreConstants.HEADER_USER_AGENT, CoreMessages.PLUGIN_NAME_SHORT + "/" + CoreMessages.VERSION);
        }
    }

    private void doExecute(final NativeHttpRequest request, final Composite composite) {
        final JobListener jobListener = new JobListener() {

            private boolean jDisposed;

            @Override
            public boolean isDisposed() {
                return jDisposed;
            }

            @Override
            public void update(final Object obj) {
                synchronized (BusinessJob.this) {
                    if (BusinessJob.this.isDisposed()) {
                        return;
                    }
                    final String[] values = (String[]) obj;
                    model.setRequest(values[0]);
                    model.fireExecute(new ModelEvent(ModelEvent.REQUEST_APPENDED, model));
                    model.setResponse(values[1]);
                    model.fireExecute(new ModelEvent(ModelEvent.REQUEST_STOPPED, model));
                }
                jDisposed = true;
            }

            @Override
            public Object execute() {
                final CoreContext ctx = CoreContext.getContext();
                final AuthItem authItem = model.getAuth();
                final ProxyItem proxyItem = (ProxyItem) ctx.getObject(CoreObjects.PROXY_ITEM);
                final String[] files = { generateFilePath("REQ.txt", request), generateFilePath("RESP.txt", request) };
                model.setPayloadFiles(files);
                model.fireExecute(new ModelEvent(ModelEvent.PAYLOAD_FILES, model));

                final HttpPerformer httpPerformer = new HttpPerformer(request, proxyItem, authItem, files);
                currentPerformer = httpPerformer;
                try {
                    httpPerformer.execute();
                    return new String[] { httpPerformer.getRequest(), httpPerformer.getResponse() };

                } catch (final ConnectException e) {
                    return new String[] { httpPerformer.getRequest(),
                            CoreMessages.CONNECTION_REFUSED + "[" + model.getHttpMethod() + " " + model.getUrl() + "]\n\n\t" + getProxyInfo(model) };

                } catch (final IOException | IllegalStateException e) {
                    return new String[] { httpPerformer.getRequest(),
                            CoreMessages.CONNECTION_IO_ERR + "[" + model.getHttpMethod() + " " + model.getUrl() + "]\n\n\t" + getProxyInfo(model) };

                } catch (final Throwable e) {
                    return new String[] { httpPerformer.getRequest(),
                            CoreMessages.CONNECTION_UNKNOWN + "[" + model.getHttpMethod() + " " + model.getUrl() + "]\n\n\t" + getProxyInfo(model) };
                } finally {
                    currentPerformer = null;
                }
            }
        };
        final JobRunner jobRunner = new JobRunner(composite, jobListener);
        jobRunner.run();
    }

    private String getProxyInfo(final ItemModel m) {
        final ProxyItem pi = m.getProxy();
        if (pi.isProxy()) {
            return "using proxy [" + pi.getHost() + ":" + pi.getPort() + "]";
        } else {
            return "";
        }
    }

    public synchronized void abort(final boolean withNotify) {
        if (withNotify) {
            model.fireExecute(new ModelEvent(ModelEvent.REQUEST_ABORTED, model));
        }
        buzDisposed = true;
        final HttpPerformer performer = currentPerformer;
        if (performer != null) {
            performer.abort();
        }
    }

    public boolean isDisposed() {
        return buzDisposed;
    }

    private String generateFilePath(final String ext, final NativeHttpRequest request) {
        String urlPath = ParseUtils.toTitle(request.getPath());
        urlPath = urlPath.replace("/", "-");
        if (urlPath.startsWith("-")) {
            urlPath = urlPath.substring(1);
        }
        final SimpleDateFormat formatter = new SimpleDateFormat("MMddHHmmss");
        return CoreContext.PRODUCT_USER_DIR + File.separator + urlPath + "-" + formatter.format(new Date()) + "." + ext;
    }

    private static String getFileExt(final String path) {
        if (path.contains(".")) {
            try {
                final String[] parts = path.split("\\.");
                return parts[parts.length - 1];
            } catch (final Exception e) {
                // ignore
            }
        }
        return null;
    }

    private static String getContentType(final String fileExtension) {
        if (contentTypes == null) {
            contentTypes = new HashMap<>();
            contentTypes.put("au", "audio/basic");
            contentTypes.put("avi", "video/x-msvideo");
            contentTypes.put("bin", "application/octet-stream");
            contentTypes.put("bmp", "image/bmp");
            contentTypes.put("css", "text/css");
            contentTypes.put("doc", "application/msword");
            contentTypes.put("exe", "application/octet-stream");
            contentTypes.put("gif", "image/gif");
            contentTypes.put("gz", "application/x-gzip");
            contentTypes.put("htm", "text/html");
            contentTypes.put("html", "text/html");
            contentTypes.put("jar", "application/java-archive");
            contentTypes.put("java", "text/plain");
            contentTypes.put("jpeg", "image/jpeg");
            contentTypes.put("jpg", "image/jpeg");
            contentTypes.put("js", "text/javascript");
            contentTypes.put("json", "application/json");
            contentTypes.put("mov", "video/quicktime");
            contentTypes.put("mp3", "audio/x-mpeg");
            contentTypes.put("mp4", "video/mp4");
            contentTypes.put("mpeg", "video/mpeg");
            contentTypes.put("mpg", "video/mpeg");
            contentTypes.put("ogg", "audio/ogg");
            contentTypes.put("pdf", "application/pdf");
            contentTypes.put("png", "image/png");
            contentTypes.put("ppt", "application/vnd.ms-powerpoint");
            contentTypes.put("rtf", "application/rtf");
            contentTypes.put("sh", "application/x-sh");
            contentTypes.put("tar", "application/x-tar");
            contentTypes.put("tif", "image/tiff");
            contentTypes.put("tiff", "image/tiff");
            contentTypes.put("txt", "text/plain");
            contentTypes.put("xml", "application/xml");
            contentTypes.put("xls", "application/vnd.ms-excel");
            contentTypes.put("zip", "application/zip");
            contentTypes.put("wmv", "video/x-ms-wmv");
        }
        if (fileExtension != null) {
            return contentTypes.get(fileExtension.toLowerCase().trim());
        }
        return null;
    }
}
