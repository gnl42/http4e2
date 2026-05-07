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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Utility class for executing HTTP calls using Java's native HttpClient.
 *
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
public class HttpUtils {

    public static void execute(final HttpClient client, final HttpRequest request, final ResponseReader reader) throws Exception {
        final HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (reader != null) {
            reader.read(response);
        }
    }

    public static void execute(final HttpRequest request, final ResponseReader reader) throws Exception {
        final HttpClient client = HttpClient.newHttpClient();
        execute(client, request, reader);
    }

    public static void dumpResponse(final HttpResponse<InputStream> response, final PrintStream out) {
        try {
            out.println("------------");
            out.println("HTTP/1.1 " + response.statusCode());

            response.headers().map().forEach((name, values) -> {
                if (name != null) {
                    values.forEach(val -> out.println(name + ": " + val));
                }
            });

            final InputStreamReader inR = new InputStreamReader(response.body());
            final BufferedReader buf = new BufferedReader(inR);
            String line;
            while ((line = buf.readLine()) != null) {
                out.println(line);
            }
            out.println("------------");

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void dumpResponse(final HttpResponse<InputStream> response, final StringBuilder httpResponsePacket, final StringBuilder respBody) {
        try {
            httpResponsePacket.append('\n');
            httpResponsePacket.append("HTTP/1.1 ").append(response.statusCode());
            httpResponsePacket.append('\n');

            response.headers().map().forEach((name, values) -> {
                if (name != null) {
                    values.forEach(val -> {
                        httpResponsePacket.append(name).append(": ").append(val);
                        httpResponsePacket.append('\n');
                    });
                }
            });

            final InputStreamReader inR = new InputStreamReader(response.body());
            final BufferedReader buf = new BufferedReader(inR);
            String line;
            while ((line = buf.readLine()) != null) {
                httpResponsePacket.append(line).append('\n');
                respBody.append(line).append('\n');
            }
            httpResponsePacket.append('\n');

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}