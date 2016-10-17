/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.msf4j.client.codec;

import feign.Response;
import feign.codec.Decoder;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.client.exception.RestServiceException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Feign error decoder which translates REST service error response to Java exception class
 * which inherits {@link RestServiceException}
 */
public class DefaultErrorDecoder implements ErrorDecoder {
    private static final Logger log = LoggerFactory.getLogger(DefaultErrorDecoder.class);
    private final Map<String, Class<? extends RestServiceException>> errorResponseMappers;
    private Decoder decoder = new MSF4JJacksonDecoder();
    private ErrorDecoder fallbackErrorDecoder = new ErrorDecoder.Default();

    public DefaultErrorDecoder(Map<String, Class<? extends RestServiceException>> errorResponseMappers) {
        this.errorResponseMappers = errorResponseMappers;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            DefaultRestErrorResponse apiErrorResponse = (DefaultRestErrorResponse) decoder.decode(response,
                    DefaultRestErrorResponse.class);
            if (apiErrorResponse != null && errorResponseMappers.containsKey(apiErrorResponse.getErrorCode())) {
                return getExceptionSupplierFromExceptionClass(
                        errorResponseMappers.get(apiErrorResponse.getErrorCode()), apiErrorResponse.getMessage());
            }
        } catch (IOException e) {
            log.error("Error decoding error response", e);
        } catch (Exception e) {
            log.error("Error instantiating the exception mapped for the REST service error response '{}'",
                    response, e);
        }
        return fallbackErrorDecoder.decode(methodKey, response);
    }

    private RestServiceException getExceptionSupplierFromExceptionClass(Class<? extends RestServiceException> clazz,
                                                                        String message)
            throws IllegalAccessException, InvocationTargetException, InstantiationException {
        List<Object> supportedArguments = Collections.singletonList(message);
        for (Constructor<?> constructor : clazz.getConstructors()) {
            Class<?>[] parameters = constructor.getParameterTypes();
            List<Object> arguments = new ArrayList<>();
            for (Class<?> parameter : parameters) {
                supportedArguments
                        .stream()
                        .filter(argumentInstance -> parameter.isAssignableFrom(argumentInstance.getClass()))
                        .findFirst()
                        .ifPresent(arguments::add);
            }
            if (arguments.size() == parameters.length) {
                return (RestServiceException) constructor.newInstance(arguments.toArray(new Object[0]));
            }
        }
        log.warn("Could not instantiate the exception '{}'", clazz.getName());
        return null;
    }
}
