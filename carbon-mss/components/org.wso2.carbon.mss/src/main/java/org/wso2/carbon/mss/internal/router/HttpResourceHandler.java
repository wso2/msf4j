/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.mss.internal.router;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.mss.HandlerContext;
import org.wso2.carbon.mss.HttpHandler;
import org.wso2.carbon.mss.HttpHandlerWrapper;
import org.wso2.carbon.mss.HttpResponder;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

/**
 * HttpResourceHandler handles the http request. HttpResourceHandler looks up all Jax-rs annotations in classes
 * and dispatches to appropriate method on receiving requests.
 */
public final class HttpResourceHandler implements HttpHandler {

    private static final Logger LOG = LoggerFactory.getLogger(HttpResourceHandler.class);

    private final PatternPathRouterWithGroups<HttpResourceModel> patternRouter = PatternPathRouterWithGroups.create();
    private final Iterable<HttpHandler> handlers;
    private final Iterable<HandlerHook> handlerHooks;
    private final URLRewriter urlRewriter;

    /**
     * Construct HttpResourceHandler. Reads all annotations from all the handler classes and methods passed in, constructs
     * patternPathRouter which is routable by path to {@code HttpResourceModel} as destination of the route.
     *
     * @param handlers         Iterable of HttpHandler.
     * @param handlerHooks     Iterable of HandlerHook.
     * @param urlRewriter      URL re-writer.
     * @param exceptionHandler Exception handler
     */
    public HttpResourceHandler(Iterable<? extends HttpHandler> handlers, Iterable<? extends HandlerHook> handlerHooks,
                               URLRewriter urlRewriter, ExceptionHandler exceptionHandler) {
        //Store the handlers to call init and destroy on all handlers.
        this.handlers = ImmutableList.copyOf(handlers);
        this.handlerHooks = ImmutableList.copyOf(handlerHooks);
        this.urlRewriter = urlRewriter;

        for (HttpHandler handler : handlers) {
            String basePath = "";
            Class<?> serviceClass;
            if(HttpHandlerWrapper.class.isInstance(handler)){
                serviceClass = ((HttpHandlerWrapper)handler).getServiceClass();
            } else {
                serviceClass = handler.getClass();
            }

            if (serviceClass.isAnnotationPresent(Path.class)) {
                basePath = serviceClass.getAnnotation(Path.class).value();
            }

            for (Method method : serviceClass.getDeclaredMethods()) {
                if (method.getParameterTypes().length >= 2 &&
                        method.getParameterTypes()[0].isAssignableFrom(HttpRequest.class) &&
                        method.getParameterTypes()[1].isAssignableFrom(HttpResponder.class) &&
                        Modifier.isPublic(method.getModifiers())) {

                    String relativePath = "";
                    if (method.getAnnotation(Path.class) != null) {
                        relativePath = method.getAnnotation(Path.class).value();
                    }
                    String absolutePath = String.format("%s/%s", basePath, relativePath);
                    Set<HttpMethod> httpMethods = getHttpMethods(method);
                    Preconditions.checkArgument(httpMethods.size() >= 1,
                            String.format("No HttpMethod found for method: %s", method.getName()));
                    patternRouter.add(absolutePath, new HttpResourceModel(httpMethods, absolutePath, method,
                            handler, exceptionHandler));
                } else {
                    LOG.trace("Not adding method {}({}) to path routing like. HTTP calls will not be routed to this method",
                            method.getName(), method.getParameterTypes());
                }
            }
        }
    }

    /**
     * Fetches the HttpMethod from annotations and returns String representation of HttpMethod.
     * Return emptyString if not present.
     *
     * @param method Method handling the http request.
     * @return String representation of HttpMethod from annotations or emptyString as a default.
     */
    private Set<HttpMethod> getHttpMethods(Method method) {
        Set<HttpMethod> httpMethods = Sets.newHashSet();

        if (method.isAnnotationPresent(GET.class)) {
            httpMethods.add(HttpMethod.GET);
        }
        if (method.isAnnotationPresent(PUT.class)) {
            httpMethods.add(HttpMethod.PUT);
        }
        if (method.isAnnotationPresent(POST.class)) {
            httpMethods.add(HttpMethod.POST);
        }
        if (method.isAnnotationPresent(DELETE.class)) {
            httpMethods.add(HttpMethod.DELETE);
        }

        return ImmutableSet.copyOf(httpMethods);
    }

