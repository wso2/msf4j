/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.msf4j.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;

import javax.ws.rs.core.MediaType;

/**
 * Interface that needs to be implemented to intercept request method calls
 * <p>
 * Priority of request interceptors will be
 * Global request interceptors -> Class level annotated request interceptors
 * -> Method level annotated interceptors -> Resource method execution
 * -> sub-resource method class level annotated interceptors -> sub-resource method method level annotated interceptors
 * -> sub-resource execution
 * <p>
 * Upon returning false from any interceptor will cause the above flow to break and send the response immediately
 * <p>
 * Upon exception in interceptors "onRequestInterceptionError" default method in the interface will be called.
 * Override this method for custom behaviour
 */
@FunctionalInterface
public interface RequestInterceptor {

    /**
     * Globally, resource vise or sub-resource vise intercept requests
     * Please not that is you decided to return false ideally you should manually set the entity in response by using
     * method "setEntity". Otherwise the response will be a new Object()
     *
     * @param request  MSF4J request.
     * @param response MSF4J response.
     * @return is interception successful
     * @throws Exception on any exception
     */
    boolean interceptRequest(Request request, Response response) throws Exception;

    /**
     * This method will be called open request interception error (unhandled by end developer)
     * Override this method to manually handle exceptions when an unhandled error is thrown.
     *
     * @param request  MSF4J request.
     * @param response MSF4J Response.
     * @return should interception flow proceed?
     */
    default boolean onRequestInterceptionError(Request request, Response response, Exception e) {
        String message = "Exception while executing request interceptor " + this.getClass();
        Logger log = LoggerFactory.getLogger(this.getClass());
        log.error(message, e);
        response.setEntity(message)
                .setMediaType(MediaType.TEXT_PLAIN)
                .setStatus(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        return false;
    }
}
