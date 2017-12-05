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
 */

package org.wso2.msf4j.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.msf4j.internal.router.PatternPathRouter;
import org.wso2.msf4j.internal.websocket.EndpointDispatcher;
import org.wso2.msf4j.internal.websocket.EndpointsRegistryImpl;
import org.wso2.msf4j.websocket.endpoint.TestEndpoint;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Test Class for WebSocket Endpoint Registry
 */
public class EndpointRegistryTest {

    private static final Logger log = LoggerFactory.getLogger(EndpointRegistryTest.class);

    private final String testText = "test";
    private TestEndpoint testEndpoint = new TestEndpoint();
    private EndpointsRegistryImpl endpointsRegistry = EndpointsRegistryImpl.getInstance();
    private CarbonMessage textCarbonMessage = new TextCarbonMessage(testText);
    private final String uri = "/test";

    public EndpointRegistryTest() {
    }

    @BeforeClass
    public void onRegister() throws URISyntaxException {
        log.info(System.lineSeparator() +
                         "--------------------------------WebSocket Registry Test--------------------------------");
        textCarbonMessage.setProperty(Constants.TO, uri);
    }

    @Test(description = "Testing the adding a correct endpoint to the registry.")
    public void addEndpoint() throws InvocationTargetException, IllegalAccessException, URISyntaxException {
        log.info("Testing the adding a correct endpoint to the registry.");
        endpointsRegistry.addEndpoint(testEndpoint);
        PatternPathRouter.RoutableDestination<Object> routableEndpoint =
                endpointsRegistry.getRoutableEndpoint(uri);
        Object webSocketEndpoint = routableEndpoint.getDestination();
        List<Object> paralist = new LinkedList<>();
        paralist.add(testText);
        paralist.add(null);
        Optional<Method> methodOptional = new EndpointDispatcher().getOnStringMessageMethod(webSocketEndpoint);
        if (methodOptional.isPresent()) {
            String returnValue = (String) methodOptional.get().invoke(webSocketEndpoint, paralist.toArray());
            Assert.assertEquals(returnValue, testText);
        } else {
            Assert.assertTrue(false);
        }
    }

    @Test(description = "Testing the removing of an endpoint from the registry.")
    public void removeEndpoint() {
        log.info("Testing the removing of an endpoint from the registry.");
        endpointsRegistry.removeEndpoint(testEndpoint);
        PatternPathRouter.RoutableDestination<Object> endPoint = endpointsRegistry.
                getRoutableEndpoint(uri);
        Assert.assertTrue(endPoint == null);

    }
}
