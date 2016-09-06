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
package org.wso2.msf4j.swagger;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.conf.Constants;
import org.wso2.msf4j.service.SecondService;
import org.wso2.msf4j.service.TestMicroservice;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.testng.Assert.assertEquals;

public class SwaggerTest {

    public static final String HEADER_KEY_CONNECTION = "CONNECTION";
    public static final String HEADER_VAL_CLOSE = "CLOSE";
    private final TestMicroservice testMicroservice = new TestMicroservice();
    private static final int port = Constants.PORT + 39;
    private MicroservicesRunner microservicesRunner;
    private MicroservicesRunner secondMicroservicesRunner;
    private final SecondService secondService = new SecondService();
    protected static URI baseURI;

    @BeforeClass
    public void setup() throws Exception {
        baseURI = URI.create(String.format("http://%s:%d", Constants.HOSTNAME, port));
        microservicesRunner = new MicroservicesRunner(port);
        microservicesRunner.deploy(testMicroservice).start();
        secondMicroservicesRunner = new MicroservicesRunner(port + 1);
        secondMicroservicesRunner.deploy(secondService).start();
    }

    @AfterClass
    public void teardown() throws Exception {
        microservicesRunner.stop();
        secondMicroservicesRunner.stop();
    }

    @Test
    public void testGlobalSwagger() throws Exception {
        HttpURLConnection urlConn = request("/swagger", HttpMethod.GET);
        assertEquals(Response.Status.OK.getStatusCode(), urlConn.getResponseCode());
        assertEquals(MediaType.APPLICATION_JSON, urlConn.getHeaderField(HttpHeaders.CONTENT_TYPE));
        urlConn.disconnect();
    }

    @Test
    public void testServiceSwagger() throws Exception {
        HttpURLConnection urlConn = request("/swagger?path=/test/v1", HttpMethod.GET);
        assertEquals(Response.Status.OK.getStatusCode(), urlConn.getResponseCode());
        assertEquals(MediaType.APPLICATION_JSON, urlConn.getHeaderField(HttpHeaders.CONTENT_TYPE));
        urlConn.disconnect();
    }

    @Test
    public void testNonExistentServiceSwagger() throws Exception {
        HttpURLConnection urlConn = request("/swagger?path=/zzaabdf", HttpMethod.GET);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), urlConn.getResponseCode());
        assertEquals(MediaType.APPLICATION_JSON, urlConn.getHeaderField(HttpHeaders.CONTENT_TYPE));
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
}
