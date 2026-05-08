
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Simple model holding all data needed to execute an HTTP request using Java's native HttpClient.
 */
public class NativeHttpRequest {

    public static class MultipartPart {
        public final String name;
        public final boolean isFile;
        public final String filePath;
        public final String contentType;
        public final String value;

        public MultipartPart(final String name, final String value) {
            this.name = name;
            this.value = value;
            isFile = false;
            filePath = null;
            contentType = null;
        }

        public MultipartPart(final String name, final String filePath, final String contentType) {
            this.name = name;
            this.filePath = filePath;
            this.contentType = contentType;
            isFile = true;
            value = null;
        }
    }

    private String method;
    private String url;
    private final Map<String, List<String>> headers = new LinkedHashMap<>();
    private byte[] body;
    private boolean multipart;
    private final List<MultipartPart> multipartParts = new ArrayList<>();

    public String getMethod() {
        return method;
    }

    public void setMethod(final String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void addHeader(final String name, final String value) {
        headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
    }

    public void setHeader(final String name, final String value) {
        final List<String> list = new ArrayList<>();
        list.add(value);
        headers.put(name, list);
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(final byte[] body) {
        this.body = body;
    }

    public boolean isMultipart() {
        return multipart;
    }

    public void setMultipart(final boolean multipart) {
        this.multipart = multipart;
    }

    public List<MultipartPart> getMultipartParts() {
        return multipartParts;
    }

    public void addMultipartPart(final MultipartPart part) {
        multipartParts.add(part);
    }

    public String getPath() {
        try {
            final java.net.URI uri = new java.net.URI(url);
            final String path = uri.getPath();
            return path == null || path.isEmpty() ? "/" : path;
        } catch (final Exception e) {
            return "/";
        }
    }
}
