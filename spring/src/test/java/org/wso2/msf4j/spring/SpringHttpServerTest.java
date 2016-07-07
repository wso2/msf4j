/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.msf4j.spring;

import org.springframework.context.ConfigurableApplicationContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.msf4j.HttpServerTest;
import org.wso2.msf4j.conf.Constants;

import java.net.URI;

/**
 * Spring tests.
 */
public class SpringHttpServerTest extends HttpServerTest {

    private static final int port = Constants.PORT;
    private ConfigurableApplicationContext configurableApplicationContext;

    @BeforeClass
    public void setup() throws Exception {
        baseURI = URI.create(String.format("http://%s:%d", Constants.HOSTNAME, port));
        configurableApplicationContext = MSF4JSpringApplication.run(SpringHttpServerTest.class);
    }

    @AfterClass
    public void teardown() throws Exception {
        configurableApplicationContext.close();
    }
}
