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

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import io.netty.handler.codec.http.HttpMethod;
import org.wso2.msf4j.HttpStreamer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

/**
 * HttpResourceModel contains information needed to handle Http call for a given path. Used as a destination in
 * {@code PatternPathRouterWithGroups} to route URI paths to right Http end points.
 */
public final class HttpResourceModel {

    private static final Set<Class<? extends Annotation>> SUPPORTED_PARAM_ANNOTATIONS =
            ImmutableSet.of(PathParam.class, QueryParam.class, HeaderParam.class, Context.class);
    private static final String[] ANY_MEDIA_TYPE = new String[]{"*/*"};
    private static final int STREAMING_REQ_UNKNOWN = 0, STREAMING_REQ_SUPPORTED = 1, STREAMING_REQ_UNSUPPORTED = 2;

    private final Set<HttpMethod> httpMethods;
    private final String path;
    private final Method method;
    private final Object handler;
    private final List<ParameterInfo<?>> paramInfoList;
    private final ExceptionHandler exceptionHandler;
    private List<String> consumesMediaTypes;
    private List<String> producesMediaTypes;
    private int isStreamingReqSupported = STREAMING_REQ_UNKNOWN;


    /**
     * Construct a resource model with HttpMethod, method that handles httprequest, Object that contains the method.
     *
     * @param path    path associated with this model.
     * @param method  handler that handles the http request.
     * @param handler instance {@code HttpHandler}.
     * @param exceptionHandler instance {@code ExceptionHandler} to handle exceptions.
     */
    public HttpResourceModel(String path, Method method, Object handler,
                             ExceptionHandler exceptionHandler) {
        this.httpMethods = getHttpMethods(method);
        this.path = path;
        this.method = method;
        this.handler = handler;
        this.paramInfoList = makeParamInfoList(method);
        this.exceptionHandler = exceptionHandler;
        consumesMediaTypes = parseConsumesMediaTypes();
        producesMediaTypes = parseProducesMediaTypes();
    }

    private List<String> parseConsumesMediaTypes() {
        String[] consumesMediaTypeArr = method.isAnnotationPresent(Consumes.class) ?
                method.getAnnotation(Consumes.class).value() :
                handler.getClass().isAnnotationPresent(Consumes.class) ?
                        handler.getClass().getAnnotation(Consumes.class).value() :
                        ANY_MEDIA_TYPE;
        return Arrays.asList(consumesMediaTypeArr);
    }

    private List<String> parseProducesMediaTypes() {
        String[] producesMediaTypeArr = method.isAnnotationPresent(Produces.class) ?
                method.getAnnotation(Produces.class).value() :
                handler.getClass().isAnnotationPresent(Produces.class) ?
                        handler.getClass().getAnnotation(Produces.class).value() :
                        ANY_MEDIA_TYPE;
        return Arrays.asList(producesMediaTypeArr);
    }

    public boolean matchConsumeMediaType(String consumesMediaType) {
        return consumesMediaType == null
                || consumesMediaType.isEmpty()
                || consumesMediaType.equals("*/*")
                || this.consumesMediaTypes.contains("*/*")
                || this.consumesMediaTypes.contains(consumesMediaType);
    }

    public boolean matchProduceMediaType(List<String> producesMediaTypes) {
        return producesMediaTypes == null
                || producesMediaTypes.contains("*/*")
                || this.producesMediaTypes.contains("*/*")
                || this.producesMediaTypes
                .stream().filter
                        (producesMediaTypes::contains).findAny().isPresent();
    }

    /**
     * @return httpMethods.
     */
    public Set<HttpMethod> getHttpMethod() {
        return httpMethods;
    }

    /**
     * @return path associated with this model.
     */
    public String getPath() {
        return path;
    }

