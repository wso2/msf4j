/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.msf4j;

import io.netty.handler.codec.http.HttpHeaderNames;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.config.ConfigProviderFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.msf4j.conf.Constants;
import org.wso2.msf4j.conf.SSLClientContext;
import org.wso2.msf4j.exception.TestExceptionMapper;
import org.wso2.msf4j.exception.TestExceptionMapper2;
import org.wso2.msf4j.service.SecondService;
import org.wso2.msf4j.service.TestMicroServiceWithDynamicPath;
import org.wso2.msf4j.service.TestMicroservice;
import org.wso2.transport.http.netty.contract.config.TransportsConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.HttpMethod;

import static org.wso2.msf4j.internal.MSF4JConstants.WSO2_TRANSPORT_HTTP_CONFIG_NAMESPACE;

/**
 * Test the HttpsServer.
 */
public class TransportConfigurationTest extends HttpServerTest {

    private static SSLClientContext sslClientContext;

    private final TestMicroservice testMicroservice = new TestMicroservice();
    private final SecondService secondService = new SecondService();
    private MicroservicesRunner microservicesRunner;
    private MicroservicesRunner secondMicroservicesRunner;

    private static final int port = Constants.PORT + 4;


    @BeforeClass
    public void setup() throws Exception {
        baseURI = URI.create(String.format("https://%s:%d", Constants.HOSTNAME, port));
        TransportsConfiguration transportsConfiguration = getConfiguration(Thread.currentThread().
                getContextClassLoader().getResource("netty-transports-1.yaml").getPath());
        microservicesRunner = new MicroservicesRunner(transportsConfiguration);
        sslClientContext = new SSLClientContext();
        microservicesRunner
                .addExceptionMapper(new TestExceptionMapper(), new TestExceptionMapper2())
                .deploy(testMicroservice)
                .start();
        secondMicroservicesRunner = new MicroservicesRunner(port + 1);
        secondMicroservicesRunner.deploy(secondService).start();
        microservicesRunner.deploy("/DynamicPath", new TestMicroServiceWithDynamicPath());
        microservicesRunner.deploy("/DynamicPath2", new TestMicroServiceWithDynamicPath());
    }

    /**
     * Get the {@code TransportsConfiguration} represented by a particular configuration file
     *
     * @param configFileLocation configuration file location
     * @return TransportsConfiguration represented by a particular configuration file
     */
    public TransportsConfiguration getConfiguration(String configFileLocation) {
        TransportsConfiguration transportsConfiguration;

        File file = new File(configFileLocation);
        if (file.exists()) {
            try {
            transportsConfiguration =
                    ConfigProviderFactory.getConfigProvider(Paths.get(configFileLocation), null)
                    .getConfigurationObject(WSO2_TRANSPORT_HTTP_CONFIG_NAMESPACE, TransportsConfiguration.class);
            } catch (ConfigurationException e) {
                throw new RuntimeException(
                        "Error while loading " + configFileLocation + " configuration file", e);
            }
        } else { // return a default config
            transportsConfiguration = new TransportsConfiguration();
        }

        return transportsConfiguration;
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
            urlConn.setRequestProperty(HttpHeaderNames.CONNECTION.toString(), HEADER_VAL_CLOSE);
        }
        return urlConn;
    }

    @Override
    protected Socket createRawSocket(URL url) throws IOException {
        return sslClientContext.getClientContext().getSocketFactory().createSocket(url.getHost(), url.getPort());
    }

    static void setSslClientContext(SSLClientContext sslClientContext) {
        TransportConfigurationTest.sslClientContext = sslClientContext;
    }
}
