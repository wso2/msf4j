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
package org.wso2.msf4j.client;

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
import feign.Client;
import feign.Request;
import feign.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.analytics.common.tracing.TracingConstants;
import org.wso2.msf4j.analytics.zipkintracing.TraceableHttpClientRequest;
import org.wso2.msf4j.analytics.zipkintracing.TraceableHttpClientResponse;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *  Client for Zipkin Tracing.
 */
public class FeginZipkinTracingClient implements Client {
    private static final Logger log = LoggerFactory.getLogger(FeignTracingClient.class);
    private final Client clientDelegate;
    private final ClientRequestInterceptor requestInterceptor;
    private final ClientResponseInterceptor responseInterceptor;

    /**
     * Constructor of FeignTracingClient.
     */
    public FeginZipkinTracingClient(Client client, String instanceName) {
        this(client, instanceName, TracingConstants.DEFAULT_ZIPKIN_URL);
    }

    /**
     * Constructor of FeginZipkinTracingClient.
     *
     * @param client
     * @param instanceName
     * @param zipkinUrl URL of the receiver of DAS server.
     */
    public FeginZipkinTracingClient(Client client, String instanceName, String zipkinUrl) {
        this.clientDelegate = client;
        Brave.Builder builder = new Brave.Builder(instanceName);
        builder.spanCollector(HttpSpanCollector.create(zipkinUrl, new EmptySpanCollectorMetricsHandler()));
        Brave brave = builder.build();
        requestInterceptor = brave.clientRequestInterceptor();
        responseInterceptor = brave.clientResponseInterceptor();
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        Map<String, Collection<String>> traceHeaders = new HashMap<>();
        traceHeaders.putAll(request.headers());
        Request wrappedRequest =
                Request.create(request.method(), request.url(), traceHeaders, request.body(), request.charset());
        HttpClientRequest httpClientRequest = new TraceableHttpClientRequest(wrappedRequest);
        ClientRequestAdapter adapter = new HttpClientRequestAdapter(httpClientRequest, new DefaultSpanNameProvider());
        requestInterceptor.handle(adapter);

        Response response = clientDelegate.execute(wrappedRequest, options);

        HttpResponse httpResponse = new TraceableHttpClientResponse(response);
        ClientResponseAdapter responseAdapter = new HttpClientResponseAdapter(httpResponse);
        responseInterceptor.handle(responseAdapter);
        return response;
    }
}

