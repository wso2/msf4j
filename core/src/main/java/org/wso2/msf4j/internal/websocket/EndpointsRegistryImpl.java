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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.websocket.WebSocketCarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.msf4j.WebSocketEndpoint;
import org.wso2.msf4j.WebSocketEndpointsRegistry;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation for {@link WebSocketEndpointsRegistry}
 * Endpoints will be registered in a {@link java.util.HashMap} as a pair of {@link URI} & {@link DispatchedEndpoint}
 */
public class EndpointsRegistryImpl implements WebSocketEndpointsRegistry {

    private final Logger log = LoggerFactory.getLogger(EndpointsRegistryImpl.class);
    private static final EndpointsRegistryImpl webSocketEndpointsRegistry = new EndpointsRegistryImpl();

    /*

     */
    private Map<URI, DispatchedEndpoint> registeredEndpoints = new ConcurrentHashMap<>();

    public EndpointsRegistryImpl() {
    }

    /**
     * @return the {@link EndpointsRegistryImpl} instance
     */
    public static EndpointsRegistryImpl getInstance() {
        return webSocketEndpointsRegistry;
    }

    public void addEndpoint(WebSocketEndpoint... webSocketEndpoints) {
        Arrays.stream(webSocketEndpoints).forEach(
                webSocketEndpoint -> {
                    try {
                        DispatchedEndpoint dispatchedEndpoint = dispatchEndpoint(webSocketEndpoint);
                        registeredEndpoints.put(dispatchedEndpoint.getUri(), dispatchedEndpoint);
                    } catch (Exception e) {
                        log.error(e.toString());
                    }
                }
        );
    }

    public void removeEndpoint(WebSocketEndpoint webSocketEndpoint) throws Exception {
        DispatchedEndpoint dispatchedEndpoint = dispatchEndpoint(webSocketEndpoint);
        registeredEndpoints.remove(dispatchedEndpoint.getUri());
    }

    public DispatchedEndpoint dispatchEndpoint(WebSocketEndpoint webSocketEndpoint) throws Exception {
        EndpointDispatcher dispatcher = new EndpointDispatcher(webSocketEndpoint);
        return dispatcher.getDispatchedEndpoint();
    }

    public DispatchedEndpoint getDispatchedEndpoint(WebSocketCarbonMessage webSocketCarbonMessage) {
        URI uri = (URI) webSocketCarbonMessage.getProperty(Constants.TO);
        return registeredEndpoints.get(uri);
    }

    @Override
    public Set<Object> getAllEndpoints() {
        //TODO : Implementation
        return null;
    }
}
