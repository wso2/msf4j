/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.msf4j.internal.router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.util.Utils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * MicroserviceMetadata handles the http request. HttpResourceHandler looks up all Jax-rs annotations in classes
 * and dispatches to appropriate method on receiving requests.
 */
public final class MicroserviceMetadata {

    private static final Logger log = LoggerFactory.getLogger(MicroserviceMetadata.class);

    private final PatternPathRouter<HttpResourceModel> patternRouter = PatternPathRouter.create();

    /**
     * Construct HttpResourceHandler. Reads all annotations from all the handler classes and methods passed in,
     * constructs patternPathRouter which is routable by path to {@code HttpResourceModel} as destination of the route.
     *
     * @param services Iterable of HttpHandler
     */
    public MicroserviceMetadata(Iterable<? extends Object> services) {
        //Store the services to call init and destroy on all services.

        for (Object service : services) {
            String basePath = "";
            if (service.getClass().isAnnotationPresent(Path.class)) {
                basePath = service.getClass().getAnnotation(Path.class).value();
            }

            for (Method method : service.getClass().getMethods()) {
                if (method.isAnnotationPresent(PostConstruct.class) || method.isAnnotationPresent(PreDestroy.class)) {
                    continue;
                }

                if (Modifier.isPublic(method.getModifiers()) && isHttpMethodAvailable(method)) {
                    String relativePath = "";
                    if (method.getAnnotation(Path.class) != null) {
                        relativePath = method.getAnnotation(Path.class).value();
                    }
                    String absolutePath = String.format("%s/%s", basePath, relativePath);
                    patternRouter.add(absolutePath, new HttpResourceModel(absolutePath, method, service, false));
                } else if (Modifier.isPublic(method.getModifiers()) && method.getAnnotation(Path.class) != null) {
                    // Sub resource locator method
                    String relativePath = method.getAnnotation(Path.class).value();
                    if (relativePath.startsWith("/")) {
                        relativePath = relativePath.substring(1);
                    }
                    String absolutePath = String.format("%s/%s", basePath, relativePath);
                    patternRouter.add(absolutePath, new HttpResourceModel(absolutePath, method, service, true));
                } else {
                    log.trace("Not adding method {}({}) to path routing like. " +
                                    "HTTP calls will not be routed to this method",
                            method.getName(), method.getParameterTypes());
                }
            }
        }
    }

    /**
     * Register given service object with the given base path. Path annotion of the service class will be ignore,
     * instead use the provided base path.
     *
     * @param service HttpHandler object
     * @param basePath Path the handler should be registered
     */
    public void addMicroserviceMetadata(final Object service, String basePath) {
        //Store the services to call init and destroy on all services.
        for (Method method : service.getClass().getMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class) || method.isAnnotationPresent(PreDestroy.class)) {
                continue;
            }

