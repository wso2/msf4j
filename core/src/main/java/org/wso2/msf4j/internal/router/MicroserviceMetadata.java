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

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.HttpResponder;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.ServiceMethodInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

/**
 * MicroserviceMetadata handles the http request. HttpResourceHandler looks up all Jax-rs annotations in classes
 * and dispatches to appropriate method on receiving requests.
 */
public final class MicroserviceMetadata {

    private static final Logger log = LoggerFactory.getLogger(MicroserviceMetadata.class);

    private final PatternPathRouterWithGroups<HttpResourceModel> patternRouter = PatternPathRouterWithGroups.create();
    private final Iterable<Interceptor> interceptors;
    private final URLRewriter urlRewriter;

    /**
     * Construct HttpResourceHandler. Reads all annotations from all the handler classes and methods passed in,
     * constructs patternPathRouter which is routable by path to {@code HttpResourceModel} as destination of the route.
     *
     * @param handlers         Iterable of HttpHandler.
     * @param interceptors     Iterable of interceptors.
     * @param urlRewriter      URL re-writer.
     * @param exceptionHandler Exception handler
     */
    public MicroserviceMetadata(Iterable<? extends Object> handlers, Iterable<? extends Interceptor> interceptors,
                                URLRewriter urlRewriter, ExceptionHandler exceptionHandler) {
        //Store the handlers to call init and destroy on all handlers.
        this.interceptors = ImmutableList.copyOf(interceptors);
        this.urlRewriter = urlRewriter;

        for (Object handler : handlers) {
            String basePath = "";
            if (handler.getClass().isAnnotationPresent(Path.class)) {
                basePath = handler.getClass().getAnnotation(Path.class).value();
            }

            for (Method method : handler.getClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(PostConstruct.class) || method.isAnnotationPresent(PreDestroy.class)) {
                    continue;
                }

                if (Modifier.isPublic(method.getModifiers()) && isHttpMethodAvailable(method)) {
                    String relativePath = "";
                    if (method.getAnnotation(Path.class) != null) {
                        relativePath = method.getAnnotation(Path.class).value();
                    }
                    String absolutePath = String.format("%s/%s", basePath, relativePath);
                    patternRouter.add(absolutePath, new HttpResourceModel(absolutePath, method,
                            handler, new ExceptionHandler()));
                } else {
                    log.trace("Not adding method {}({}) to path routing like. " +
                                    "HTTP calls will not be routed to this method",
                            method.getName(), method.getParameterTypes());
                }
            }
        }
    }

    private boolean isHttpMethodAvailable(Method method) {
        return method.isAnnotationPresent(GET.class) ||
                method.isAnnotationPresent(PUT.class) ||
                method.isAnnotationPresent(POST.class) ||
                method.isAnnotationPresent(DELETE.class);
    }

    /**
     * Call the appropriate handler for handling the httprequest. 404 if path is not found. 405 if path is found but
     * httpMethod does not match what's configured.
     *
     * @param request   instance of {@code HttpRequest}
     * @param responder instance of {@code HttpResponder} to handle the request.
     * @return HttpMethodInfo object, null if urlRewriter rewrite returns false, also when method cannot be invoked.
     * @throws HandlerException If URL rewriting fails
     */
    public HttpMethodInfoBuilder getDestinationMethod(HttpRequest request, HttpResponder responder)
            throws HandlerException {
        if (urlRewriter != null) {
            try {
                request.setUri(URI.create(request.getUri()).normalize().toString());
                if (!urlRewriter.rewrite(request, responder)) {
                    return null;
                }
            } catch (Throwable t) {
                log.error("Exception thrown during rewriting of uri {}", request.getUri(), t);
                throw new HandlerException(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        String.format("Caught exception processing request. Reason: %s", t.getMessage()));
            }
        }

        String acceptHeaderStr = request.headers().get(HttpHeaders.Names.ACCEPT);
        List<String> acceptHeader = (acceptHeaderStr != null) ?
                Arrays.asList(acceptHeaderStr.split("\\s*,\\s*"))
                        .stream()
                        .map(mediaType -> mediaType.split("\\s*;\\s*")[0])
                        .collect(Collectors.toList()) :
                null;

        String contentTypeHeaderStr = request.headers().get(HttpHeaders.Names.CONTENT_TYPE);
        //Trim specified charset since UTF-8 is assumed
        String contentTypeHeader = (contentTypeHeaderStr != null) ? contentTypeHeaderStr.split("\\s*;\\s*")[0] : null;

        try {
            String path = URI.create(request.getUri()).normalize().getPath();

            List<PatternPathRouterWithGroups.RoutableDestination<HttpResourceModel>>
                    routableDestinations = patternRouter.getDestinations(path);

            List<PatternPathRouterWithGroups.RoutableDestination<HttpResourceModel>>
                    matchedDestinations = getMatchedDestination(routableDestinations, request.getMethod(), path);

            if (!matchedDestinations.isEmpty()) {
                PatternPathRouterWithGroups.RoutableDestination<HttpResourceModel>
                        matchedDestination = matchedDestinations.stream()
                        .filter(matchedDestination1 -> {
                            return matchedDestination1.getDestination().matchConsumeMediaType(contentTypeHeader)
                                    && matchedDestination1.getDestination().matchProduceMediaType(acceptHeader);
                        }).findFirst().get();
                HttpResourceModel httpResourceModel = matchedDestination.getDestination();

                // Call preCall method of handler interceptors.
                boolean terminated = false;
                ServiceMethodInfo serviceMethodInfo = new ServiceMethodInfo(httpResourceModel.getMethod().
                        getDeclaringClass().getName(), httpResourceModel.getMethod());
                for (Interceptor interceptor : interceptors) {
                    if (!interceptor.preCall(request, responder, serviceMethodInfo)) {
                        // Terminate further request processing if preCall returns false.
                        terminated = true;
                        break;
                    }
                }

                // Call httpresource handle method, return the HttpMethodInfo Object.
                if (!terminated) {
                    // Wrap responder to make post hook calls.
                    responder = new WrappedHttpResponder(responder, interceptors, request, serviceMethodInfo);
                    return HttpMethodInfoBuilder
                            .getInstance()
                            .httpResourceModel(httpResourceModel)
                            .httpRequest(request)
                            .httpResponder(responder)
                            .requestInfo(matchedDestination.getGroupNameValues(),
                                    contentTypeHeader,
                                    acceptHeader);
                }
            } else if (!routableDestinations.isEmpty()) {
                //Found a matching resource but could not find the right HttpMethod so return 405
                throw new HandlerException(HttpResponseStatus.METHOD_NOT_ALLOWED, request.getUri());
            } else {
                throw new HandlerException(HttpResponseStatus.NOT_FOUND,
                        String.format("Problem accessing: %s. Reason: Not Found", request.getUri()));
            }
        } catch (NoSuchElementException ex) {
            throw new HandlerException(HttpResponseStatus.UNSUPPORTED_MEDIA_TYPE,
                    String.format("Problem accessing: %s. Reason: Unsupported Media Type", request.getUri()), ex);
        }
        return null;
    }

    /**
     * Get HttpResourceModel which matches the HttpMethod of the request.
     *
     * @param routableDestinations List of ResourceModels.
     * @param targetHttpMethod     HttpMethod.
     * @param requestUri           request URI.
     * @return RoutableDestination that matches httpMethod that needs to be handled. null if there are no matches.
     */
    private List<PatternPathRouterWithGroups.RoutableDestination<HttpResourceModel>>
    getMatchedDestination(List<PatternPathRouterWithGroups.RoutableDestination<HttpResourceModel>> routableDestinations,
                          HttpMethod targetHttpMethod, String requestUri) {

        Iterable<String> requestUriParts = Splitter.on('/').omitEmptyStrings().split(requestUri);
        List<PatternPathRouterWithGroups.RoutableDestination<HttpResourceModel>> matchedDestinations =
                Lists.newArrayListWithExpectedSize(routableDestinations.size());
        int maxExactMatch = 0;
        int maxGroupMatch = 0;
        int maxPatternLength = 0;

        for (PatternPathRouterWithGroups.RoutableDestination<HttpResourceModel> destination : routableDestinations) {
            HttpResourceModel resourceModel = destination.getDestination();
            int groupMatch = destination.getGroupNameValues().size();

            for (HttpMethod httpMethod : resourceModel.getHttpMethod()) {
                if (targetHttpMethod.equals(httpMethod)) {

                    int exactMatch = getExactPrefixMatchCount(
                            requestUriParts, Splitter.on('/').omitEmptyStrings().split(resourceModel.getPath()));

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
