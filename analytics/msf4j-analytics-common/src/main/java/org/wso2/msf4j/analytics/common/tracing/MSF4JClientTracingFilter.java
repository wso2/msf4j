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

package org.wso2.msf4j.analytics.common.tracing;

import java.io.IOException;
import java.util.Date;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

/**
 * Filter for tracing client side request/response flow.
 */
// TODO: Write tests and add the OSGi mode support
public class MSF4JClientTracingFilter implements ClientRequestFilter, ClientResponseFilter {

    private static final String TRACE_EVENT_ATTRIBUTE = "trace-event-attribute";
    private TraceEvent parentEvent;
    private String instanceId;
    private String instanceName;
    private String dasUrl;

    /**
     * Constructor of the MSF4JClientTracingFilter.
     *
     * @param clientName Name of the client
     */
    public MSF4JClientTracingFilter(String clientName) {
        this(clientName, TracingConstants.DAS_RECEIVER_URL);
    }

    /**
     * Constructor of the MSF4JClientTracingFilter.
     *
     * @param clientName Name of the client
     * @param dasUrl     URL of the receiver of DAS server
     */
    public MSF4JClientTracingFilter(String clientName, String dasUrl) {
        this(clientName, null, dasUrl);
    }

    /**
     * Constructor of the MSF4JClientTracingFilter.
     *
     * @param clientName  Name of the client
     * @param parentEvent TraceEvent of the caller
     * @param dasUrl      URL of the receiver of DAS server
     */
    public MSF4JClientTracingFilter(String clientName, TraceEvent parentEvent, String dasUrl) {
        this.instanceName = clientName;
        this.instanceId = TracingUtil.generateUniqueId();
        this.dasUrl = dasUrl;
        if (parentEvent != null) {
            this.parentEvent = parentEvent;
        } else {
            this.parentEvent = TracingEventTracker.getTraceEvent();
        }
    }

    /**
     * Intercepts the client request flow and extract request information
     * to be published to the DAS for tracing.
     */
    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        long time = new Date().getTime();
        String clientTraceId;
        String traceOriginId;
        String traceParentId = null;
        if (this.parentEvent == null) {
            traceOriginId = TracingUtil.generateUniqueId();
            clientTraceId = traceOriginId;
        } else {
            traceOriginId = parentEvent.getOriginId();
            clientTraceId = TracingUtil.generateUniqueId();
            traceParentId = parentEvent.getTraceId();
        }
        TraceEvent clientTraceEvent = new TraceEvent(
                TracingConstants.CLIENT_TRACE_START,
                clientTraceId,
                traceOriginId,
                time
        );
        clientTraceEvent.setInstanceId(instanceId);
        clientTraceEvent.setInstanceName(instanceName);
        clientTraceEvent.setParentId(traceParentId);
        clientTraceEvent.setHttpMethod(requestContext.getMethod());
        clientTraceEvent.setUrl(requestContext.getUri().toString());
        requestContext.setProperty(TRACE_EVENT_ATTRIBUTE, clientTraceEvent);
        requestContext.getHeaders().putSingle(TracingConstants.TRACE_ID_HEADER, clientTraceId);
        requestContext.getHeaders().putSingle(TracingConstants.TRACE_ORIGIN_ID_HEADER, traceOriginId);
        TracingUtil.pushToDAS(clientTraceEvent, dasUrl);
    }

    /**
     * Intercepts the client response flow and extract response information
     * to be published to the DAS server for tracing.
     */
    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
        long time = new Date().getTime();
        TraceEvent traceEvent = (TraceEvent) requestContext.getProperty(TRACE_EVENT_ATTRIBUTE);
        if (traceEvent != null) {
            TraceEvent endTraceEvent = new TraceEvent(
                    TracingConstants.CLIENT_TRACE_END,
                    traceEvent.getTraceId(),
                    traceEvent.getOriginId(),
                    time
            );
            endTraceEvent.setStatusCode(responseContext.getStatus());
            TracingUtil.pushToDAS(endTraceEvent, dasUrl);
        }
    }
}