            if (Modifier.isPublic(method.getModifiers()) && isHttpMethodAvailable(method)) {
                String relativePath = "";
                if (method.getAnnotation(Path.class) != null) {
                    relativePath = method.getAnnotation(Path.class).value();
                }
                String absolutePath = String.format("%s/%s", basePath, relativePath);
                patternRouter.add(absolutePath, new HttpResourceModel(absolutePath, method, service, false));
            } else {
                log.trace("Not adding method {}({}) to path routing like. " +
                          "HTTP calls will not be routed to this method", method.getName(), method.getParameterTypes());
            }
        }

    }

    private boolean isHttpMethodAvailable(Method method) {
        return method.isAnnotationPresent(GET.class) ||
                method.isAnnotationPresent(PUT.class) ||
                method.isAnnotationPresent(POST.class) ||
                method.isAnnotationPresent(DELETE.class) ||
                method.isAnnotationPresent(HEAD.class) ||
                method.isAnnotationPresent(OPTIONS.class);
    }

    /**
     * Get destination resource method to match the arrived request.
     * 404 if path is not found. 405 if httpMethod does not match what's configured.
     * 415 if mediatype does not match.
     *
     * @param uri               request uri
     * @param httpMethod        http method of the request
     * @param contentTypeHeader content type of the request
     * @param acceptHeader      accept type of the request
     * @return matching resource method
     * @throws HandlerException if the method not found or content type mismatch
     */
    public PatternPathRouter
            .RoutableDestination<HttpResourceModel> getDestinationMethod(String uri,
                                                                         String httpMethod,
                                                                         String contentTypeHeader,
                                                                         List<String> acceptHeader)
            throws HandlerException {
        try {
            String path = URI.create(uri).normalize().getPath();

            List<PatternPathRouter.RoutableDestination<HttpResourceModel>>
                    routableDestinations = patternRouter.getDestinations(path);

            List<PatternPathRouter.RoutableDestination<HttpResourceModel>>
                    matchedDestinations = getMatchedDestination(routableDestinations, httpMethod, path);

            if (!matchedDestinations.isEmpty()) {
                if (matchedDestinations.size() == 1) {
                    return matchedDestinations.stream().filter(matchedDestination1 ->
                                                                       matchedDestination1.getDestination()
                                                                                          .matchConsumeMediaType(
                                                                                                  contentTypeHeader) &&
                                                                       matchedDestination1.getDestination()
                                                                                          .matchProduceMediaType(
                                                                                                  acceptHeader))
                                              .findFirst().get();
                } else {
                    return matchedDestinations.stream().filter(matchedDestination1 ->
                                                                       matchedDestination1.getDestination()
                                                                                          .matchConsumeMediaType(
                                                                                                  contentTypeHeader) &&
                                                                       matchedDestination1.getDestination()
                                                                                          .matchProduceMediaType(
                                                                                                  acceptHeader))
                                              .filter(destination -> destination.getDestination().getHttpHandler()
                                                                                .getClass() ==
                                                                     destination.getDestination().getMethod()
                                                                                .getDeclaringClass()).findFirst().get();
                }
            } else if (!routableDestinations.isEmpty()) {
                //Found a matching resource but could not find the right HttpMethod so return 405
                throw new HandlerException(Response.Status.METHOD_NOT_ALLOWED, uri);
            } else {
                throw new HandlerException(Response.Status.NOT_FOUND,
                        String.format("Problem accessing: %s. Reason: Not Found", uri));
            }
        } catch (NoSuchElementException ex) {
            throw new HandlerException(Response.Status.UNSUPPORTED_MEDIA_TYPE,
                    String.format("Problem accessing: %s. Reason: Unsupported Media Type", uri), ex);
        }
    }

    /**
     * Get HttpResourceModel which matches the HttpMethod of the request.
     *
     * @param routableDestinations List of ResourceModels.
     * @param targetHttpMethod     HttpMethod.
     * @param requestUri           request URI.
     * @return RoutableDestination that matches httpMethod that needs to be handled. null if there are no matches.
     */
    private List<PatternPathRouter.RoutableDestination<HttpResourceModel>>
    getMatchedDestination(List<PatternPathRouter.RoutableDestination<HttpResourceModel>> routableDestinations,
                          String targetHttpMethod, String requestUri) {

        Iterable<String> requestUriParts = Collections.unmodifiableList(Utils.split(requestUri, "/", true));
        List<PatternPathRouter.RoutableDestination<HttpResourceModel>> matchedDestinations =
                new ArrayList<>(routableDestinations.size());
        int maxExactMatch = 0;
        int maxGroupMatch = 0;
        int maxPatternLength = 0;

        for (PatternPathRouter.RoutableDestination<HttpResourceModel> destination : routableDestinations) {
            HttpResourceModel resourceModel = destination.getDestination();
            int groupMatch = destination.getGroupNameValues().size();

            for (String httpMethod : resourceModel.getHttpMethod()) {
                if (targetHttpMethod.equals(httpMethod)) {
                    int exactMatch = getExactPrefixMatchCount(requestUriParts, Collections
                            .unmodifiableList(Utils.split(resourceModel.getPath(), "/", true)));

                    // When there are multiple matches present, the following precedence order is used -
                    // 1. template path that has highest exact prefix match with the url is chosen.
                    // 2. template path has the maximum groups is chosen.
                    // 3. finally, template path that has the longest length is chosen.
                    if (exactMatch > maxExactMatch) {
                        maxExactMatch = exactMatch;
                        maxGroupMatch = groupMatch;
                        maxPatternLength = resourceModel.getPath().length();

                        matchedDestinations.clear();
                        matchedDestinations.add(destination);
                    } else if (exactMatch == maxExactMatch && groupMatch >= maxGroupMatch) {
                        if (groupMatch > maxGroupMatch || resourceModel.getPath().length() > maxPatternLength) {
                            maxGroupMatch = groupMatch;
                            maxPatternLength = resourceModel.getPath().length();
                            matchedDestinations.clear();
                        }
                        matchedDestinations.add(destination);
                    }
                }
            }
        }
        return matchedDestinations;
    }

    /**
     * @return the number of path components that match from left to right.
     */
    private int getExactPrefixMatchCount(Iterable<String> first, Iterable<String> second) {
        int count = 0;
        for (Iterator<String> fit = first.iterator(), sit = second.iterator(); fit.hasNext() && sit.hasNext(); ) {
            if (fit.next().equals(sit.next())) {
                ++count;
            } else {
                break;
            }
        }
        return count;
    }
}
