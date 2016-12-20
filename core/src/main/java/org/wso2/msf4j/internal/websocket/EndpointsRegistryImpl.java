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
import org.wso2.msf4j.WebSocketEndpoint;
import org.wso2.msf4j.WebSocketEndpointsRegistry;

import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation for {@link WebSocketEndpointsRegistry}
 * This is a singleton class
 */
public class EndpointsRegistryImpl implements WebSocketEndpointsRegistry {

    private final Logger logger = LoggerFactory.getLogger(EndpointsRegistryImpl.class);
    private static final EndpointsRegistryImpl webSocketEndpointsRegistry =
            new EndpointsRegistryImpl();
    private Map<URI, WebSocketEndpoint> regiteredEndpoints = new ConcurrentHashMap<>();

    //Makes the class singleton
    private EndpointsRegistryImpl() {
    }

    /**
     * @return the {@link EndpointsRegistryImpl} instance
     */
    protected EndpointsRegistryImpl getWebSocketEndpointsRegistryInstandce() {
        return webSocketEndpointsRegistry;
    }

    public void addEndpoint(WebSocketEndpoint webSocketEndpoint){
        //TODO : Implementation
    }

    @Override
    public Set<Object> getAllEndpoints() {
        //TODO : Implementation
        return null;
    }
}
