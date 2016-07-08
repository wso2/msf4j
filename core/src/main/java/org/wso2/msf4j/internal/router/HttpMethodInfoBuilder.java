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

import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;

import java.util.Map;

/**
 * Builder for HttpMethodInfo that will hold HttpResourceModel and
 * request information in order to process the request when all
 * data is ready.
 */
public class HttpMethodInfoBuilder {

    private HttpResourceModel httpResourceModel;
    private Request request;
    private Response responder;
    private Map<String, String> groupValues;
    private HttpMethodInfo httpMethodInfo;

    /**
     * Set the associated HttpResourceModel object.
     *
     * @param httpResourceModel resource model for the request
     * @return HttpMethodInfoBuilder object
     */
    public HttpMethodInfoBuilder httpResourceModel(HttpResourceModel httpResourceModel) {
        this.httpResourceModel = httpResourceModel;
        return this;
    }

    /**
     * Set the associated Request object.
     *
     * @param request Request object
     * @return HttpMethodInfoBuilder object
     */
    public HttpMethodInfoBuilder httpRequest(Request request) {
        this.request = request;
        return this;
    }

    /**
     * Set the associated Response object.
     *
     * @param responder Response object
     * @return HttpMethodInfoBuilder object
     */
    public HttpMethodInfoBuilder httpResponder(Response responder) {
        this.responder = responder;
        return this;
    }

    /**
     * Set information of the request that were processed
     * when searching for the route.
     *
     * @param groupValues request info to be set
     * @return HttpMethodInfoBuilder object
     */
    public HttpMethodInfoBuilder requestInfo(Map<String, String> groupValues) {
        this.groupValues = groupValues;
        return this;
    }

    /**
     * Build HttpMethodInfo instance.
     *
     * @return HttpMethodInfo object
     * @throws HandlerException if error occurs while executing the request
     */
    public HttpMethodInfo build() throws HandlerException {
        if (httpMethodInfo == null) {
            httpMethodInfo = (new HttpResourceModelProcessor(httpResourceModel))
                    .buildHttpMethodInfo(request, responder, groupValues);
        }
        return httpMethodInfo;
    }

    public HttpResourceModel getHttpResourceModel() {
        return httpResourceModel;
    }

    public Response getResponder() {
        return responder;
    }

    public Request getRequest() {
        return request;
    }
}
