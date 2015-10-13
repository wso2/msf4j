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

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.wso2.carbon.mss.HttpResponder;
import org.wso2.carbon.mss.internal.router.beanconversion.BeanConverter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
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

    private final Set<HttpMethod> httpMethods;
    private final String path;
    private final Method method;
    private final Object handler;
    private final List<ParameterInfo<?>> paramInfoList;
    private final ExceptionHandler exceptionHandler;
    private List<String> consumesMediaTypes;
    private List<String> producesMediaTypes;

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
        this.paramInfoList = makeParamInfoList(method);
        this.exceptionHandler = exceptionHandler;
        consumesMediaTypes = parseConsumesMediaTypes();
        producesMediaTypes = parseProducesMediaTypes();
    }

    private List<String> parseConsumesMediaTypes() {
        String[] consumesMediaTypeArr = (method.isAnnotationPresent(Consumes.class)) ?
                method.getAnnotation(Consumes.class).value() :
                (handler.getClass().isAnnotationPresent(Consumes.class)) ?
                        handler.getClass().getAnnotation(Consumes.class).value() :
                        ANY_MEDIA_TYPE;
        return Arrays.asList(consumesMediaTypeArr);
    }

    private List<String> parseProducesMediaTypes() {
        String[] producesMediaTypeArr = (method.isAnnotationPresent(Produces.class)) ?
                method.getAnnotation(Produces.class).value() :
                (handler.getClass().isAnnotationPresent(Produces.class)) ?
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

    /**
     * Handle http Request.
     *
     * @param request     HttpRequest to be handled.
     * @param responder   HttpResponder to write the response.
     * @param groupValues Values needed for the invocation.
     */
    @SuppressWarnings("unchecked")
    public HttpMethodInfo handle(HttpRequest request,
                                 HttpResponder responder,
                                 Map<String, String> groupValues,
                                 String contentType,
                                 List<String> acceptTypes)
            throws Exception {

        //TODO: Refactor group values.
        try {
            if (httpMethods.contains(request.getMethod())) {
                //Setup args for reflection call
                Object[] args = new Object[paramInfoList.size()];
                String acceptType = "*/*";
                int idx = 0;
                for (ParameterInfo<?> paramInfo : paramInfoList) {
                    if (paramInfo.getAnnotation() != null) {
                        Class<? extends Annotation> annotationType = paramInfo.getAnnotation().annotationType();
                        if (PathParam.class.isAssignableFrom(annotationType)) {
                            args[idx] = getPathParamValue((ParameterInfo<String>) paramInfo, groupValues);
                        } else if (QueryParam.class.isAssignableFrom(annotationType)) {
                            args[idx] = getQueryParamValue((ParameterInfo<List<String>>) paramInfo, request.getUri());
                        } else if (HeaderParam.class.isAssignableFrom(annotationType)) {
                            args[idx] = getHeaderParamValue((ParameterInfo<List<String>>) paramInfo, request);
                        } else if (Context.class.isAssignableFrom(annotationType)) {
                            args[idx] = getContextParamValue((ParameterInfo<Object>) paramInfo, request, responder);
                        }
                    } else if (request instanceof FullHttpRequest) {
                        acceptType = (acceptTypes.contains("*/*")) ?
                                producesMediaTypes.get(0) :
                                producesMediaTypes.stream()
                                        .filter(acceptTypes::contains)
                                        .findFirst()
                                        .get();
                        String content = ((FullHttpRequest) request).content().toString(Charsets.UTF_8);
                        Type paramType = paramInfo.getParameterType();
                        args[idx] = BeanConverter.instance((contentType != null) ? contentType : "*/*")
                                .toObject(content, paramType);
                    }
                    idx++;
                }

                return new HttpMethodInfo(method, handler, request, responder, args, exceptionHandler,
                        (acceptType != null) ? acceptType : "*/*");
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
    private Object getContextParamValue(ParameterInfo<Object> paramInfo, HttpRequest request,
                                        HttpResponder responder) {
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
    private Object getPathParamValue(ParameterInfo<String> info, Map<String, String> groupValues) {
        PathParam pathParam = info.getAnnotation();
        String value = groupValues.get(pathParam.value());
        Preconditions.checkArgument(value != null, "Could not resolve value for parameter %s", pathParam.value());
        if (value == null) {
            String defaultVal = info.getDefaultVal();
            if (defaultVal != null) {
                value = defaultVal;
            }
        }
        return info.convert(value);
    }

    @SuppressWarnings("unchecked")
    private Object getQueryParamValue(ParameterInfo<List<String>> info, String uri) {
        QueryParam queryParam = info.getAnnotation();
        List<String> values = new QueryStringDecoder(uri).parameters().get(queryParam.value());
        if (values == null || values.isEmpty()) {
            String defaultVal = info.getDefaultVal();
            if (defaultVal != null) {
                values = Arrays.asList(defaultVal);
            }
        }
        return info.convert(values);
    }

    @SuppressWarnings("unchecked")
    private Object getHeaderParamValue(ParameterInfo<List<String>> info, HttpRequest request) {
        HeaderParam headerParam = info.getAnnotation();
        String headerName = headerParam.value();
        List<String> headers = request.headers().getAll(headerName);
        if (headers == null || headers.isEmpty()) {
            String defaultVal = info.getDefaultVal();
            if (defaultVal != null) {
                headers = Arrays.asList(defaultVal);
            }
        }
        return info.convert(headers);
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

    /**
     * A container class to hold information about a handler method parameters.
     */
    private static final class ParameterInfo<T> {
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
