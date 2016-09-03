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

import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.msf4j.conf.Constants;
import org.wso2.msf4j.conf.SSLClientContext;
import org.wso2.msf4j.exception.TestExceptionMapper;
import org.wso2.msf4j.exception.TestExceptionMapper2;
import org.wso2.msf4j.service.TestMicroservice;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

/**
 * Test the HttpsServer with mutual authentication.
 */
public class MutualAuthServerTest extends HttpsServerTest {

    private final TestMicroservice testMicroservice = new TestMicroservice();
    private MicroservicesRunner microservicesRunner;

    private static String hostname = Constants.HOSTNAME;
    private static final int port = Constants.PORT + 5;
    private static File trustKeyStore;

    @BeforeClass
    public void setup() throws Exception {
        baseURI = URI.create(String.format("https://%s:%d", hostname, port));
        trustKeyStore = new File(tmpFolder, "MutualAuthServerTest.jks");
        trustKeyStore.createNewFile();
        /*IOUtils.copy(Thread.currentThread().getContextClassLoader().getResource("client.jks").openStream(),
                     Files.newOutputStream(trustKeyStore.toPath(), StandardOpenOption.APPEND));*/
        Files.copy(Thread.currentThread().getContextClassLoader().getResource("client.jks").openStream(), trustKeyStore.toPath(), StandardCopyOption.REPLACE_EXISTING);
        String trustKeyStorePassword = "password";
        setSslClientContext(new SSLClientContext(trustKeyStore, trustKeyStorePassword));

        System.setProperty(YAMLTransportConfigurationBuilder.NETTY_TRANSPORT_CONF,
                           Thread.currentThread().getContextClassLoader().getResource("netty-transports-2.yml")
                                 .getPath());
        microservicesRunner = new MicroservicesRunner();
        microservicesRunner.addExceptionMapper(new TestExceptionMapper(), new TestExceptionMapper2())
                           .deploy(testMicroservice).start();
    }

    @AfterClass
    public void teardown() throws Exception {
        microservicesRunner.stop();
        trustKeyStore.delete();
    }
}