    /**
     * @return handler method that handles an http end-point.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * @return instance of {@code HttpHandler}.
     */
    public Object getHttpHandler() {
        return handler;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("httpMethods", httpMethods)
                .add("path", path)
                .add("method", method)
                .add("handler", handler)
                .toString();
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
     * Gathers all parameters' annotations for the given method, starting from the third parameter.
     */
    private List<ParameterInfo<?>> makeParamInfoList(Method method) {
        List<ParameterInfo<?>> paramInfoList = new ArrayList<>();

        Type[] paramTypes = method.getGenericParameterTypes();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();

        for (int i = 0; i < paramAnnotations.length; i++) {
            Annotation[] annotations = paramAnnotations[i];

            //Can have only one from @PathParam, @QueryParam, @HeaderParam or @Context.
            if (Sets.intersection(SUPPORTED_PARAM_ANNOTATIONS, ImmutableSet.of(annotations)).size() > 1) {
                throw new IllegalArgumentException(
                        String.format("Must have exactly one annotation from %s for parameter %d in method %s",
                                SUPPORTED_PARAM_ANNOTATIONS, i, method));
            }

            Annotation annotation = null;
            Type parameterType = paramTypes[i];
            Function<?, Object> converter = null;
            String defaultVal = null;
            for (Annotation annotation0 : annotations) {
                annotation = annotation0;
                Class<? extends Annotation> annotationType = annotation.annotationType();
                if (PathParam.class.isAssignableFrom(annotationType)) {
                    converter = ParamConvertUtils.createPathParamConverter(parameterType);
                } else if (QueryParam.class.isAssignableFrom(annotationType)) {
                    converter = ParamConvertUtils.createQueryParamConverter(parameterType);
                } else if (HeaderParam.class.isAssignableFrom(annotationType)) {
                    converter = ParamConvertUtils.createHeaderParamConverter(parameterType);
                } else if (DefaultValue.class.isAssignableFrom(annotationType)) {
                    defaultVal = ((DefaultValue) annotation).value();
                }
            }
            ParameterInfo<?> parameterInfo = ParameterInfo.create(parameterType, annotation, defaultVal, converter);
            paramInfoList.add(parameterInfo);
        }

        return Collections.unmodifiableList(paramInfoList);
    }

    public boolean isStreamingReqSupported() {
        if (isStreamingReqSupported == STREAMING_REQ_SUPPORTED) {
            return true;
        } else if (isStreamingReqSupported == STREAMING_REQ_UNSUPPORTED) {
            return false;
        } else if (paramInfoList.stream().filter(parameterInfo -> parameterInfo
                .getParameterType().equals(HttpStreamer.class))
                .findAny().isPresent()) {
            isStreamingReqSupported = STREAMING_REQ_SUPPORTED;
            return true;
        } else {
            isStreamingReqSupported = STREAMING_REQ_UNSUPPORTED;
            return false;
        }
    }

    public List<ParameterInfo<?>> getParamInfoList() {
        return paramInfoList;
    }

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public List<String> getConsumesMediaTypes() {
        return consumesMediaTypes;
    }

    public List<String> getProducesMediaTypes() {
        return producesMediaTypes;
    }

    /**
     * A container class to hold information about a handler method parameters.
     */
    public static final class ParameterInfo<T> {
        private final Annotation annotation;
        private final Function<T, Object> converter;
        private final Type parameterType;
        private final String defaultVal;

        private ParameterInfo(Type parameterType, Annotation annotation, String defaultVal, @Nullable Function<T,
                Object>
                converter) {
            this.parameterType = parameterType;
            this.annotation = annotation;
            this.defaultVal = defaultVal;
            this.converter = converter;
        }

        static <V> ParameterInfo<V> create(Type parameterType, Annotation annotation, String defaultVal,
                                           @Nullable Function<V, Object> converter) {
            return new ParameterInfo<>(parameterType, annotation, defaultVal, converter);
        }

        @SuppressWarnings("unchecked")
        <V extends Annotation> V getAnnotation() {
            return (V) annotation;
        }

        public Type getParameterType() {
            return parameterType;
        }

        public String getDefaultVal() {
            return defaultVal;
        }

        Object convert(T input) {
            return (converter == null) ? null : converter.apply(input);
        }
    }
}
