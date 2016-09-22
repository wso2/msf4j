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
import org.wso2.msf4j.HttpStreamHandler;
import org.wso2.msf4j.HttpStreamer;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;

import static org.wso2.msf4j.internal.router.Util.GROUP_PATTERN;
import static org.wso2.msf4j.internal.router.Util.GROUP_PATTERN_REGEX;
import static org.wso2.msf4j.internal.router.Util.WILD_CARD_PATTERN;

/**
 * HttpMethodInfo is a helper class having state information about the http handler method to be invoked, the handler
 * and arguments required for invocation by the Dispatcher. RequestRouter populates this class and stores in its
 * context as attachment.
 */
public class HttpMethodInfo {

    private final Method method;
    private final Object handler;
    private final Object[] args;
    private MultivaluedMap<String, Object> formParameters = null;
    private Response responder;
    private HttpStreamHandler httpStreamHandler;
    private static final Logger log = LoggerFactory.getLogger(HttpMethodInfo.class);

    /**
     * Construct HttpMethodInfo object for a handler
     * method that does not support streaming.
     *
     * @param method    handler method
     * @param handler   object of the handler method
     * @param args      method arguments array
     * @param responder responder object
     */
    public HttpMethodInfo(Method method,
                          Object handler,
                          Object[] args,
                          MultivaluedMap<String, Object> formParameters,
                          Response responder) {
        this.method = method;
        this.handler = handler;
        this.args = Arrays.copyOf(args, args.length);
        this.formParameters = formParameters;
        this.responder = responder;
    }

