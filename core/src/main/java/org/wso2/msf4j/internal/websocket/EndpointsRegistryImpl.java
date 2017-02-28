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
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.msf4j.internal.router.PatternPathRouter;
import org.wso2.msf4j.websocket.WebSocketEndpoint;
import org.wso2.msf4j.websocket.WebSocketEndpointsRegistry;
import org.wso2.msf4j.websocket.exception.WebSocketEndpointAnnotationException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation for {@link WebSocketEndpointsRegistry}
 * Endpoints are Dispatched and Stored in a {@link PatternPathRouter}. So when new request comes it will be routed to
 * best matching URI.
 */
public class EndpointsRegistryImpl implements WebSocketEndpointsRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointsRegistryImpl.class);
    private static final EndpointsRegistryImpl webSocketEndpointsRegistry = new EndpointsRegistryImpl();

    // Map <uri, WebSocketEndpoint>
    private final Map<String, DispatchedEndpoint> webSocketEndpointMap = new ConcurrentHashMap<>();
    private PatternPathRouter<DispatchedEndpoint> endpointPatternPathRouter = PatternPathRouter.create();

    //Makes this class singleton.
    private EndpointsRegistryImpl() {
    }

    /**
     * @return the {@link EndpointsRegistryImpl} instance
     */
    public static EndpointsRegistryImpl getInstance() {
        return webSocketEndpointsRegistry;
    }

    /**
     * Adding endpoints to the registry.
     * @param webSocketEndpoints {@link WebSocketEndpoint} to add.
     */
    public void addEndpoint(Object... webSocketEndpoints) throws WebSocketEndpointAnnotationException {
        for (Object endpoint: webSocketEndpoints) {
            DispatchedEndpoint dispatchedEndpoint = dispatchEndpoint(endpoint);
            webSocketEndpointMap.put(dispatchedEndpoint.getUri(), dispatchedEndpoint);
            updatePatternPathRouter();
            LOGGER.info("Endpoint Registered : " + dispatchedEndpoint.getUri());
        }
    }

    /**
     * Remove {@link WebSocketEndpoint} from Registry.
     * @param webSocketEndpoint {@link WebSocketEndpoint} which should be removed.
     * @throws WebSocketEndpointAnnotationException throws when WebSocket {@link javax.websocket.server.ServerEndpoint}
     * is no declared in the endpoint.
     */
    public void removeEndpoint(WebSocketEndpoint webSocketEndpoint) throws WebSocketEndpointAnnotationException {
        DispatchedEndpoint dispatchedEndpoint = dispatchEndpoint(webSocketEndpoint);
        webSocketEndpointMap.remove(dispatchedEndpoint.getUri());
        updatePatternPathRouter();
        LOGGER.info("Removed endpoint : " + dispatchedEndpoint.getUri());
    }

    /**
     * Return the best possible {@link org.wso2.msf4j.internal.router.PatternPathRouter.RoutableDestination}.
     * @param carbonMessage {@link CarbonMessage} to find the URI.
     * @return the best possible {@link org.wso2.msf4j.internal.router.PatternPathRouter.RoutableDestination}.
     */
    public PatternPathRouter.RoutableDestination<DispatchedEndpoint> getRoutableEndpoint(
            CarbonMessage carbonMessage) {
        String uri = (String) carbonMessage.getProperty(Constants.TO);
        LOGGER.info("path : " + uri);
        List<PatternPathRouter.RoutableDestination<DispatchedEndpoint>> routableDestinations =
                endpointPatternPathRouter.getDestinations(uri);
        return getBestEndpoint(routableDestinations, uri);
    }

    @Override
    public Set<Object> getAllEndpoints() {
        return webSocketEndpointMap.entrySet().stream()
                .map(Map.Entry::getValue)
                .map(DispatchedEndpoint::getWebSocketEndpoint)
                .collect(Collectors.toSet());
    }

    /*
    Dispatch the endpoint.
     */
    private DispatchedEndpoint dispatchEndpoint(Object webSocketEndpoint) throws WebSocketEndpointAnnotationException {
        EndpointDispatcher dispatcher = new EndpointDispatcher(webSocketEndpoint);
        return dispatcher.getDispatchedEndpoint();
    }

    /*
    Find the best matching RoutableDestination from the All matching RoutableDestinations
     */
    private PatternPathRouter.RoutableDestination<DispatchedEndpoint> getBestEndpoint(
            List<PatternPathRouter.RoutableDestination<DispatchedEndpoint>> routableDestinationList,
            String requestUri) {
        PatternPathRouter.RoutableDestination<DispatchedEndpoint> bestRoutableDestination = null;
        int currentBestHitCount = 0;
        for (PatternPathRouter.RoutableDestination<DispatchedEndpoint>
                currentRoutableDestination: routableDestinationList) {
            int tempCount = getHitCount(currentRoutableDestination.getDestination().getUri().split("/"),
                                        requestUri.split("/"));

            if (tempCount > currentBestHitCount) {
                bestRoutableDestination = currentRoutableDestination;
                currentBestHitCount = tempCount;
            }
        }
        return bestRoutableDestination;
    }

    /*
    Update the PatternPathRouter when adding and removing an endpoint.
     */
    private void updatePatternPathRouter() {
        endpointPatternPathRouter = PatternPathRouter.create();
        webSocketEndpointMap.entrySet().forEach(
                entry -> {
                    DispatchedEndpoint dispatchedEndpoint = entry.getValue();
                    endpointPatternPathRouter.add(dispatchedEndpoint.getUri(), dispatchedEndpoint);
                }
        );
    }

    /*
    Compare and find number of equalities of the Endpoint URI and Requested URI
     */
    private int getHitCount(String[] destinationUriChunkArray, String[] requestUriChunkArray) {
        int count = 0;
        for (int i = 0; i < destinationUriChunkArray.length; i++) {
            if (destinationUriChunkArray[i].equals(requestUriChunkArray[i])) {
                count++;
            }
        }
        return count;
    }
}
