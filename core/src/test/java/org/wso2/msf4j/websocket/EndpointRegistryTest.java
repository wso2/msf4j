/*
 *   Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.msf4j.websocket;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.TextCarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.msf4j.WebSocketEndpoint;
import org.wso2.msf4j.internal.router.PatternPathRouter;
import org.wso2.msf4j.internal.websocket.DispatchedEndpoint;
import org.wso2.msf4j.internal.websocket.EndpointsRegistryImpl;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

/**
 * Test for Endpoint Registry
 */
public class EndpointRegistryTest {

    private final String testText = "test";
    private WebSocketEndpoint testEndpoint = new TestEndpoint();
    private EndpointsRegistryImpl endpointsRegistry = EndpointsRegistryImpl.getInstance();
    private CarbonMessage textCarbonMessage = new TextCarbonMessage(testText);

    public EndpointRegistryTest() throws URISyntaxException {
    }

    @BeforeClass
    public void onRegister() throws URISyntaxException {
        URI uri = new URI("/test");
        textCarbonMessage.setProperty(Constants.TO, uri);
    }

    @Test
    public void registerEndpoint() throws InvocationTargetException, IllegalAccessException, URISyntaxException {
        endpointsRegistry.addEndpoint(testEndpoint);
        PatternPathRouter.RoutableDestination<DispatchedEndpoint> routableEndpoint =
                endpointsRegistry.getRoutableEndpoint(textCarbonMessage);
        DispatchedEndpoint dispatchedEndpoint = routableEndpoint.getDestination();
        List<Object> paralist = new LinkedList<>();
        paralist.add(testText);
        paralist.add(null);
        String returnValue = (String) dispatchedEndpoint.getOnStringMessageMethod().
                invoke(testEndpoint, paralist.toArray());
        Assert.assertEquals(returnValue, testText);
    }

    @Test
    public void removeEndpoint() throws Exception {
        endpointsRegistry.removeEndpoint(testEndpoint);
        Assert.assertTrue(endpointsRegistry.getRoutableEndpoint(textCarbonMessage) == null);
    }

}
