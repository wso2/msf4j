/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.msf4j.osgi.test;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.kernel.CarbonServerInfo;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.osgi.test.service.SecondService;
import org.wso2.msf4j.osgi.test.service.TestService;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Dictionary;
import java.util.Hashtable;
import javax.inject.Inject;
import javax.ws.rs.HttpMethod;

import static org.testng.AssertJUnit.assertEquals;

/**
 * OSGi tests class to test MSF4J startup.
 *
 * @since 5.0.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class MSF4JOSGiTest {

    private static final String HEADER_VAL_CLOSE = "CLOSE";

    private static final String HOSTNAME = "localhost";
    private static final int PORT = 8080;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Inject
    BundleContext bundleContext;

    @Test
    public void testServerStarup() {
        Assert.assertNotNull(carbonServerInfo, "CarbonServerInfo Service is null");
    }

    @Test
    public void testDynamicServiceRegistration() throws IOException {
        Dictionary<String, String> properties = new Hashtable<>();
        properties.put("contextPath", "/BasePathV1");
        bundleContext.registerService(Microservice.class, new TestService(), properties);

        // Register same service with different base path
        properties.put("contextPath", "/BasePathV2");
        bundleContext.registerService(Microservice.class, new TestService(), properties);

        //Register some other service
        bundleContext.registerService(Microservice.class, new SecondService(), null);

        HttpURLConnection urlConn = request("/BasePathV1/hello", HttpMethod.GET);
        assertEquals(200, urlConn.getResponseCode());
        String content = getContent(urlConn);
        assertEquals("Hello MSF4J from TestService", content);
        urlConn.disconnect();

        urlConn = request("/BasePathV2/hello", HttpMethod.GET);
        assertEquals(200, urlConn.getResponseCode());
        content = getContent(urlConn);
        assertEquals("Hello MSF4J from TestService", content);
        urlConn.disconnect();
    }

    private HttpURLConnection request(String path, String method) throws IOException {
        return request(path, method, false);
    }

    private HttpURLConnection request(String path, String method, boolean keepAlive) throws IOException {
        URI baseURI = URI.create(String.format("http://%s:%d", HOSTNAME, PORT));
        URL url = baseURI.resolve(path).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
            urlConn.setDoOutput(true);
        }
        urlConn.setRequestMethod(method);
        if (!keepAlive) {
            urlConn.setRequestProperty(HttpHeaderNames.CONNECTION.toString(), HEADER_VAL_CLOSE);
        }

        return urlConn;
    }

    private String getContent(HttpURLConnection urlConn) throws IOException {
        return new String(IOUtils.toByteArray(urlConn.getInputStream()), Charsets.UTF_8);
    }
}
