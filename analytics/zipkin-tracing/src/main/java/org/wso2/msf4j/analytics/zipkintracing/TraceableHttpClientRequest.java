/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.wso2.msf4j.analytics.zipkintracing;

import com.github.kristofa.brave.http.HttpClientRequest;
import feign.Request;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Adaptor class for client side request object tracing
 */
public class TraceableHttpClientRequest implements HttpClientRequest {

    private final Request request;

    public TraceableHttpClientRequest(Request request) {
        this.request = request;
    }

    @Override
    public void addHeader(String header, String value) {
        Collection<String> existingValues = request.headers().get(header);
        if (existingValues == null) {
            existingValues = new ArrayList<>();
        }
        existingValues.add(value);
        request.headers().put(header, existingValues);
    }

    @Override
    public URI getUri() {
        return URI.create(request.url());
    }

    @Override
    public String getHttpMethod() {
        return request.method();
    }
}