    /**
     * Call the appropriate handler for handling the httprequest. 404 if path is not found. 405 if path is found but
     * httpMethod does not match what's configured.
     *
     * @param request   instance of {@code HttpRequest}
     * @param responder instance of {@code HttpResponder} to handle the request.
     */
    public void handle(HttpRequest request, HttpResponder responder) {

        if (urlRewriter != null) {
            try {
                request.setUri(URI.create(request.getUri()).normalize().toString());
                if (!urlRewriter.rewrite(request, responder)) {
                    return;
                }
            } catch (Throwable t) {
                responder.sendString(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        String.format("Caught exception processing request. Reason: %s",
                                t.getMessage()));
                LOG.error("Exception thrown during rewriting of uri {}", request.getUri(), t);
                return;
            }
        }

        try {
            String path = URI.create(request.getUri()).normalize().getPath();

            List<PatternPathRouterWithGroups.RoutableDestination<HttpResourceModel>> routableDestinations
                    = patternRouter.getDestinations(path);

            PatternPathRouterWithGroups.RoutableDestination<HttpResourceModel> matchedDestination =
                    getMatchedDestination(routableDestinations, request.getMethod(), path);

            if (matchedDestination != null) {
                //Found a httpresource route to it.
                HttpResourceModel httpResourceModel = matchedDestination.getDestination();

                // Call preCall method of handler hooks.
                boolean terminated = false;
                HandlerInfo info = new HandlerInfo(httpResourceModel.getMethod().getDeclaringClass().getName(),
                        httpResourceModel.getMethod().getName());
                for (HandlerHook hook : handlerHooks) {
                    if (!hook.preCall(request, responder, info)) {
                        // Terminate further request processing if preCall returns false.
                        terminated = true;
                        break;
                    }
                }

                // Call httpresource method
                if (!terminated) {
                    // Wrap responder to make post hook calls.
                    responder = new WrappedHttpResponder(responder, handlerHooks, request, info);
                    if (httpResourceModel.handle(request, responder, matchedDestination.getGroupNameValues()).isStreaming()) {
                        responder.sendString(HttpResponseStatus.METHOD_NOT_ALLOWED,
                                String.format("Body Consumer not supported for internalHttpResponder: %s",
                                        request.getUri()));
                    }
                }
            } else if (routableDestinations.size() > 0) {
                //Found a matching resource but could not find the right HttpMethod so return 405
                responder.sendString(HttpResponseStatus.METHOD_NOT_ALLOWED,
                        String.format("Problem accessing: %s. Reason: Method Not Allowed", request.getUri()));
            } else {
                responder.sendString(HttpResponseStatus.NOT_FOUND, String.format("Problem accessing: %s. Reason: Not Found",
                        request.getUri()));
            }
        } catch (Throwable t) {
            responder.sendString(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    String.format("Caught exception processing request. Reason: %s", t.getMessage()));
            LOG.error("Exception thrown during request processing for uri {}", request.getUri(), t);
        }
    }

    /**
     * Call the appropriate handler for handling the httprequest. 404 if path is not found. 405 if path is found but
     * httpMethod does not match what's configured.
     *
     * @param request   instance of {@code HttpRequest}
     * @param responder instance of {@code HttpResponder} to handle the request.
     * @return HttpMethodInfo object, null if urlRewriter rewrite returns false, also when method cannot be invoked.
     */
    public HttpMethodInfo getDestinationMethod(HttpRequest request, HttpResponder responder) throws Exception {
        if (urlRewriter != null) {
            try {
                request.setUri(URI.create(request.getUri()).normalize().toString());
                if (!urlRewriter.rewrite(request, responder)) {
                    return null;
                }
            } catch (Throwable t) {
                LOG.error("Exception thrown during rewriting of uri {}", request.getUri(), t);
                throw new HandlerException(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        String.format("Caught exception processing request. Reason: %s", t.getMessage()));
            }
        }

        try {
            String path = URI.create(request.getUri()).normalize().getPath();

            List<PatternPathRouterWithGroups.RoutableDestination<HttpResourceModel>> routableDestinations =
                    patternRouter.getDestinations(path);

            PatternPathRouterWithGroups.RoutableDestination<HttpResourceModel> matchedDestination =
                    getMatchedDestination(routableDestinations, request.getMethod(), path);

            if (matchedDestination != null) {
                HttpResourceModel httpResourceModel = matchedDestination.getDestination();

                // Call preCall method of handler hooks.
                boolean terminated = false;
                HandlerInfo info = new HandlerInfo(httpResourceModel.getMethod().getDeclaringClass().getName(),
                        httpResourceModel.getMethod().getName());
                for (HandlerHook hook : handlerHooks) {
                    if (!hook.preCall(request, responder, info)) {
                        // Terminate further request processing if preCall returns false.
                        terminated = true;
                        break;
                    }
                }

                // Call httpresource handle method, return the HttpMethodInfo Object.
                if (!terminated) {
                    // Wrap responder to make post hook calls.
                    responder = new WrappedHttpResponder(responder, handlerHooks, request, info);
                    return httpResourceModel.handle(request, responder, matchedDestination.getGroupNameValues());
                }
            } else if (routableDestinations.size() > 0) {
                //Found a matching resource but could not find the right HttpMethod so return 405
                throw new HandlerException(HttpResponseStatus.METHOD_NOT_ALLOWED, request.getUri());
            } else {
                throw new HandlerException(HttpResponseStatus.NOT_FOUND,
                        String.format("Problem accessing: %s. Reason: Not Found", request.getUri()));
            }
        } catch (Throwable t) {
            if (t instanceof HandlerException) {
                throw (HandlerException) t;
            }
            throw new HandlerException(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    String.format("Caught exception processing request. Reason: %s", t.getMessage()), t);
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
    private PatternPathRouterWithGroups.RoutableDestination<HttpResourceModel>
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

        if (matchedDestinations.size() > 1) {
            throw new IllegalStateException(String.format("Multiple matched handlers found for request uri %s", requestUri));
        } else if (matchedDestinations.size() == 1) {
            return matchedDestinations.get(0);
        }
        return null;
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

    @Override
    public void init(HandlerContext context) {
        for (HttpHandler handler : handlers) {
            handler.init(context);
        }
    }

    @Override
    public void destroy(HandlerContext context) {
        for (HttpHandler handler : handlers) {
            handler.destroy(context);
        }
    }
}
