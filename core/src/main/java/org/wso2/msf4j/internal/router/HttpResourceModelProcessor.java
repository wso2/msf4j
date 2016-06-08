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
import org.apache.commons.io.FileCleaningTracker;
import org.wso2.msf4j.HttpStreamer;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FormItem;
import org.wso2.msf4j.formparam.FormParamIterator;
import org.wso2.msf4j.formparam.exception.FileUploadException;
import org.wso2.msf4j.formparam.util.StreamUtil;
import org.wso2.msf4j.internal.beanconversion.BeanConverter;
import org.wso2.msf4j.util.BufferUtil;
import org.wso2.msf4j.util.QueryStringDecoderUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.FormParam;
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
    private Map<String, List<Object>> formParameters = null;
    private Map<String, String> formParamContentType = new HashMap<>();
    private static Path tempRepoPath = Paths.get(System.getProperty("java.io.tmpdir"), "msf4jtemp");
    // Temp File cleaning thread
    private static FileCleaningTracker fileCleaningTracker = new FileCleaningTracker();

    public HttpResourceModelProcessor(HttpResourceModel httpResourceModel) {
        this.httpResourceModel = httpResourceModel;
    }

    /**
     * Build an HttpMethodInfo object to dispatch the request.
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
                    } else if (FormParam.class.isAssignableFrom(annotationType)) {
                        args[idx] = getFormParamValue((HttpResourceModel.ParameterInfo<List<Object>>) paramInfo,
                                                      request);
                    } else if (FormDataParam.class.isAssignableFrom(annotationType)) {
                        args[idx] = getFormDataParamValue((HttpResourceModel.ParameterInfo<List<Object>>) paramInfo,
                                                          request);
                    } else {
                        createObject(request, args, idx, paramInfo);
                    }
                } else {
                    // If an annotation is not present the parameter is considered a
                    // request body data parameter
                    createObject(request, args, idx, paramInfo);
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

    private void createObject(Request request, Object[] args, int idx, HttpResourceModel.ParameterInfo<?> paramInfo) {
        ByteBuffer fullContent = BufferUtil.merge(request.getFullMessageBody());
        Type paramType = paramInfo.getParameterType();
        args[idx] =
                BeanConverter.getConverter((request.getContentType() != null) ? request.getContentType() :
                        MediaType.WILDCARD).convertToObject(fullContent, paramType);
    }

    private Object getFormDataParamValue(HttpResourceModel.ParameterInfo<List<Object>> paramInfo, Request request)
            throws FileUploadException, IOException {
        if (Files.notExists(tempRepoPath)) {
            Files.createDirectory(tempRepoPath);
        }

        Type paramType = paramInfo.getParameterType();
        FormDataParam formDataParam = paramInfo.getAnnotation();
        if (getFormParameters() == null) {
            FormParamIterator formParamIterator = new FormParamIterator(request);
            Map<String, List<Object>> parameters = new HashMap<>();
            while (formParamIterator.hasNext()) {
                FormItem item = formParamIterator.next();

                String cType = item.getContentType();
                if (cType != null && cType.contains(";")) {
                    cType = cType.split(";")[0];
                }
                boolean isFile = item.getHeaders().getHeader("content-disposition").contains("filename") ||
                                 "application/octet-stream".equals(item.getHeaders().getHeader("content-type"));
                formParamContentType.putIfAbsent(item.getFieldName(), cType);

                List<Object> existingValues = parameters.get(item.getFieldName());
                if (existingValues == null) {
                    parameters.put(item.getFieldName(),
                                   isFile ? new ArrayList<>(Collections.singletonList(createAndTrackTempFile(item))) :
                                   new ArrayList<>(Collections.singletonList(StreamUtil.asString(item.openStream()))));
                } else {
                    existingValues.add(isFile ? createAndTrackTempFile(item) : StreamUtil.asString(item.openStream()));
                }
            }
            setFormParameters(parameters);
        }

        List<Object> parameter = getParameter(formDataParam.value());
        if (paramInfo.getConverter() != null) {
            // We need to skip the conversion for java.io.File types
            if (paramType instanceof ParameterizedType) {
                return parameter;
            } else if (parameter.get(0).getClass().isAssignableFrom(File.class)) {
                return parameter.get(0);
            }
            return paramInfo.convert(parameter);
        }
        // These are beans. Convert using existing BeanConverter
        return BeanConverter.getConverter(formParamContentType.get(formDataParam.value())).convertToObject(
                ByteBuffer.wrap(parameter.get(0).toString().getBytes(Charset.defaultCharset())), paramType);
    }

    private File createAndTrackTempFile(FormItem item) throws IOException {
        Path path = Paths.get(tempRepoPath.toString(), item.getName());
        File file = path.toFile();
        StreamUtil.copy(item.openStream(), new FileOutputStream(file), true);
        fileCleaningTracker.track(file, file);
        return file;
    }

    private Object getFormParamValue(HttpResourceModel.ParameterInfo<List<Object>> paramInfo, Request request)
            throws FileUploadException, IOException {
        FormParam formParam = paramInfo.getAnnotation();
        if (getFormParameters() == null) {
            Map<String, List<Object>> parameters = new HashMap<>();
            if (MediaType.MULTIPART_FORM_DATA.equals(request.getContentType())) {
                FormParamIterator formParamIterator = new FormParamIterator(request);
                while (formParamIterator.hasNext()) {
                    FormItem item = formParamIterator.next();
                    List<Object> existingValues = parameters.get(item.getFieldName());
                    if (existingValues == null) {
                        parameters.put(item.getFieldName(), new ArrayList<>(
                                Collections.singletonList(StreamUtil.asString(item.openStream()))));
                    } else {
                        existingValues.add(StreamUtil.asString(item.openStream()));
                    }
                }
            } else if (MediaType.APPLICATION_FORM_URLENCODED.equals(request.getContentType())) {
                ByteBuffer fullContent = BufferUtil.merge(request.getFullMessageBody());
                String bodyStr = BeanConverter.getConverter(
                        (request.getContentType() != null) ? request.getContentType() : MediaType.WILDCARD)
                                              .convertToObject(fullContent, paramInfo.getParameterType()).toString();
                QueryStringDecoderUtil queryStringDecoderUtil = new QueryStringDecoderUtil(bodyStr, false);
                queryStringDecoderUtil.parameters().entrySet().
                        forEach(entry -> parameters.put(entry.getKey(), new ArrayList<>(entry.getValue())));
            }
            setFormParameters(parameters);
        }

        List<Object> paramValue = getParameter(formParam.value());
        if (paramValue == null) {
            String defaultVal = paramInfo.getDefaultVal();
            if (defaultVal != null) {
                paramValue = Collections.singletonList(defaultVal);
            }
        }
        return paramInfo.convert(paramValue);
    }

    @SuppressWarnings("unchecked")
    private Object getContextParamValue(HttpResourceModel.ParameterInfo<Object> paramInfo, Request request,
                                        Response responder) throws FileUploadException, IOException {
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
        } else if (((Class) paramType).isAssignableFrom(FormParamIterator.class)) {
            value = new FormParamIterator(request);
        }
        Preconditions.checkArgument(value != null, "Could not resolve parameter %s", paramType.getTypeName());
        return value;
    }

    @SuppressWarnings("unchecked")
    private Object getPathParamValue(HttpResourceModel.ParameterInfo<String> info, Map<String, String> groupValues) {
        PathParam pathParam = info.getAnnotation();
        String value = groupValues.get(pathParam.value());
        if (value == null) {
            String defaultVal = info.getDefaultVal();
            if (defaultVal != null) {
                value = defaultVal;
            }
        }
        Preconditions.checkArgument(value != null, "Could not resolve value for parameter %s", pathParam.value());
        return info.convert(value);
    }

    @SuppressWarnings("unchecked")
    private Object getQueryParamValue(HttpResourceModel.ParameterInfo<List<String>> info, String uri) {
        QueryParam queryParam = info.getAnnotation();
        List<String> values = new QueryStringDecoderUtil(uri).parameters().get(queryParam.value());
        if (values == null || values.isEmpty()) {
            String defaultVal = info.getDefaultVal();
            if (defaultVal != null) {
                values = Collections.singletonList(defaultVal);
            }
        }
        return info.convert(values);
    }

    @SuppressWarnings("unchecked")
    private Object getHeaderParamValue(HttpResourceModel.ParameterInfo<List<String>> info, Request request) {
        HeaderParam headerParam = info.getAnnotation();
        String headerName = headerParam.value();
        String header = request.getHeader(headerName);
        if (header == null || header.isEmpty()) {
            String defaultVal = info.getDefaultVal();
            if (defaultVal != null) {
                header = defaultVal;
            }
        }
        return info.convert(Collections.singletonList(header));
    }

    /**
     * @return parameter value of the given key.
     * @param key parameter name
     */
    public List<Object> getParameter(String key) {
        return formParameters.get(key);
    }

    /**
     *
     * @return Map of request formParameters
     */
    public Map<String, List<Object>> getFormParameters() {
        return formParameters;
    }

    /**
     * Set the request formParameters
     *
     * @param parameters request formParameters
     */
    public void setFormParameters(Map<String, List<Object>> parameters) {
        this.formParameters = parameters;
    }
}
