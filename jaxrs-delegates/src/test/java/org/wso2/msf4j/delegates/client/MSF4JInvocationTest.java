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

package org.wso2.msf4j.delegates.client;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.service.ClientTestMicroService;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

/**
 * Class that tests MSF4JInvocation
 */
public class MSF4JInvocationTest extends Assert {

    private MicroservicesRunner microservicesRunner;
    private static final int PORT = 8080 + 21;

    @BeforeClass
    public void setUp() {
        microservicesRunner = new MicroservicesRunner(PORT);
        microservicesRunner
                .deploy(new ClientTestMicroService())
                .start();
    }

    @AfterClass
    public void teardown() throws Exception {
        microservicesRunner.stop();
    }

    @Test
    public void testSimpleGetRequest() {
        Response response = ClientBuilder.newClient().target("http://0.0.0.0:8101").path("test/hello").request().get();
        assertEquals(response.getStatus(), 200);
        assertEquals(response.getEntity(), "Hello");
    }

}
