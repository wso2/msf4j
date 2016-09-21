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

package org.wso2.msf4j;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.msf4j.conf.Constants;
import org.wso2.msf4j.service.ExtendedTestMicroservice;
import org.wso2.msf4j.service.TestMicroServiceWithDynamicPath;
import org.wso2.msf4j.service.TestMicroservice;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import javax.ws.rs.HttpMethod;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Test for extended (inherited) microservices.
 */
public class ExtendedServiceTest {

    public static final String HEADER_KEY_CONNECTION = "CONNECTION";
    public static final String HEADER_VAL_CLOSE = "CLOSE";
    protected static final Type STRING_MAP_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();
    protected static final Gson GSON = new Gson();

    private final TestMicroservice testMicroservice = new ExtendedTestMicroservice();

    private static final int port = Constants.PORT + 39;
    protected static URI baseURI;

    private MicroservicesRunner microservicesRunner;

    @BeforeClass
    public void setup() throws Exception {
        baseURI = URI.create(String.format("http://%s:%d", Constants.HOSTNAME, port));
        microservicesRunner = new MicroservicesRunner(port);
        microservicesRunner
                .deploy(testMicroservice)
                .start();
        microservicesRunner.deploy("/DynamicPath", new TestMicroServiceWithDynamicPath());
    }

    @AfterClass
    public void teardown() throws Exception {
        microservicesRunner.stop();
    }

    /**
     * Tests that an overridden method gets invoked.
     *
     * @throws IOException
     */
    @Test
    public void testValidEndPoints() throws IOException {
        HttpURLConnection urlConn = request("/ext-test/v1/resource?num=10", HttpMethod.GET);
        assertEquals(200, urlConn.getResponseCode());
        String content = getContent(urlConn);

        Map<String, String> map = GSON.fromJson(content, STRING_MAP_TYPE);
        assertEquals(1, map.size());
        assertEquals("Handled extended get in resource end-point", map.get("status"));
        urlConn.disconnect();
    }

    @Test
    public void testPutWithData() throws IOException {
        HttpURLConnection urlConn = request("/ext-test/v1/facebook/1/message", HttpMethod.PUT);
        writeContent(urlConn, "Hello, World");
        assertEquals(200, urlConn.getResponseCode());

        String content = getContent(urlConn);

        Map<String, String> map = GSON.fromJson(content, STRING_MAP_TYPE);
        assertEquals(1, map.size());
        assertEquals("Handled put in tweets end-point, id: 1. Content: Hello, World", map.get("result"));
        urlConn.disconnect();
    }

    private HttpURLConnection request(String path, String method) throws IOException {
        return request(path, method, false);
    }

    private HttpURLConnection request(String path, String method, boolean keepAlive) throws IOException {
        URL url = baseURI.resolve(path).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
            urlConn.setDoOutput(true);
        }
        urlConn.setRequestMethod(method);
        if (!keepAlive) {
            urlConn.setRequestProperty(HEADER_KEY_CONNECTION, HEADER_VAL_CLOSE);
        }

        return urlConn;
    }

    private String getContent(HttpURLConnection urlConn) throws IOException {
        return new String(IOUtils.toByteArray(urlConn.getInputStream()), Charsets.UTF_8);
    }

    protected void writeContent(HttpURLConnection urlConn, String content) throws IOException {
        urlConn.getOutputStream().write(content.getBytes(Charsets.UTF_8));
    }
}
