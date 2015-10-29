/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.mss.internal.router;

import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.wso2.carbon.mss.MicroservicesRunner;
import org.wso2.carbon.transport.http.netty.internal.config.TransportConfigurationBuilder;

import java.io.File;
import java.net.URI;

/**
 * Test the HttpsServer with mutual authentication.
 */
public class MutualAuthServerTest extends HttpsServerTest {

    private static SSLClientContext sslClientContext;

    private static final TestHandler testHandler = new TestHandler();
    private static MicroservicesRunner microservicesRunner;

    private static String hostname = Constants.HOSTNAME;
    private static final int port = Constants.PORT + 5;


    @BeforeClass
    public static void setup() throws Exception {
        File trustKeyStore = tmpFolder.newFile();
        ByteStreams.copy(Resources.newInputStreamSupplier(Resources.getResource("client.jks")),
                Files.newOutputStreamSupplier(trustKeyStore));
        String trustKeyStorePassword = "password";
        setSslClientContext(new SSLClientContext(trustKeyStore, trustKeyStorePassword));

        System.setProperty(TransportConfigurationBuilder.NETTY_TRANSPORT_CONF,
                Resources.getResource("netty-transports-1.xml").getPath());
        microservicesRunner = new MicroservicesRunner();
        sslClientContext = new SSLClientContext();
        microservicesRunner
                .deploy(testHandler)
                .start();
        baseURI = URI.create(String.format("https://%s:%d", hostname, port));
    }

    @AfterClass
    public static void teardown() throws Exception {
        microservicesRunner.stop();
    }
}
