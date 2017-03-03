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
import org.wso2.msf4j.internal.MSF4JConstants;

/**
 * Interface that needs to be implemented to intercept response method calls
 * <p>
 * Priority of response interceptors will be
 * sub-resource method method level annotated interceptors -> sub-resource method class level annotated interceptors
 * -> Method level annotated response interceptors -> Class level annotated response interceptors
 * -> Global response interceptors
 * <p>
 * Upon returning false from any interceptor will cause the above flow to break and send the response immediately
 * <p>
 * Upon exception in interceptors "onResponseInterceptionError" default method in the interface will be called.
 * Override this method for custom behaviour
 */
@FunctionalInterface
public interface ResponseInterceptor {

    /**
     * Globally, resource vise or sub-resource vise intercept responses.
     * Please not that is you decided to return false ideally you should manually set the entity in response by using
     * method "setEntity". Otherwise the response will be a new Object()
     *
     * @param request  MSF4J request.
     * @param response MSF4J response.
     * @return is interception successful
     * @throws Exception on any exception
     */
    boolean interceptResponse(Request request, Response response) throws Exception;

    /**
     * This method will be called open response interception error (unhandled by end developer)
     * Override this method to manually handle exceptions when an unhandled error is thrown.
     *
     * @param request  MSF4J request.
     * @param response MSF4J Response.
     * @return should interception flow proceed?
     */
    default boolean onResponseInterceptionError(Request request, Response response, Exception e) {
        String message = "Exception while executing response interceptor " + this.getClass();
        Logger log = LoggerFactory.getLogger(this.getClass());
        log.error(message, e);
        response.setEntity(e.getMessage() + " - " + message + this.getClass())
                .setMediaType(MSF4JConstants.MEDIA_TYPE_TEXT_PLAIN)
                .setStatus(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        return false;
    }
}
