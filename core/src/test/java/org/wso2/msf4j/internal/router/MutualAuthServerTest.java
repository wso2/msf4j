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

import com.google.common.io.ByteStreams;
import com.google.common.io.FileWriteMode;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.msf4j.MicroservicesRunner;

import java.io.File;
import java.net.URI;

/**
 * Test the HttpsServer with mutual authentication.
 */
public class MutualAuthServerTest extends HttpsServerTest {

    private static final TestMicroservice TEST_MICROSERVICE = new TestMicroservice();
    private static MicroservicesRunner microservicesRunner;

    private static String hostname = Constants.HOSTNAME;
    private static final int port = Constants.PORT + 5;
    private static File trustKeyStore;


    @BeforeClass
    public static void setup() throws Exception {
        baseURI = URI.create(String.format("https://%s:%d", hostname, port));
        trustKeyStore = new File(tmpFolder, "MutualAuthServerTest.jks");
        trustKeyStore.createNewFile();
        ByteStreams.copy(
                Resources.asByteSource(Resources.getResource("client.jks")).openStream(),
                Files.asByteSink(trustKeyStore, FileWriteMode.APPEND).openStream());
        String trustKeyStorePassword = "password";
        setSslClientContext(new SSLClientContext(trustKeyStore, trustKeyStorePassword));

        System.setProperty(YAMLTransportConfigurationBuilder.NETTY_TRANSPORT_CONF,
                Resources.getResource("netty-transports-2.yml").getPath());
        microservicesRunner = new MicroservicesRunner();
        microservicesRunner
                .addExceptionMapper(new TestExceptionMapper(), new TestExceptionMapper2())
                .deploy(TEST_MICROSERVICE)
                .start();
    }

    @AfterClass
    public static void teardown() throws Exception {
        microservicesRunner.stop();
        trustKeyStore.delete();
    }
}
