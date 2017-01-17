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

import org.wso2.msf4j.HttpStreamer;
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.util.Utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import javax.annotation.Nullable;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

/**
 * HttpResourceModel contains information needed to handle Http call for a given path. Used as a destination in
 * {@code PatternPathRouter} to route URI paths to right Http end points.
 */
public final class HttpResourceModel {

    private static final Set<Class<? extends Annotation>> SUPPORTED_PARAM_ANNOTATIONS;

    static {
        Set<Class<? extends Annotation>> supportedAnnotation =
                new HashSet<>();
        supportedAnnotation.add(PathParam.class);
        supportedAnnotation.add(QueryParam.class);
        supportedAnnotation.add(HeaderParam.class);
        supportedAnnotation.add(Context.class);
        supportedAnnotation.add(FormParam.class);
        supportedAnnotation.add(FormDataParam.class);
        supportedAnnotation.add(CookieParam.class);

        SUPPORTED_PARAM_ANNOTATIONS = Collections.unmodifiableSet(supportedAnnotation);
    }

    private static final String[] ANY_MEDIA_TYPE = new String[]{"*/*"};
    private static final int STREAMING_REQ_UNKNOWN = 0, STREAMING_REQ_SUPPORTED = 1, STREAMING_REQ_UNSUPPORTED = 2;

    private final Set<String> httpMethods;
    private final String path;
    private final Method method;
    private final Object handler;
    private final List<ParameterInfo<?>> paramInfoList;
    private List<String> consumesMediaTypes;
    private List<String> producesMediaTypes;
    private int isStreamingReqSupported = STREAMING_REQ_UNKNOWN;
    private Map<SubresourceKey, HttpResourceModel> subResources = new HashMap<>();
    private boolean isSubResourceLocator;
    private boolean isSubResourceScanned;
    private HttpResourceModel parent;

    /**
     * If this is a subresource locator, get the parent of this Model.
     * @return HttpResourceModel parent model.
     */
    public HttpResourceModel getParent() {
        return parent;
    }

    /**
     * If this is a subresource locator, set the parent Model.
     * @param parent HttpResourceModel of this.
     */
    public void setParent(HttpResourceModel parent) {
        this.parent = parent;
        consumesMediaTypes = parseConsumesMediaTypes();
        producesMediaTypes = parseProducesMediaTypes();
    }

    /**
     * Get the sub resource locators of this model.
     * @return Map of subresource locators of this.
     */
    public Map<SubresourceKey, HttpResourceModel> getSubResources() {
        return subResources;
    }

    /**
     * Set the sub resource locators of this model.
     * @param subResources Map of sub resource locators.
     */
    public void setSubResources(Map<SubresourceKey, HttpResourceModel> subResources) {
        this.subResources = subResources;
    }

    public void addSubResources(SubresourceKey subresourceKey, HttpResourceModel httpResourceModel) {
        subResources.put(subresourceKey, httpResourceModel);
    }

    /**
     * Set if this model is already scanned for sub resource locators.
     * @param subResourceScanned boolean indicate whether this already scanned for sub resource locators.
     */
    public void setSubResourceScanned(boolean subResourceScanned) {
        isSubResourceScanned = subResourceScanned;
    }

    /**
     * Check if this model is already scanned for sub resource locators.
     * @return boolean whether this model already scanned for sub resources.
     */
    public boolean isSubResourceScanned() {
        return isSubResourceScanned;
    }

    /**
     * Construct a resource model with HttpMethod, method that handles httprequest, Object that contains the method.
     *
     * @param path             path associated with this model.
     * @param method           handler that handles the http request.
     * @param handler          instance {@code HttpHandler}.
     * @param isSubResourceLocator indicate if this is a subresource locator method
     */
    public HttpResourceModel(String path, Method method, Object handler, boolean isSubResourceLocator) {
        this.httpMethods = getHttpMethods(method);
        this.path = path;
        this.method = method;
        this.handler = handler;
        this.isSubResourceLocator = isSubResourceLocator;
        this.paramInfoList = makeParamInfoList(method);
        consumesMediaTypes = parseConsumesMediaTypes();
        producesMediaTypes = parseProducesMediaTypes();
    }

    private List<String> parseConsumesMediaTypes() {
        String[] consumesMediaTypeArr =
                method.isAnnotationPresent(Consumes.class) ? method.getAnnotation(Consumes.class).value() :
                handler.getClass().isAnnotationPresent(Consumes.class) ?
                handler.getClass().getAnnotation(Consumes.class).value() :
                parent == null ? ANY_MEDIA_TYPE : new String[] {};
        if (parent != null && consumesMediaTypeArr.length == 0) {
            HttpResourceModel tmpParent = parent;
            while (tmpParent.getConsumesMediaTypes().size() != 0 && tmpParent.getParent() != null) {
                tmpParent = parent.getParent();
            }
            consumesMediaTypeArr = tmpParent.getMethod().isAnnotationPresent(Consumes.class) ?
                                   tmpParent.getMethod().getAnnotation(Consumes.class).value() :
                                   handler.getClass().isAnnotationPresent(Consumes.class) ?
                                   handler.getClass().getAnnotation(Consumes.class).value() : ANY_MEDIA_TYPE;
        }
        return Arrays.asList(consumesMediaTypeArr);
    }

