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

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.wso2.carbon.mss.HttpResponder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

/**
 * HttpResourceModel contains information needed to handle Http call for a given path. Used as a destination in
 * {@code PatternPathRouterWithGroups} to route URI paths to right Http end points.
 */
public final class HttpResourceModel {

    private static final Set<Class<? extends Annotation>> SUPPORTED_PARAM_ANNOTATIONS =
            ImmutableSet.of(PathParam.class, QueryParam.class, HeaderParam.class, Context.class);

    private final Set<HttpMethod> httpMethods;
    private final String path;
    private final Method method;
    private final Object handler;
    private final List<Map<Class<? extends Annotation>, ParameterInfo<?>>> paramInfoList;
    private final ExceptionHandler exceptionHandler;

    /**
     * Construct a resource model with HttpMethod, method that handles httprequest, Object that contains the method.
     *
     * @param httpMethods Set of http methods that is handled by the resource.
     * @param path        path associated with this model.
     * @param method      handler that handles the http request.
     * @param handler     instance {@code HttpHandler}.
     */
    public HttpResourceModel(Set<HttpMethod> httpMethods, String path, Method method, Object handler,
                             ExceptionHandler exceptionHandler) {
        this.httpMethods = httpMethods;
        this.path = path;
        this.method = method;
        this.handler = handler;
        this.paramInfoList = createParametersInfos(method);
        this.exceptionHandler = exceptionHandler;
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

    /**
     * Handle http Request.
     *
     * @param request     HttpRequest to be handled.
     * @param responder   HttpResponder to write the response.
     * @param groupValues Values needed for the invocation.
     */
    @SuppressWarnings("unchecked")
    public HttpMethodInfo handle(HttpRequest request, HttpResponder responder, Map<String, String> groupValues)
            throws Exception {

        //TODO: Refactor group values.
        try {
            if (httpMethods.contains(request.getMethod())) {
                //Setup args for reflection call
                Object[] args = new Object[paramInfoList.size()];

                int idx = 0;
                for (Map<Class<? extends Annotation>, ParameterInfo<?>> info : paramInfoList) {
                    if (info.containsKey(PathParam.class)) {
                        args[idx] = getPathParamValue(info, groupValues);
                    } else if (info.containsKey(QueryParam.class)) {
                        args[idx] = getQueryParamValue(info, request.getUri());
                    } else if (info.containsKey(HeaderParam.class)) {
                        args[idx] = getHeaderParamValue(info, request);
                    } else if (info.containsKey(Context.class)) {
                        args[idx] = getContextParamValue(info, request, responder);
                    }
                    idx++;
                }

                return new HttpMethodInfo(method, handler, request, responder, args, exceptionHandler);
            } else {
                //Found a matching resource but could not find the right HttpMethod so return 405
                throw new HandlerException(HttpResponseStatus.METHOD_NOT_ALLOWED, String.format
                        ("Problem accessing: %s. Reason: Method Not Allowed", request.getUri()));
            }
        } catch (Throwable e) {
            throw new HandlerException(HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    String.format("Error in executing request: %s %s", request.getMethod(),
                            request.getUri()), e);
        }
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

    @SuppressWarnings("unchecked")
    private Object getContextParamValue(Map<Class<? extends Annotation>, ParameterInfo<?>> annotations,
                                        HttpRequest request, HttpResponder responder) {
        ParameterInfo<Object> paramInfo = (ParameterInfo<Object>) annotations.get(Context.class);
        Type paramType = paramInfo.getParameterType();
        Object value = null;
        if (((Class) paramType).isAssignableFrom(HttpRequest.class)) {
            value = request;
        } else if (((Class) paramType).isAssignableFrom(HttpResponder.class)) {
            value = responder;
        }
        Preconditions.checkArgument(value != null, "Could not resolve parameter %s", paramType.getTypeName());
        return value;
    }

    @SuppressWarnings("unchecked")
    private Object getPathParamValue(Map<Class<? extends Annotation>, ParameterInfo<?>> annotations,
                                     Map<String, String> groupValues) {
        ParameterInfo<String> info = (ParameterInfo<String>) annotations.get(PathParam.class);
        PathParam pathParam = info.getAnnotation();
        String value = groupValues.get(pathParam.value());
        Preconditions.checkArgument(value != null, "Could not resolve value for parameter %s", pathParam.value());
        return info.convert(value);
    }

    @SuppressWarnings("unchecked")
    private Object getQueryParamValue(Map<Class<? extends Annotation>, ParameterInfo<?>> annotations, String uri) {
        ParameterInfo<List<String>> info = (ParameterInfo<List<String>>) annotations.get(QueryParam.class);
        QueryParam queryParam = info.getAnnotation();
        List<String> values = new QueryStringDecoder(uri).parameters().get(queryParam.value());

        return (values == null) ? info.convert(defaultValue(annotations)) : info.convert(values);
    }

    @SuppressWarnings("unchecked")
    private Object getHeaderParamValue(Map<Class<? extends Annotation>, ParameterInfo<?>> annotations,
                                       HttpRequest request) {
        ParameterInfo<List<String>> info = (ParameterInfo<List<String>>) annotations.get(HeaderParam.class);
        HeaderParam headerParam = info.getAnnotation();
        String headerName = headerParam.value();
        List<String> headers = request.headers().getAll(headerParam.value());

        return (request.headers().contains(headerName)) ?
                info.convert(headers) :
                info.convert(defaultValue(annotations));
    }

    /**
     * Returns a List of String created based on the {@link DefaultValue} if it is presented in the annotations Map.
     *
     * @return a List of String or an empty List if {@link DefaultValue} is not presented
     */
    private List<String> defaultValue(Map<Class<? extends Annotation>, ParameterInfo<?>> annotations) {
        List<String> values = ImmutableList.of();

        ParameterInfo<?> defaultInfo = annotations.get(DefaultValue.class);
        if (defaultInfo != null) {
            DefaultValue defaultValue = defaultInfo.getAnnotation();
            values = ImmutableList.of(defaultValue.value());
        }
        return values;
    }

    /**
     * Gathers all parameters' annotations for the given method, starting from the third parameter.
     */
    private List<Map<Class<? extends Annotation>, ParameterInfo<?>>> createParametersInfos(Method method) {
        ImmutableList.Builder<Map<Class<? extends Annotation>, ParameterInfo<?>>> result = ImmutableList.builder();
        Type[] parameterTypes = method.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();

        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            Map<Class<? extends Annotation>, ParameterInfo<?>> paramAnnotations = Maps.newIdentityHashMap();

            for (Annotation annotation : annotations) {
                Class<? extends Annotation> annotationType = annotation.annotationType();
                Type parameterType = parameterTypes[i];
                Function<?, Object> converter = null;
                if (PathParam.class.isAssignableFrom(annotationType)) {
                    converter = ParamConvertUtils.createPathParamConverter(parameterType);
                } else if (QueryParam.class.isAssignableFrom(annotationType)) {
                    converter = ParamConvertUtils.createQueryParamConverter(parameterType);
                } else if (HeaderParam.class.isAssignableFrom(annotationType)) {
                    converter = ParamConvertUtils.createHeaderParamConverter(parameterType);
                }
                ParameterInfo<?> parameterInfo = ParameterInfo.create(parameterType, annotation, converter);
                paramAnnotations.put(annotationType, parameterInfo);
            }

            //Can have only one from @PathParam, @QueryParam or @HeaderParam.
            if (Sets.intersection(SUPPORTED_PARAM_ANNOTATIONS, paramAnnotations.keySet()).size() > 1) {
                throw new IllegalArgumentException(
                        String.format("Must have exactly one annotation from %s for parameter %d in method %s",
                                SUPPORTED_PARAM_ANNOTATIONS, i, method));
            }

            result.add(Collections.unmodifiableMap(paramAnnotations));
        }

        return result.build();
    }

    /**
     * A container class to hold information about a handler method parameters.
     */
    private static final class ParameterInfo<T> {
        private final Annotation annotation;
        private final Function<T, Object> converter;
        private final Type parameterType;

        private ParameterInfo(Type parameterType, Annotation annotation, @Nullable Function<T, Object> converter) {
            this.parameterType = parameterType;
            this.annotation = annotation;
            this.converter = converter;
        }

        static <V> ParameterInfo<V> create(Type parameterType, Annotation annotation,
                                           @Nullable Function<V, Object> converter) {
            return new ParameterInfo<>(parameterType, annotation, converter);
        }

        @SuppressWarnings("unchecked")
        <V extends Annotation> V getAnnotation() {
            return (V) annotation;
        }

        public Type getParameterType() {
            return parameterType;
        }

        Object convert(T input) {
            return (converter == null) ? null : converter.apply(input);
        }
    }
}
