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

import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.FileDeleteStrategy;
import org.wso2.msf4j.HttpStreamer;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.beanconversion.BeanConversionException;
import org.wso2.msf4j.beanconversion.MediaTypeConverter;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FormItem;
import org.wso2.msf4j.formparam.FormParamIterator;
import org.wso2.msf4j.formparam.exception.FormUploadException;
import org.wso2.msf4j.formparam.util.StreamUtil;
import org.wso2.msf4j.internal.beanconversion.BeanConverter;
import org.wso2.msf4j.util.BufferUtil;
import org.wso2.msf4j.util.QueryStringDecoderUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

/**
 * This class is responsible for processing the HttpResourceModel
 * when a HTTP request arrives.
 */
public class HttpResourceModelProcessor {

    private final HttpResourceModel httpResourceModel;
    private HttpStreamer httpStreamer;
    private MultivaluedMap<String, Object> formParameters = null;
    private Map<String, String> formParamContentType = new HashMap<>();
    private static Path tempRepoPath = Paths.get(System.getProperty("java.io.tmpdir"), "msf4jtemp");
    private Path tmpPathForRequest;
    // Temp File cleaning thread
    private static FileCleaningTracker fileCleaningTracker = new FileCleaningTracker();
    private static final String FILEINFO_POSTFIX = "file.info";

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
                    } else if (CookieParam.class.isAssignableFrom(annotationType)) {
                        args[idx] = getCookieParamValue((HttpResourceModel.ParameterInfo<String>) paramInfo,
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
                        args, formParameters,
                        responder);
            } else {
                return new HttpMethodInfo(httpResourceModel.getMethod(),
                        httpResourceModel.getHttpHandler(),
                        args, formParameters,
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
            throws FormUploadException, IOException {
        Type paramType = paramInfo.getParameterType();
        FormDataParam formDataParam = paramInfo.getAnnotation();
        if (getFormParameters() == null) {
            setFormParameters(extractRequestFormParams(request, paramInfo, true));
        }

        List<Object> parameter = getParameter(formDataParam.value());
        boolean isNotNull = (parameter != null);
        if (paramInfo.getConverter() != null) {
            // We need to skip the conversion for java.io.File types and handle special cases
            if (paramType instanceof ParameterizedType && isNotNull &&
                    parameter.get(0).getClass().isAssignableFrom(File.class)) {
                return parameter;
            } else if (isNotNull && parameter.get(0).getClass().isAssignableFrom(File.class)) {
                return parameter.get(0);
            } else if (MediaType.TEXT_PLAIN.equalsIgnoreCase(formParamContentType.get(formDataParam.value()))) {
                return paramInfo.convert(parameter);
            } else if (MediaType.APPLICATION_FORM_URLENCODED.equals(request.getContentType())) {
                return paramInfo.convert(parameter);
            }
            // Beans with string constructor
            return createBean(parameter, formDataParam, paramType, isNotNull);
        }
        // We only support InputStream for a single file. Therefore only get first element from the list
        if (paramType == InputStream.class && isNotNull && parameter.get(0).getClass().isAssignableFrom(File.class)) {
            return new FileInputStream((File) parameter.get(0));
        } else if (paramType == FileInfo.class) {
            List<Object> fileInfo = getParameter(formDataParam.value() + FILEINFO_POSTFIX);
            return fileInfo == null ? null : fileInfo.get(0);
        }
        // These are beans without having string constructor. Convert using existing BeanConverter
        return createBean(parameter, formDataParam, paramType, isNotNull);
    }

    /**
     * Extract the form items in the request.
     *
     * @param request     Request which need to be processed
     * @param paramInfo   of the method
     * @param addFileInfo if FileInfo object needed to be added to params. In a case of InputStream this should be true
     * @return MultivaluedMap of form items
     * @throws IOException if error occurs while processing the multipart/form-data request
     */
    private MultivaluedMap<String, Object> extractRequestFormParams(Request request,
                                                                    HttpResourceModel.ParameterInfo paramInfo,
                                                                    boolean addFileInfo) throws IOException {
        MultivaluedMap<String, Object> parameters = new MultivaluedHashMap<>();
        if (MediaType.MULTIPART_FORM_DATA.equals(request.getContentType())) {
            FormParamIterator formParamIterator = new FormParamIterator(request);
            while (formParamIterator.hasNext()) {
                FormItem item = formParamIterator.next();

                String cType = item.getContentType();
                if (cType != null && cType.contains(";")) {
                    cType = cType.split(";")[0];
                }
                if (cType == null) {
                    cType = MediaType.TEXT_PLAIN;
                }
                boolean isFile = item.getHeaders().getHeader("content-disposition").contains("filename") ||
                        MediaType.APPLICATION_OCTET_STREAM.equals(item.getHeaders().getHeader("content-type"));
                formParamContentType.putIfAbsent(item.getFieldName(), cType);

                List<Object> existingValues = parameters.get(item.getFieldName());
                if (existingValues == null) {
                    parameters.put(item.getFieldName(),
                            isFile ? new ArrayList<>(Collections.singletonList(createAndTrackTempFile(item))) :
                                    new ArrayList<>(Collections.singletonList(StreamUtil.asString(item.openStream()))));
                } else {
                    existingValues.add(isFile ? createAndTrackTempFile(item) : StreamUtil.asString(item.openStream()));
                }

                if (addFileInfo && isFile) {
                    //Create FileInfo bean to handle InputStream
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setFileName(item.getName());
                    fileInfo.setContentType(item.getContentType());
                    parameters.putSingle(item.getFieldName() + FILEINFO_POSTFIX, fileInfo);
                }
            }
        } else if (MediaType.APPLICATION_FORM_URLENCODED.equals(request.getContentType())) {
            ByteBuffer fullContent = BufferUtil.merge(request.getFullMessageBody());
            String bodyStr = BeanConverter
                    .getConverter((request.getContentType() != null) ? request.getContentType() : MediaType.WILDCARD)
                    .convertToObject(fullContent, paramInfo.getParameterType()).toString();
            QueryStringDecoderUtil queryStringDecoderUtil = new QueryStringDecoderUtil(bodyStr, false);
            queryStringDecoderUtil.parameters().entrySet().
                    forEach(entry -> parameters.put(entry.getKey(), new ArrayList<>(entry.getValue())));
        }
        return parameters;
    }

    private Object createBean(List<Object> parameter, FormDataParam formDataParam, Type paramType, boolean isNotNull) {
        if (isNotNull) {
            MediaTypeConverter converter = BeanConverter.getConverter(formParamContentType.get(formDataParam.value()));
            ByteBuffer value = ByteBuffer.wrap(parameter.get(0).toString().getBytes(Charset.defaultCharset()));
            return converter.convertToObject(value, paramType);
        }
        throw new BeanConversionException("Content cannot be null");
    }

    private File createAndTrackTempFile(FormItem item) throws IOException {
        if (tmpPathForRequest == null) {
            if (Files.notExists(tempRepoPath)) {
                Files.createDirectory(tempRepoPath);
            }
            tmpPathForRequest = Files.createTempDirectory(tempRepoPath, "tmp");
        }
        Path path = Paths.get(tmpPathForRequest.toString(), item.getName());
        File file = path.toFile();
        StreamUtil.copy(item.openStream(), new FileOutputStream(file), true);
        fileCleaningTracker.track(file, file);
        fileCleaningTracker.track(tmpPathForRequest.toFile(), file, FileDeleteStrategy.FORCE);
        return file;
    }

    private Object getFormParamValue(HttpResourceModel.ParameterInfo<List<Object>> paramInfo, Request request)
            throws FormUploadException, IOException {
        FormParam formParam = paramInfo.getAnnotation();
        if (getFormParameters() == null) {
            MultivaluedMap<String, Object> parameters = new MultivaluedHashMap<>();
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
                                        Response responder) throws FormUploadException, IOException {
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
        } else if (((Class) paramType).isAssignableFrom(MultivaluedMap.class)) {
            MultivaluedMap<String, Object> listMultivaluedMap = new MultivaluedHashMap<>();
            if (MediaType.MULTIPART_FORM_DATA.equals(request.getContentType())) {
                listMultivaluedMap = extractRequestFormParams(request, paramInfo, false);
            } else if (MediaType.APPLICATION_FORM_URLENCODED.equals(request.getContentType())) {
                ByteBuffer fullContent = BufferUtil.merge(request.getFullMessageBody());
                String bodyStr = BeanConverter.getConverter(
                        (request.getContentType() != null) ? request.getContentType() : MediaType.WILDCARD)
                        .convertToObject(fullContent, paramInfo.getParameterType()).toString();
                QueryStringDecoderUtil queryStringDecoderUtil = new QueryStringDecoderUtil(bodyStr, false);
                MultivaluedMap<String, Object> finalListMultivaluedMap = listMultivaluedMap;
                queryStringDecoderUtil.parameters().entrySet().
                        forEach(entry -> finalListMultivaluedMap.put(entry.getKey(), new ArrayList(entry.getValue())));
            }
            value = listMultivaluedMap;
        }
        Objects.requireNonNull(value, String.format("Could not resolve parameter %s", paramType.getTypeName()));
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
        Objects.requireNonNull(value, String.format("Could not resolve value for parameter %s", pathParam.value()));
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

    @SuppressWarnings("unchecked")
    private Object getCookieParamValue(HttpResourceModel.ParameterInfo<String> info, Request request) {
        CookieParam cookieParam = info.getAnnotation();
        String cookieName = cookieParam.value();
        String cookieHeader = request.getHeader("Cookie");
        if (cookieHeader != null) {
            String cookieValue = Arrays.stream(cookieHeader.split(";"))
                    .filter(cookie -> cookie.startsWith(cookieName + "="))
                    .findFirst()
                    .map(cookie -> cookie.substring((cookieName + "=").length()))
                    .orElseGet(info::getDefaultVal);
            return info.convert(cookieValue);
        }
        return null;
    }

    /**
     * @param key parameter name.
     * @return parameter value of the given key.
     */
    private List<Object> getParameter(String key) {
        return formParameters.get(key);
    }

    /**
     * @return Map of request formParameters
     */
    public Map<String, List<Object>> getFormParameters() {
        return formParameters;
    }

    /**
     * Set the request formParameters.
     *
     * @param parameters request formParameters
     */
    public void setFormParameters(MultivaluedMap<String, Object> parameters) {
        this.formParameters = parameters;
    }
}
