/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.msf4j.internal.router;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.msf4j.HttpResponder;
import org.wso2.msf4j.MicroservicesRunner;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;

/**
 * Test URLRewriter.
 */
public class URLRewriterTest {
    private static final Gson GSON = new Gson();

    private static final TestHandler testHandler = new TestHandler();

    private static String hostname = Constants.HOSTNAME;
    private static final int port = Constants.PORT + 3;
    private static URI baseURI;

    private static final MicroservicesRunner microservicesRunner = new MicroservicesRunner(port);

    @BeforeClass
    public static void setup() throws Exception {
        // MicroservicesRegistry is singleton
        microservicesRunner.getMsRegistry().setUrlRewriter(new TestURLRewriter());
        microservicesRunner
                .deploy(testHandler)
                .start();
        baseURI = URI.create(String.format("http://%s:%d", hostname, port));
    }

    @AfterClass
    public static void teardown() throws Exception {
        microservicesRunner.stop();
    }

    private static int doGet(String resource) throws Exception {
        return doGet(resource, ImmutableMap.<String, String>of());
    }

    private static int doGet(String resource, String key, String value, String... keyValues) throws Exception {
        Map<String, String> headerMap = Maps.newHashMap();
        headerMap.put(key, value);

        for (int i = 0; i < keyValues.length; i += 2) {
            headerMap.put(keyValues[i], keyValues[i + 1]);
        }
        return doGet(resource, headerMap);
    }

    private static int doGet(String resource, Map<String, String> headers) throws Exception {
        URL url = baseURI.resolve(resource).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        try {
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    urlConn.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }
            return urlConn.getResponseCode();
        } finally {
            urlConn.disconnect();
        }
    }

    @Test
    public void testUrlRewrite() throws Exception {
        int status = doGet("/rewrite/test/v1/resource");
        Assert.assertEquals(HttpResponseStatus.OK.code(), status);

        HttpURLConnection urlConn = request("/rewrite/test/v1/tweets/7648", HttpMethod.PUT);
        Assert.assertEquals(HttpResponseStatus.OK.code(), urlConn.getResponseCode());
        Map<String, String> stringMap = GSON.fromJson(getContent(urlConn),
                new TypeToken<Map<String, String>>() {
                }.getType());
        Assert.assertEquals(ImmutableMap.of("status", "Handled put in tweets end-point, id: 7648"), stringMap);

        urlConn.disconnect();
    }

    @Test
    public void testUrlRewriteNormalize() throws Exception {
        int status = doGet("/rewrite//test/v1//resource");
        Assert.assertEquals(HttpResponseStatus.OK.code(), status);
    }

    @Test
    public void testRegularCall() throws Exception {
        int status = doGet("/test/v1/resource");
        Assert.assertEquals(HttpResponseStatus.OK.code(), status);
    }

    @Test
    public void testUrlRewriteUnknownPath() throws Exception {
        int status = doGet("/rewrite/unknown/test/v1/resource");
        Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), status);
    }

    @Test
    public void testUrlRewriteRedirect() throws Exception {
        int status = doGet("/redirect/test/v1/resource");
        Assert.assertEquals(HttpResponseStatus.OK.code(), status);
    }

    private HttpURLConnection request(String path, HttpMethod method) throws IOException {
        URL url = baseURI.resolve(path).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        if (method == HttpMethod.POST || method == HttpMethod.PUT) {
            urlConn.setDoOutput(true);
        }
        urlConn.setRequestMethod(method.name());

        return urlConn;
    }

    private String getContent(HttpURLConnection urlConn) throws IOException {
        return new String(ByteStreams.toByteArray(urlConn.getInputStream()), Charsets.UTF_8);
    }

    private static class TestURLRewriter implements URLRewriter {
        @Override
        public boolean rewrite(HttpRequest request, HttpResponder responder) {
            if (request.getUri().startsWith("/rewrite/")) {
                request.setUri(request.getUri().replace("/rewrite/", "/"));
            }

            if (request.getUri().startsWith("/redirect/")) {
                responder.sendStatus(HttpResponseStatus.MOVED_PERMANENTLY,
                        ImmutableMultimap.of("Location", request.getUri().replace("/redirect/", "/rewrite/")));
                return false;
            }
            return true;
        }
    }
}
