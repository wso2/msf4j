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

package org.wso2.msf4j.analytics.tracing;

import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.analytics.common.tracing.TraceEvent;
import org.wso2.msf4j.analytics.common.tracing.TracingConstants;
import org.wso2.msf4j.analytics.common.tracing.TracingEventTracker;
import org.wso2.msf4j.analytics.common.tracing.TracingUtil;
import org.wso2.msf4j.interceptor.MSF4JRequestInterceptor;
import org.wso2.msf4j.interceptor.MSF4JResponseInterceptor;

import java.util.Date;

/**
 * Interceptor for tracing server side request/response flows.
 */
// TODO: Write tests and add the OSGi mode support
public class MSF4JTracingInterceptor implements MSF4JRequestInterceptor, MSF4JResponseInterceptor {

    private static final String RESPONDER_ATTRIBUTE = "responder-attribute";
    private static final String TRACE_EVENT_ATTRIBUTE = "trace-event-attribute";
    private String instanceId;
    private String instanceName;
    private String dasUrl;

    /**
     * Constructor of the MSF4JTracingInterceptor.
     *
     * @param microServiceName Name of the Microservice
     */
    public MSF4JTracingInterceptor(String microServiceName) {
        this(microServiceName, TracingConstants.DAS_RECEIVER_URL);
    }

    /**
     * Constructor of the MSF4JTracingInterceptor.
     *
     * @param microServiceName Name of the Microservice
     * @param dasUrl           URL of the receiver of DAS server
     */
    public MSF4JTracingInterceptor(String microServiceName, String dasUrl) {
        this.instanceId = TracingUtil.generateUniqueId();
        this.instanceName = microServiceName;
        this.dasUrl = dasUrl;
    }

    /**
     * Intercepts the server request flow and extract request information
     * to be published to the DAS for tracing.
     */
    @Override
    public boolean interceptRequest(Request request, Response response) throws Exception {
        long time = new Date().getTime();
        request.setProperty(RESPONDER_ATTRIBUTE, response);
        String traceOriginId = request.getHeader(TracingConstants.TRACE_ORIGIN_ID_HEADER);
        String serverTraceId;
        if (traceOriginId == null) {
            traceOriginId = TracingUtil.generateUniqueId();
            serverTraceId = traceOriginId;
        } else {
            serverTraceId = TracingUtil.generateUniqueId();
        }
        String traceParentId = request.getHeader(TracingConstants.TRACE_ID_HEADER);
        TraceEvent serverTraceEvent = new TraceEvent(
                TracingConstants.SERVER_TRACE_START,
                serverTraceId,
                traceOriginId,
                time
        );
        serverTraceEvent.setInstanceId(instanceId);
        serverTraceEvent.setInstanceName(instanceName);
        serverTraceEvent.setParentId(traceParentId);
        serverTraceEvent.setHttpMethod(request.getHttpMethod());
        serverTraceEvent.setUrl(request.getUri());
        TracingEventTracker.setTraceEvent(serverTraceEvent);
        request.setProperty(TRACE_EVENT_ATTRIBUTE, serverTraceEvent);
        TracingUtil.pushToDAS(serverTraceEvent, dasUrl);
        return true;
    }

    /**
     * Intercepts the server response flow and extract response information
     * to be published to the DAS for tracing.
     */
    @Override
    public boolean interceptResponse(Request request, Response response) throws Exception {
        long time = new Date().getTime();
        TraceEvent traceEvent = (TraceEvent) request.getProperty(TRACE_EVENT_ATTRIBUTE);
        if (traceEvent != null) {
            TraceEvent endTraceEvent = new TraceEvent(
                    TracingConstants.SERVER_TRACE_END,
                    traceEvent.getTraceId(),
                    traceEvent.getOriginId(),
                    time
            );
            Response responder = (Response) request.getProperty(RESPONDER_ATTRIBUTE);
            endTraceEvent.setStatusCode(responder.getStatusCode());
            TracingUtil.pushToDAS(endTraceEvent, dasUrl);
        }
        return true;
    }
}
