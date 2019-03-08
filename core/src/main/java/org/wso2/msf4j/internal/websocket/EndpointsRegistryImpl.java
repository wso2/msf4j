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

package org.wso2.msf4j.internal.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.internal.router.PatternPathRouter;
import org.wso2.msf4j.websocket.WebSocketEndpointsRegistry;
import org.wso2.msf4j.websocket.exception.WebSocketEndpointAnnotationException;
import org.wso2.msf4j.websocket.exception.WebSocketEndpointMethodReturnTypeException;
import org.wso2.msf4j.websocket.exception.WebSocketMethodParameterException;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation for {@link WebSocketEndpointsRegistry}.
 * Endpoints are Dispatched and Stored in a {@link PatternPathRouter}. So when new request comes it will be routed to
 * best matching URI.
 */
public class EndpointsRegistryImpl implements WebSocketEndpointsRegistry {

    private static final Logger log = LoggerFactory.getLogger(EndpointsRegistryImpl.class);
    private static final EndpointsRegistryImpl webSocketEndpointsRegistry = new EndpointsRegistryImpl();
    private final EndpointValidator validator = new EndpointValidator();

    // Map <uri, WebSocketEndpoint>
    private final Map<String, Object> webSocketEndpointMap = new ConcurrentHashMap<>();
    // PatterPathRouter<WebSocketEndpoint>
    private PatternPathRouter<Object> endpointPatternPathRouter = PatternPathRouter.create();

    // Makes this class singleton.
    private EndpointsRegistryImpl() {
    }

    /**
     * @return this singleton instance of {@link EndpointsRegistryImpl}.
     */
    public static EndpointsRegistryImpl getInstance() {
        return webSocketEndpointsRegistry;
    }

    /**
     * Adding endpoints to the registry.
     *
     * @param webSocketEndpoints to add.
     * @return the endpoints which could not deploy due to validation errors.
     */
    public List<Object> addEndpoint(Object... webSocketEndpoints) {
        List<Object> endpointsWithError = new LinkedList<>();
        Arrays.stream(webSocketEndpoints).forEach(
                endpoint -> {
                    EndpointDispatcher dispatcher = new EndpointDispatcher();
                    try {
                        if (validator.validate(endpoint)) {
                            webSocketEndpointMap.put(dispatcher.getUri(endpoint), endpoint);
                            log.info("Endpoint Registered : " + dispatcher.getUri(endpoint));
                        }
                    } catch (WebSocketEndpointAnnotationException e) {
                        endpointsWithError.add(endpoint);
                        log.error("Cannot deploy endpoint" +
                                          ": server endpoint not defined." + System.lineSeparator() + e.toString());
                    } catch (WebSocketMethodParameterException e) {
                        endpointsWithError.add(endpoint);
                        log.error("Cannot deploy endpoint " + endpoint.getClass().getName() +
                                          ": error method definition." + System.lineSeparator() + e.toString());
                    } catch (WebSocketEndpointMethodReturnTypeException e) {
                        endpointsWithError.add(endpoint);
                        log.error("Cannot deploy endpoint " + endpoint.getClass().getName() +
                                          ": invalid method return type." + System.lineSeparator() + e.toString());
                    }

                }
        );
        updatePatternPathRouter();
        return endpointsWithError;
    }

    /**
     * Remove WebSocket Endpoint from Registry.
     *
     * @param webSocketEndpoint which should be removed.
     */
    public void removeEndpoint(Object webSocketEndpoint) {
        EndpointDispatcher dispatcher = new EndpointDispatcher();
        webSocketEndpointMap.remove(dispatcher.getUri(webSocketEndpoint));
        updatePatternPathRouter();
        log.info("Removed endpoint : " + dispatcher.getUri(webSocketEndpoint));
    }

    /**
     * Extract the best possible {@link org.wso2.msf4j.internal.router.PatternPathRouter.RoutableDestination}.
     *
     * @param uri String of the desired destination endpoint.
     * @return the best possible {@link org.wso2.msf4j.internal.router.PatternPathRouter.RoutableDestination}.
     */
    public PatternPathRouter.RoutableDestination<Object> getRoutableEndpoint(String uri) {
        List<PatternPathRouter.RoutableDestination<Object>> routableDestinations =
                endpointPatternPathRouter.getDestinations(uri);
        return getBestEndpoint(routableDestinations, uri);
    }

    @Override
    public Set<Object> getAllEndpoints() {
        return webSocketEndpointMap.entrySet().stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());
    }

    /**
     * Find the best matching RoutableDestination from the All matching RoutableDestinations.
     *
     * @param routableDestinationList routable destication list for a given uri.
     * @param requestUri uri which the best endpoint should be found for.
     * @return the best possible routable destination for the requested uri.
     */
    private PatternPathRouter.RoutableDestination<Object> getBestEndpoint(
            List<PatternPathRouter.RoutableDestination<Object>> routableDestinationList, String requestUri) {
        PatternPathRouter.RoutableDestination<Object> bestRoutableDestination = null;
        int currentBestHitCount = 0;
        for (PatternPathRouter.RoutableDestination<Object>
                currentRoutableDestination: routableDestinationList) {
            int tempCount = getHitCount(new EndpointDispatcher().getUri(currentRoutableDestination.getDestination())
                                                .split("/"), requestUri.split("/"));
            if (tempCount > currentBestHitCount) {
                bestRoutableDestination = currentRoutableDestination;
                currentBestHitCount = tempCount;
            }
        }
        return bestRoutableDestination;
    }

    /**
     * Update the PatternPathRouter when adding and removing an endpoint.
     */
    private void updatePatternPathRouter() {
        endpointPatternPathRouter = PatternPathRouter.create();
        webSocketEndpointMap.entrySet().forEach(
                entry -> endpointPatternPathRouter.add(entry.getKey(), entry.getValue())
        );
    }

    //

    /**
     * Compare and find number of equalities of the Endpoint URI and Requested URI.
     *
     * @param destinationUriChunkArray chunk words of the endpoint uri.
     * @param requestUriChunkArray chunk words of the requested uri.
     * @return hit count - how many words are matched endpoint uri against requested uri.
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
