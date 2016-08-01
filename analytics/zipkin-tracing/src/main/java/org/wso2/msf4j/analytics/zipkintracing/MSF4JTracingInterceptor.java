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
import com.github.kristofa.brave.EmptySpanCollectorMetricsHandler;
import com.github.kristofa.brave.ServerRequestInterceptor;
import com.github.kristofa.brave.ServerResponseInterceptor;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.http.HttpResponse;
import com.github.kristofa.brave.http.HttpServerRequest;
import com.github.kristofa.brave.http.HttpServerRequestAdapter;
import com.github.kristofa.brave.http.HttpServerResponseAdapter;
import com.github.kristofa.brave.http.HttpSpanCollector;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;

/**
 * Interceptor for tracing server side request/response flows.
 */
public class MSF4JTracingInterceptor implements Interceptor {

    private final ServerRequestInterceptor reqInterceptor;
    private final ServerResponseInterceptor respInterceptor;
    private Response responder;

    /**
     * Constructor of the MSF4JTracingInterceptor.
     *
     * @param microServiceName Name of the Microservice
     */
    public MSF4JTracingInterceptor(String microServiceName) {
        Brave.Builder builder = new Brave.Builder(microServiceName);
        builder.spanCollector(HttpSpanCollector.create("http://0.0.0.0:9411/", new EmptySpanCollectorMetricsHandler()));
        Brave brave = builder.build();
        reqInterceptor = brave.serverRequestInterceptor();
        respInterceptor = brave.serverResponseInterceptor();
    }

    /**
     * Intercepts the server request flow and extract request information
     * to be published to the Zipkin server for tracing.
     */
    @Override
    public boolean preCall(Request request, Response responder, ServiceMethodInfo serviceMethodInfo) throws Exception {
        this.responder = responder;
        HttpServerRequest req = new TraceableHttpServerRequest(request);
        HttpServerRequestAdapter reqAdapter = new HttpServerRequestAdapter(req, new DefaultSpanNameProvider());
        reqInterceptor.handle(reqAdapter);
        return true;
    }

    /**
     * Intercepts the server response flow and extract response information
     * to be published to the Zipkin server for tracing.
     */
    @Override
    public void postCall(Request request, int status, ServiceMethodInfo serviceMethodInfo) throws Exception {
        HttpResponse httpResponse = new TraceableHttpServerResponse(responder);
        HttpServerResponseAdapter adapter = new HttpServerResponseAdapter(httpResponse);
        respInterceptor.handle(adapter);
    }
}
