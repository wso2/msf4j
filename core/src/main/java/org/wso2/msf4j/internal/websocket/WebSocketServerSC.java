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

package org.wso2.msf4j.internal.websocket;


import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.msf4j.internal.DataHolder;
import org.wso2.msf4j.websocket.WebSocketEndpoint;
import org.wso2.msf4j.websocket.exception.WebSocketEndpointAnnotationException;

/**
 * OSGi Service component for WebSocket server. This will identify the endpoints which are trying to identify
 * and register them as WebSocket Server Endpoints
 *
 * @since 1.0.0
 */

@Component(
        name = "org.wso2.msf4j.internal.websocket.WebSocketServerSC",
        immediate = true,
        property = {
                "componentName=wso2-websocket-server"
        }
)
public class WebSocketServerSC implements RequiredCapabilityListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServerSC.class);
    private EndpointsRegistryImpl endpointsRegistry = EndpointsRegistryImpl.getInstance();

    @Activate
    protected void start(final BundleContext bundleContext) {
        LOGGER.debug("Endpoint Activated.");
    }

    @Reference(
        name = "websocketEndpoint",
        service = WebSocketEndpoint.class,
        cardinality = ReferenceCardinality.MULTIPLE,
        policy = ReferencePolicy.DYNAMIC,
        unbind = "removeEndpoint"
    )
    protected void addEndpoint(WebSocketEndpoint endpoint) throws WebSocketEndpointAnnotationException {
        endpointsRegistry.addEndpoint(endpoint);
    }

    protected void removeEndpoint(WebSocketEndpoint endpoint) throws Exception {
        endpointsRegistry.removeEndpoint(endpoint);
    }

    /**
     * Receives a notification when all the required services are available in the OSGi service registry.
     */
    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        DataHolder.getInstance().getBundleContext().registerService(WebSocketServerSC.class, this, null);
        LOGGER.info("All required capabilities are available");
    }
}
