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

import feign.Client;
import feign.Request;
import feign.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.analytics.common.tracing.TraceEvent;
import org.wso2.msf4j.analytics.common.tracing.TracingConstants;
import org.wso2.msf4j.analytics.common.tracing.TracingEventTracker;
import org.wso2.msf4j.analytics.common.tracing.TracingUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Supports tracing capabilities with WSO2DAS
 */
class FeignTracingClient implements Client {
    private static final Logger log = LoggerFactory.getLogger(FeignTracingClient.class);
    private final String instanceId;
    private final String instanceName;
    private final String dasUrl;
    private final Client clientDelegate;

    /**
     * Constructor of FeignTracingClient.
     */
    public FeignTracingClient(Client client, String instanceName) {
        this(client, instanceName, TracingConstants.DAS_RECEIVER_URL);
    }

    /**
     * Constructor of FeignTracingClient with custom SSL configuration
     *
     * @param dasReceiverUrl URL of the receiver of DAS server
     */
    public FeignTracingClient(Client client, String instanceName, String dasReceiverUrl) {
        this.instanceName = instanceName;
        this.dasUrl = dasReceiverUrl;
        this.instanceId = TracingUtil.generateUniqueId();
        this.clientDelegate = client;
    }

    @Override
    public Response execute(Request request, Request.Options options) throws IOException {
        TraceEvent clientStartTraceEvent = generateClientStartTraceEvent(request);
        Request traceableRequest = tracePreRequest(request, clientStartTraceEvent);
        Response response = clientDelegate.execute(traceableRequest, options);
        tracePostRequest(response, clientStartTraceEvent);
        return response;
    }

    private TraceEvent generateClientStartTraceEvent(Request request) {
        long time = new Date().getTime();
        String clientTraceId;
        String traceOriginId;
        String traceParentId = null;
        TraceEvent parentEvent = TracingEventTracker.getTraceEvent();
        if (parentEvent == null) {
            traceOriginId = TracingUtil.generateUniqueId();
            clientTraceId = traceOriginId;
        } else {
            traceOriginId = parentEvent.getOriginId();
            clientTraceId = TracingUtil.generateUniqueId();
            traceParentId = parentEvent.getTraceId();
        }
        TraceEvent clientStartTraceEvent = new TraceEvent(
                TracingConstants.CLIENT_TRACE_START,
                clientTraceId,
                traceOriginId,
                time
        );
        clientStartTraceEvent.setInstanceId(instanceId);
        clientStartTraceEvent.setInstanceName(instanceName);
        clientStartTraceEvent.setParentId(traceParentId);
        clientStartTraceEvent.setHttpMethod(request.method());
        clientStartTraceEvent.setUrl(request.url());
        if (log.isDebugEnabled()) {
            log.debug("clientStartTraceEvent: " + ModelUtils.toString(clientStartTraceEvent));
        }
        return clientStartTraceEvent;
    }

    private Request tracePreRequest(Request request, TraceEvent traceEvent) {
        // set tracing headers to HTTP request
        Map<String, Collection<String>> traceHeaders = new HashMap<>();
        traceHeaders.putAll(request.headers());
        traceHeaders.put(TracingConstants.TRACE_ID_HEADER, Collections.singletonList(traceEvent.getTraceId()));
        traceHeaders.put(TracingConstants.TRACE_ORIGIN_ID_HEADER, Collections.singletonList(traceEvent.getOriginId()));

        // publish event to DAS
        TracingUtil.pushToDAS(traceEvent, dasUrl);
        return Request.create(request.method(), request.url(), traceHeaders, request.body(), request.charset());
    }

    /**
     * Publish client request information to DAS after response is received
     */
    private void tracePostRequest(Response response, TraceEvent traceEvent) {
        long time = new Date().getTime();
        if (traceEvent != null) {
            TraceEvent clientEndTraceEvent = new TraceEvent(
                    TracingConstants.CLIENT_TRACE_END,
                    traceEvent.getTraceId(),
                    traceEvent.getOriginId(),
                    time
            );
            clientEndTraceEvent.setStatusCode(response.status());
            if (log.isDebugEnabled()) {
                log.debug("ClientEndTraceEvent: " + ModelUtils.toString(clientEndTraceEvent));
            }
            TracingUtil.pushToDAS(clientEndTraceEvent, dasUrl);
        }
    }
}
