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
import org.wso2.msf4j.conf.Constants;
import org.wso2.msf4j.conf.SSLClientContext;
import org.wso2.msf4j.exception.TestExceptionMapper;
import org.wso2.msf4j.exception.TestExceptionMapper2;
import org.wso2.msf4j.service.SecondService;
import org.wso2.msf4j.service.TestMicroServiceWithDynamicPath;
import org.wso2.msf4j.service.TestMicroservice;
import org.wso2.transport.http.netty.config.TransportsConfiguration;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.ws.rs.HttpMethod;

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
                getContextClassLoader().getResource("netty-transports-1.yml").getPath());
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
            try (Reader in = new InputStreamReader(new FileInputStream(file), StandardCharsets.ISO_8859_1)) {
                Yaml yaml = new Yaml(new CustomClassLoaderConstructor
                        (TransportsConfiguration.class, TransportsConfiguration.class.getClassLoader()));
                yaml.setBeanAccess(BeanAccess.FIELD);
                transportsConfiguration = yaml.loadAs(in, TransportsConfiguration.class);
            } catch (IOException e) {
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