    /**
     * Construct HttpMethodInfo object for a streaming
     * supported handler method.
     *
     * @param method       handler method
     * @param handler      object of the handler method
     * @param args         method arguments array
     * @param responder    responder object
     * @param httpStreamer streaming handler
     * @throws HandlerException throws when HttpMethodInfo construction is unsuccessful
     */
    public HttpMethodInfo(Method method,
                          Object handler,
                          Object[] args,
                          MultivaluedMap<String, Object> formParameters,
                          Response responder,
                          HttpStreamer httpStreamer) throws HandlerException {
        this(method, handler, args, formParameters, responder);

        if (!method.getReturnType().equals(Void.TYPE)) {
            throw new HandlerException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR,
                    "Resource method should be void if it accepts chunked requests");
        }
        try {
            method.invoke(handler, args);
        } catch (InvocationTargetException e) {
            throw new HandlerException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR,
                    "Resource method invocation failed", e.getTargetException());
        } catch (IllegalAccessException e) {
            throw new HandlerException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR,
                    "Resource method invocation access failed", e);
        }
        httpStreamHandler = httpStreamer.getHttpStreamHandler();
        if (httpStreamHandler == null) {
            throw new HandlerException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR,
                    "Streaming unsupported");
        }
        httpStreamHandler.init(this.responder);
    }

    /**
     * Calls the http resource method.
     *
     * @param request original request
     * @param destination matching Destinations for the route
     * @throws Exception if error occurs while invoking the resource method
     */
    public void invoke(Request request, PatternPathRouter.RoutableDestination<HttpResourceModel> destination)
            throws Exception {
        Object returnVal = method.invoke(handler, args);
        returnVal = invokeSubResource(request, destination, returnVal);
        responder.setEntity(returnVal);
        responder.send();
    }

    private Object invokeSubResource(Request request,
                                   PatternPathRouter.RoutableDestination<HttpResourceModel> destination,
                                   Object returnVal) throws Exception {
        // If this is a sub resource locator need to find and invoke the correct method
        if (destination.getDestination().isSubResourceLocator()) {
            String requestPath = request.getUri();
            if (requestPath.endsWith("/")) {
                requestPath = requestPath.substring(0, requestPath.length() - 1);
            }
            if (requestPath.contains("?")) {
                requestPath = requestPath.substring(0, requestPath.indexOf("?"));
            }

            if (!destination.getDestination().isSubResourceScanned()) {
                // Scan the return object class to search the methods
                for (Method method : returnVal.getClass().getMethods()) {
                    if (Modifier.isPublic(method.getModifiers()) && Util.isHttpMethodAvailable(method)) {
                        String relativePath = "";
                        if (method.getAnnotation(Path.class) != null) {
                            relativePath = method.getAnnotation(Path.class).value();
                        }
                        if (relativePath.startsWith("/")) {
                            relativePath = relativePath.substring(1);
                        }
                        String absolutePath = relativePath.isEmpty() ? destination.getDestination().getPath() :
                                              String.format("%s/%s", destination.getDestination().getPath(),
                                                            relativePath);
                        HttpResourceModel resourceModel = new HttpResourceModel(absolutePath, method, returnVal, false);
                        resourceModel.setParent(destination.getDestination());
                        SubresourceKey subResKey = new SubresourceKey(absolutePath, method.getDeclaringClass(),
                                                                      resourceModel.getHttpMethod());
                        destination.getDestination().addSubResources(subResKey, resourceModel);
                    } else if (Modifier.isPublic(method.getModifiers()) && method.getAnnotation(Path.class) != null) {
                        // Sub resource locator method
                        String relativePath = method.getAnnotation(Path.class).value();
                        if (relativePath.startsWith("/")) {
                            relativePath = relativePath.substring(1);
                        }
                        String absolutePath = relativePath.isEmpty() ? destination.getDestination().getPath() :
                                              String.format("%s/%s", destination.getDestination().getPath(),
                                                            relativePath);
                        HttpResourceModel resourceModel = new HttpResourceModel(absolutePath, method, returnVal, true);
                        resourceModel.setParent(destination.getDestination());
                        SubresourceKey subResKey =
                                new SubresourceKey(absolutePath, method.getDeclaringClass(), Collections.emptySet());
                        destination.getDestination().addSubResources(subResKey, resourceModel);
                    }
                }
                destination.getDestination().setSubResourceScanned(true);
            }
            String finalRequestPath = requestPath;

            List<Map.Entry<SubresourceKey, HttpResourceModel>> entries =
                    destination.getDestination().getSubResources().entrySet().stream()
                               .filter(e -> e.getValue().getHttpMethod().contains(request.getHttpMethod()) &&
                                            finalRequestPath.matches(e.getKey().getPath().replaceAll(GROUP_PATTERN,
                                                                                                GROUP_PATTERN_REGEX)) &&
                                            returnVal.getClass().equals(e.getKey().getTypedClass()))
                               .collect(Collectors.toList());
            Optional<Map.Entry<SubresourceKey, HttpResourceModel>> entry = entries.stream().filter(
                    entryPair -> entryPair.getValue().matchConsumeMediaType(request.getContentType()) &&
                                 entryPair.getValue().matchProduceMediaType(request.getAcceptTypes()))
                                                                                  .findFirst();

            HttpResourceModel resourceModel;
            if (entry.isPresent()) {
                resourceModel = entry.get().getValue();
            } else {
                // Another sub-resource call
                String finalRequestPath1 = requestPath;
                entries = destination.getDestination().getSubResources().entrySet().stream()
                           .filter(e -> finalRequestPath1
                                                .matches(e.getKey().getPath()
                                                          .replaceAll(GROUP_PATTERN, GROUP_PATTERN_REGEX).concat(".*"))
                               && e.getValue().isSubResourceLocator()
                               && returnVal.getClass().equals(e.getKey().getTypedClass())).collect(Collectors.toList());
                entry = entries.stream().filter(entryPair ->
                                                entryPair
                                                        .getValue()
                                                        .matchConsumeMediaType(
                                                                request.getContentType()) &&
                                                entryPair
                                                        .getValue()
                                                        .matchProduceMediaType(
                                                                request.getAcceptTypes()))
                       .findFirst();
                if (entry.isPresent()) {
                    resourceModel = entry.get().getValue();
                } else {
                    throw new HandlerException(javax.ws.rs.core.Response.Status.NOT_FOUND,
                                               String.format("Problem accessing: %s. Reason: Not Found", requestPath));
                }
            }

            // Process path to get the PathParam values
            Path declaredAnnotation = resourceModel.getMethod().getDeclaredAnnotation(Path.class);
            String[] parts = declaredAnnotation.value().split("/");
            StringBuilder sb = new StringBuilder();
            List<String> groupNames = new ArrayList<>();
            for (String part : parts) {
                Matcher groupMatcher = Pattern.compile(GROUP_PATTERN).matcher(part);
                if (groupMatcher.matches()) {
                    groupNames.add(Util.stripBraces(part));
                    sb.append("([^/]+)");
                } else if (WILD_CARD_PATTERN.matcher(part).matches()) {
                    sb.append(".*?");
                } else {
                    sb.append(part);
                }
                sb.append("/");
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
            }

            Map<String, String> groupNameValues = new HashMap<>();
            destination.getGroupNameValues().entrySet().forEach(e -> groupNameValues.put(e.getKey(), e.getValue()));

            //Get the sub resource path
            String[] paths =
                    requestPath.split(destination.getDestination().getPath().replaceAll(GROUP_PATTERN, "([^/]+)"));
            String subResPath = "/";
            if (paths.length != 0) {
                subResPath = paths[1];
            }

            Pattern pattern = Pattern.compile(sb.toString() + ".*");
            Matcher matcher = pattern.matcher(subResPath);
            if (matcher.matches()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    groupNameValues.putIfAbsent(groupNames.get(i - 1), matcher.group(i));
                }
            }
            // Invoke the sub-resource method
            HttpResourceModelProcessor httpSubResourceModelProcessor = new HttpResourceModelProcessor(resourceModel);
            httpSubResourceModelProcessor.setFormParameters(formParameters);
            responder.setMediaType(
                    Util.getResponseType(request.getAcceptTypes(), resourceModel.getProducesMediaTypes()));
            HttpMethodInfo httpMethodInfo = httpSubResourceModelProcessor
                    .buildHttpMethodInfo(request, responder, groupNameValues);

            PatternPathRouter.RoutableDestination<HttpResourceModel> newDestination =
                    new PatternPathRouter.RoutableDestination<>(resourceModel, groupNameValues);
            Object returnedValue = httpMethodInfo.method.invoke(httpMethodInfo.handler, httpMethodInfo.args);
            return httpMethodInfo.invokeSubResource(request, newDestination, returnedValue);
        }
        return returnVal;
    }

    /**
     * If chunk handling is supported provide chunks directly.
     *
     * @param chunk chunk content
     * @throws Exception if error occurs while invoking streaming handlers
     */
    public void chunk(ByteBuffer chunk) throws Exception {
        try {
            httpStreamHandler.chunk(chunk);
        } catch (Throwable e) {
            log.error("Exception while invoking streaming handlers", e);
            httpStreamHandler.error(e);
            throw e;
        }
    }

    /**
     * If chunk handling is supported end streaming chunks.
     *
     * @throws Exception if error occurs while stopping streaming handlers
     */
    public void end() throws Exception {
        try {
            httpStreamHandler.end();
        } catch (Throwable e) {
            log.error("Exception while invoking streaming handlers", e);
            httpStreamHandler.error(e);
            throw e;
        }
    }

    /**
     * Return true if the handler method supports streaming.
     *
     * @return boolean true if streaming is supported
     */
    public boolean isStreamingSupported() {
        return httpStreamHandler != null;
    }
}