    private List<String> parseProducesMediaTypes() {
        String[] producesMediaTypeArr =
                method.isAnnotationPresent(Produces.class) ? method.getAnnotation(Produces.class).value() :
                handler.getClass().isAnnotationPresent(Produces.class) ?
                handler.getClass().getAnnotation(Produces.class).value() :
                parent == null ? ANY_MEDIA_TYPE : new String[] {};
        if (parent != null && producesMediaTypeArr.length == 0) {
            HttpResourceModel tmpParent = parent;
            while (tmpParent.getProducesMediaTypes().size() != 0 && tmpParent.getParent() != null) {
                tmpParent = parent.getParent();
            }
            producesMediaTypeArr = tmpParent.getMethod().isAnnotationPresent(Produces.class) ?
                                   tmpParent.getMethod().getAnnotation(Produces.class).value() :
                                   handler.getClass().isAnnotationPresent(Produces.class) ?
                                   handler.getClass().getAnnotation(Produces.class).value() : ANY_MEDIA_TYPE;
        }
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
    public Set<String> getHttpMethod() {
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
     * Indicate this method as subresource locator method.
     * @param subResourceLocator boolean value to set method
     */
    public void setSubResourceLocator(boolean subResourceLocator) {
        isSubResourceLocator = subResourceLocator;
    }

    /**
     * Return true if this method is a subresource locator method.
     *
     * @return boolean true if this method is a subresource locator method.
     */
    public boolean isSubResourceLocator() {
        return isSubResourceLocator;
    }

    @Override
    public String toString() {
        return Utils.toString(this, new String[] { "httpMethods", "path", "method", "handler" });
    }

    /**
     * Fetches the HttpMethod from annotations and returns String representation of HttpMethod.
     * Return emptyString if not present.
     *
     * @param method Method handling the http request.
     * @return String representation of HttpMethod from annotations or emptyString as a default.
     */
    private Set<String> getHttpMethods(Method method) {
        Set<String> httpMethods = new HashSet();
        boolean isSubResourceLocator = true;
        if (method.isAnnotationPresent(GET.class)) {
            httpMethods.add(HttpMethod.GET);
            isSubResourceLocator = false;
        }
        if (method.isAnnotationPresent(PUT.class)) {
            httpMethods.add(HttpMethod.PUT);
            isSubResourceLocator = false;
        }
        if (method.isAnnotationPresent(POST.class)) {
            httpMethods.add(HttpMethod.POST);
            isSubResourceLocator = false;
        }
        if (method.isAnnotationPresent(DELETE.class)) {
            httpMethods.add(HttpMethod.DELETE);
            isSubResourceLocator = false;
        }
        if (method.isAnnotationPresent(HEAD.class)) {
            httpMethods.add(HttpMethod.HEAD);
            isSubResourceLocator = false;
        }
        if (method.isAnnotationPresent(OPTIONS.class)) {
            httpMethods.add(HttpMethod.OPTIONS);
            isSubResourceLocator = false;
        }
        // If this is a sub resource locator need to add all the method designator
        if (isSubResourceLocator) {
            httpMethods.add(HttpMethod.GET);
            httpMethods.add(HttpMethod.POST);
            httpMethods.add(HttpMethod.PUT);
            httpMethods.add(HttpMethod.DELETE);
            httpMethods.add(HttpMethod.HEAD);
            httpMethods.add(HttpMethod.OPTIONS);
        }
        return Collections.unmodifiableSet(httpMethods);
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
            if (Utils.getIntersection(SUPPORTED_PARAM_ANNOTATIONS,
                                      Collections.unmodifiableSet(new HashSet(Arrays.asList(annotations)))) > 1) {
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
                } else if (FormParam.class.isAssignableFrom(annotationType)) {
                    converter = ParamConvertUtils.createFormParamConverter(parameterType);
                } else if (FormDataParam.class.isAssignableFrom(annotationType)) {
                    converter = ParamConvertUtils.createFormDataParamConverter(parameterType);
                } else if (HeaderParam.class.isAssignableFrom(annotationType)) {
                    converter = ParamConvertUtils.createHeaderParamConverter(parameterType);
                } else if (CookieParam.class.isAssignableFrom(annotationType)) {
                    converter = ParamConvertUtils.createCookieParamConverter(parameterType);
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

    public List<String> getConsumesMediaTypes() {
        return consumesMediaTypes;
    }

    public List<String> getProducesMediaTypes() {
        return producesMediaTypes;
    }

    /**
     * A container class to hold information about a handler method parameters.
     * @param <T> type of parameter
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

        public Function<T, Object> getConverter() {
            return converter;
        }
    }
}
