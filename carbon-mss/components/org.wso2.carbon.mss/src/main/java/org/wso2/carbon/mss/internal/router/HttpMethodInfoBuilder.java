/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mss.internal.router;

import io.netty.handler.codec.http.HttpRequest;
import org.wso2.carbon.mss.HttpResponder;

import java.util.List;
import java.util.Map;

/**
 * Container class that will hold HttpResourceModel and
 * request information inorder to process the request when all
 * data is ready.
 */
public class HttpMethodInfoBuilder {

    private HttpResourceModel httpResourceModel;
    private HttpRequest request;
    private HttpResponder responder;
    private Map<String, String> groupValues;
    private String contentType;
    private List<String> acceptTypes;

    public static HttpMethodInfoBuilder httpResourceModel(HttpResourceModel httpResourceModel) {
        HttpMethodInfoBuilder httpMethodInfoBuilder = new HttpMethodInfoBuilder();
        httpMethodInfoBuilder.httpResourceModel = httpResourceModel;
        return httpMethodInfoBuilder;
    }

    public HttpMethodInfoBuilder httpRequest(HttpRequest request) {
        this.request = request;
        return this;
    }

    public HttpMethodInfoBuilder httpResponder(HttpResponder responder) {
        this.responder = responder;
        return this;
    }

    public HttpMethodInfoBuilder requestInfo(Map<String, String> groupValues,
                                             String contentType,
                                             List<String> acceptTypes) {
        this.groupValues = groupValues;
        this.contentType = contentType;
        this.acceptTypes = acceptTypes;
        return this;
    }

    public HttpMethodInfo build() throws HandlerException {
        return (new HttpResourceModelProcessor(httpResourceModel))
                .buildHttpMethodInfo(request,responder,groupValues,contentType,acceptTypes);
    }

}
