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

import com.google.common.base.Preconditions;
import io.netty.handler.codec.http.QueryStringDecoder;
import org.wso2.msf4j.HttpStreamer;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.internal.router.beanconversion.BeanConverter;
import org.wso2.msf4j.util.BufferUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

/**
 * This class is responsible for processing the HttpResourceModel
 * when a HTTP request arrives.
 */
public class HttpResourceModelProcessor {

    private final HttpResourceModel httpResourceModel;
    private HttpStreamer httpStreamer;

    public HttpResourceModelProcessor(HttpResourceModel httpResourceModel) {
        this.httpResourceModel = httpResourceModel;
    }

    /**
     * Handle http Request.
     *
     * @param request     HttpRequest to be handled.
     * @param responder   HttpResponder to write the response.
     * @param groupValues Values needed for the invocation.
     * @return HttpMethodInfo
     * @throws HandlerException If an error occurs
     */
    @SuppressWarnings("unchecked")
    public HttpMethodInfo buildHttpMethodInfo(Request request,
                                              Response responder,
                                              Map<String, String> groupValues)
            throws HandlerException {
        try {
            //Setup args for reflection call
            List<HttpResourceModel.ParameterInfo<?>> paramInfoList = httpResourceModel.getParamInfoList();
            Object[] args = new Object[paramInfoList.size()];
            int idx = 0;
            for (HttpResourceModel.ParameterInfo<?> paramInfo : paramInfoList) {
                if (paramInfo.getAnnotation() != null) {
                    Class<? extends Annotation> annotationType = paramInfo.getAnnotation().annotationType();
                    if (PathParam.class.isAssignableFrom(annotationType)) {
                        args[idx] = getPathParamValue((HttpResourceModel.ParameterInfo<String>) paramInfo,
                                groupValues);
                    } else if (QueryParam.class.isAssignableFrom(annotationType)) {
                        args[idx] = getQueryParamValue((HttpResourceModel.ParameterInfo<List<String>>) paramInfo,
                                request.getUri());
                    } else if (HeaderParam.class.isAssignableFrom(annotationType)) {
                        args[idx] = getHeaderParamValue((HttpResourceModel.ParameterInfo<List<String>>) paramInfo,
                                request);
                    } else if (Context.class.isAssignableFrom(annotationType)) {
                        args[idx] = getContextParamValue((HttpResourceModel.ParameterInfo<Object>) paramInfo,
                                request, responder);
                    }
                } else {
                    // If an annotation is not present the parameter is considered a
                    // request body data parameter
                    ByteBuffer fullContent = BufferUtil.merge(request.getFullMessageBody());
                    Type paramType = paramInfo.getParameterType();
                    args[idx] = BeanConverter.instance(
                            (request.getContentType() != null)
                                    ? request.getContentType()
                                    : MediaType.WILDCARD
                    ).convertToObject(fullContent, paramType);
                }
                idx++;
            }

            if (httpStreamer == null) {
                return new HttpMethodInfo(httpResourceModel.getMethod(),
                        httpResourceModel.getHttpHandler(),
                        args,
                        responder);
            } else {
                return new HttpMethodInfo(httpResourceModel.getMethod(),
                        httpResourceModel.getHttpHandler(),
                        args,
                        responder,
                        httpStreamer);
            }
        } catch (Throwable e) {
            throw new HandlerException(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR,
                    String.format("Error in executing request: %s %s", request.getHttpMethod(),
                            request.getUri()), e);
        }
    }


    @SuppressWarnings("unchecked")
    private Object getContextParamValue(HttpResourceModel.ParameterInfo<Object> paramInfo, Request request,
                                        Response responder) {
        Type paramType = paramInfo.getParameterType();
        Object value = null;
        if (((Class) paramType).isAssignableFrom(Request.class)) {
            value = request;
        } else if (((Class) paramType).isAssignableFrom(Response.class)) {
            value = responder;
        } else if (((Class) paramType).isAssignableFrom(HttpStreamer.class)) {
            if (httpStreamer == null) {
                httpStreamer = new HttpStreamer();
            }
            value = httpStreamer;
        }
        Preconditions.checkArgument(value != null, "Could not resolve parameter %s", paramType.getTypeName());
        return value;
    }

    @SuppressWarnings("unchecked")
    private Object getPathParamValue(HttpResourceModel.ParameterInfo<String> info, Map<String, String> groupValues) {
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
    private Object getQueryParamValue(HttpResourceModel.ParameterInfo<List<String>> info, String uri) {
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
    private Object getHeaderParamValue(HttpResourceModel.ParameterInfo<List<String>> info, Request request) {
        HeaderParam headerParam = info.getAnnotation();
        String headerName = headerParam.value();
        List<String> headers = Collections.singletonList(request.getHeader(headerName));
        if (headers == null || headers.isEmpty()) {
            String defaultVal = info.getDefaultVal();
            if (defaultVal != null) {
                headers = Arrays.asList(defaultVal);
            }
        }
        return info.convert(headers);
    }
}
