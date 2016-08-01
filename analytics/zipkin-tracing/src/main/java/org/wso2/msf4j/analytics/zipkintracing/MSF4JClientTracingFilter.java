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

package org.wso2.msf4j.analytics.zipkintracing;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ClientRequestAdapter;
import com.github.kristofa.brave.ClientRequestInterceptor;
import com.github.kristofa.brave.ClientResponseAdapter;
import com.github.kristofa.brave.ClientResponseInterceptor;
import com.github.kristofa.brave.EmptySpanCollectorMetricsHandler;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.http.HttpClientRequest;
import com.github.kristofa.brave.http.HttpClientRequestAdapter;
import com.github.kristofa.brave.http.HttpClientResponseAdapter;
import com.github.kristofa.brave.http.HttpResponse;
import com.github.kristofa.brave.http.HttpSpanCollector;

import java.io.IOException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

/**
 * Filter for tracing client side request/response flow
 */
public class MSF4JClientTracingFilter implements ClientRequestFilter, ClientResponseFilter {

    private final ClientRequestInterceptor requestInterceptor;
    private final ClientResponseInterceptor responseInterceptor;

    /**
     * Constructor of the MSF4JClientTracingFilter.
     *
     * @param clientName Name of the client.
     */
    public MSF4JClientTracingFilter(String clientName) {
        Brave.Builder builder = new Brave.Builder(clientName);
        builder.spanCollector(HttpSpanCollector.create("http://0.0.0.0:9411/", new EmptySpanCollectorMetricsHandler()));
        Brave brave = builder.build();
        requestInterceptor = brave.clientRequestInterceptor();
        responseInterceptor = brave.clientResponseInterceptor();
    }

    /**
     * Intercepts the client request flow and extract request information
     * to be published to the Zipkin server for tracing.
     */
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        HttpClientRequest httpClientRequest = new TraceableHttpClientRequest(requestContext);
        ClientRequestAdapter adapter = new HttpClientRequestAdapter(httpClientRequest, new DefaultSpanNameProvider());
        requestInterceptor.handle(adapter);
    }

    /**
     * Intercepts the client response flow and extract response information
     * to be published to the Zipkin server for tracing.
     */
    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        HttpResponse httpResponse = new TraceableHttpClientResponse(responseContext);
        ClientResponseAdapter responseAdapter = new HttpClientResponseAdapter(httpResponse);
        responseInterceptor.handle(responseAdapter);
    }
}
