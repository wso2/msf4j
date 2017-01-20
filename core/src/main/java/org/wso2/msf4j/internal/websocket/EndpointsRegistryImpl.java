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
import org.wso2.msf4j.WebSocketEndpoint;
import org.wso2.msf4j.WebSocketEndpointsRegistry;
import org.wso2.msf4j.internal.router.PatternPathRouter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.websocket.server.ServerEndpoint;

/**
 * Implementation for {@link WebSocketEndpointsRegistry}
 * Endpoints are Dispatched and Stored in a {@link PatternPathRouter}. So when new request comes it will be routed to
 * best matching URI.
 *
 * @since 1.0.0
 */
public class EndpointsRegistryImpl implements WebSocketEndpointsRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(EndpointsRegistryImpl.class);
    private static final EndpointsRegistryImpl webSocketEndpointsRegistry = new EndpointsRegistryImpl();
    private PatternPathRouter<DispatchedEndpoint> endpointPatternPathRouter = PatternPathRouter.create();

    private EndpointsRegistryImpl() {
    }

    /**
     * @return the {@link EndpointsRegistryImpl} instance
     */
    public static EndpointsRegistryImpl getInstance() {
        return webSocketEndpointsRegistry;
    }

    public void addEndpoint(Object... webSocketEndpoints) {
        Arrays.stream(webSocketEndpoints).forEach(
                webSocketEndpoint -> {
                    try {
                        DispatchedEndpoint dispatchedEndpoint = dispatchEndpoint(webSocketEndpoint);
                        endpointPatternPathRouter.add(dispatchedEndpoint.getUri(), dispatchedEndpoint);
                        LOGGER.info("Endpoint Registered : " + dispatchedEndpoint.getUri().toString());
                    } catch (Exception e) {
                        LOGGER.error(e.toString());
                    }
                }
        );
    }

    public void removeEndpoint(WebSocketEndpoint webSocketEndpoint) {
        String uri = webSocketEndpoint.getClass().getAnnotation(ServerEndpoint.class).value();
//        List<PatternPathRouter.RoutableDestination<DispatchedEndpoint>> routableDestinations =
//                endpointPatternPathRouter.getDestinations(uri);
        LOGGER.info("Removed endpoint : " + uri);
        //TODO : Implement remove correctly

    }

    public DispatchedEndpoint dispatchEndpoint(Object webSocketEndpoint) throws Exception {
        EndpointDispatcher dispatcher = new EndpointDispatcher(webSocketEndpoint);
        return dispatcher.getDispatchedEndpoint();
    }

    public PatternPathRouter.RoutableDestination<DispatchedEndpoint> getRoutableEndpoint(
            CarbonMessage carbonMessage) {
        String uri = (String) carbonMessage.getProperty(Constants.TO);
        List<PatternPathRouter.RoutableDestination<DispatchedEndpoint>> routableDestinations =
                endpointPatternPathRouter.getDestinations(uri);
        return getBestEndpoint(routableDestinations, uri);
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

    @Override
    public Set<Object> getAllEndpoints() {
        //TODO : Implementation
        return null;
    }
}
