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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.msf4j.conf.Constants;
import org.wso2.msf4j.conf.SSLClientContext;
import org.wso2.msf4j.exception.TestExceptionMapper;
import org.wso2.msf4j.exception.TestExceptionMapper2;
import org.wso2.msf4j.service.SecondService;
import org.wso2.msf4j.service.TestMicroServiceWithDynamicPath;
import org.wso2.msf4j.service.TestMicroservice;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.HttpMethod;

/**
 * Test the HttpsServer.
 */
public class HttpsServerTest extends HttpServerTest {

    private static SSLClientContext sslClientContext;

    private final TestMicroservice testMicroservice = new TestMicroservice();
    private final SecondService secondService = new SecondService();
    private MicroservicesRunner microservicesRunner;
    private MicroservicesRunner secondMicroservicesRunner;

    private static final int port = Constants.PORT + 4;


    @BeforeClass
    public void setup() throws Exception {
        baseURI = URI.create(String.format("https://%s:%d", Constants.HOSTNAME, port));
        System.setProperty(YAMLTransportConfigurationBuilder.NETTY_TRANSPORT_CONF,
                           Thread.currentThread().getContextClassLoader().getResource("netty-transports-1.yml")
                                 .getPath());
        microservicesRunner = new MicroservicesRunner();
        sslClientContext = new SSLClientContext();
        microservicesRunner
                .addExceptionMapper(new TestExceptionMapper(), new TestExceptionMapper2())
                .deploy(testMicroservice)
                .start();
        secondMicroservicesRunner = new MicroservicesRunner(port + 1);
        secondMicroservicesRunner.deploy(secondService).start();
        microservicesRunner.deploy("/DynamicPath", new TestMicroServiceWithDynamicPath());
    }

    @AfterClass
    public void teardown() throws Exception {
        microservicesRunner.stop();
        secondMicroservicesRunner.stop();
    }

    @Override
    protected HttpURLConnection request(String path, String method, boolean keepAlive) throws IOException {
        URL url = baseURI.resolve(path).toURL();
        HttpsURLConnection.setDefaultSSLSocketFactory(sslClientContext.getClientContext().getSocketFactory());
        HostnameVerifier allHostsValid = (hostname1, session) -> true;

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        HttpURLConnection urlConn = (HttpsURLConnection) url.openConnection();
        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
            urlConn.setDoOutput(true);
        }
        urlConn.setRequestMethod(method);
        if (!keepAlive) {
            urlConn.setRequestProperty(HEADER_KEY_CONNECTION, HEADER_VAL_CLOSE);
        }
        return urlConn;
    }

    @Override
    protected Socket createRawSocket(URL url) throws IOException {
        return sslClientContext.getClientContext().getSocketFactory().createSocket(url.getHost(), url.getPort());
    }

    static void setSslClientContext(SSLClientContext sslClientContext) {
        HttpsServerTest.sslClientContext = sslClientContext;
    }
}
