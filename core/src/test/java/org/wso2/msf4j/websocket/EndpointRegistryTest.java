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
import org.wso2.msf4j.websocket.endpoints.TestEndpoint;
import org.wso2.msf4j.websocket.endpoints.TestEndpointWithError;
import org.wso2.msf4j.websocket.exception.WebSocketEndpointAnnotationException;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

/**
 * Test Class for WebSocket Endpoint Registry
 */
public class EndpointRegistryTest {

    private static final Logger logger = LoggerFactory.getLogger(EndpointRegistryTest.class);

    private final String testText = "test";
    private WebSocketEndpoint testEndpoint = new TestEndpoint();
    private EndpointsRegistryImpl endpointsRegistry = EndpointsRegistryImpl.getInstance();
    private CarbonMessage textCarbonMessage = new TextCarbonMessage(testText);

    public EndpointRegistryTest() {
    }

    @BeforeClass
    public void onRegister() throws URISyntaxException {
        logger.info("\n----------------WebSocket Registry Test----------------");
        String uri = "/test";
        textCarbonMessage.setProperty(Constants.TO, uri);
    }

    @Test(description = "Testing the adding a correct endpoint to the registry.")
    public void addEndpoint() throws InvocationTargetException, IllegalAccessException, URISyntaxException,
                                          WebSocketEndpointAnnotationException {
        endpointsRegistry.addEndpoint(testEndpoint);
        PatternPathRouter.RoutableDestination<Object> routableEndpoint =
                endpointsRegistry.getRoutableEndpoint(textCarbonMessage);
        Object webSocketEndpoint = routableEndpoint.getDestination();
        List<Object> paralist = new LinkedList<>();
        paralist.add(testText);
        paralist.add(null);
        String returnValue = (String) new EndpointDispatcher().getOnStringMessageMethod(webSocketEndpoint).
                invoke(testEndpoint, paralist.toArray());
        Assert.assertEquals(returnValue, testText);
    }

    @Test(description = "Testing the removing of an endpoint from the registry.")
    public void removeEndpoint() throws WebSocketEndpointAnnotationException {
        try {
            endpointsRegistry.removeEndpoint(testEndpoint);
            PatternPathRouter.RoutableDestination<Object> endPoint = endpointsRegistry.
                    getRoutableEndpoint(textCarbonMessage);
            Assert.assertTrue(endPoint == null);
        } catch (WebSocketEndpointAnnotationException e) {
            logger.error("WebSocket Annotation Exception : " + e.getMessage());
            Assert.assertTrue(false);
        }
    }

    @Test(description = "Testing the exception when server endpoint is not defined on a endpoint.")
    public void testException() {
        try {
            WebSocketEndpoint testEndpoint = new TestEndpointWithError();
            endpointsRegistry.addEndpoint(testEndpoint);
            Assert.assertTrue(false);
        } catch (WebSocketEndpointAnnotationException e) {
            logger.error("Error occurred when adding endpoint : " + e.toString());
            Assert.assertTrue(true);
        }
    }
}
