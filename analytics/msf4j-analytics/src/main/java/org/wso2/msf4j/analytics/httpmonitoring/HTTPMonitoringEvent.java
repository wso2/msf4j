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
package org.wso2.msf4j.analytics.httpmonitoring;

/**
 * Monitoring Event.
 */
public class HTTPMonitoringEvent {

    private long timestamp;
    private String serverName;
    private String serverAddress;
    private long startNanoTime;
    private String serviceClass;
    private String serviceName;
    private String serviceContext;
    private String serviceMethod;
    private String requestUri;
    private String httpMethod;
    private String contentType;
    private Integer responseHttpStatusCode;
    private String referrer;
    private long responseTime;
    private long requestSizeBytes;
    private String activityId;
    private String parentRequest;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public long getStartNanoTime() {
        return startNanoTime;
    }

    public void setStartNanoTime(long startNanoTime) {
        this.startNanoTime = startNanoTime;
    }

    public String getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(String serviceClass) {
        this.serviceClass = serviceClass;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceContext() {
        return serviceContext;
    }

    public void setServiceContext(String serviceContext) {
        this.serviceContext = serviceContext;
    }

    public String getServiceMethod() {
        return serviceMethod;
    }

    public void setServiceMethod(String serviceMethod) {
        this.serviceMethod = serviceMethod;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Integer getResponseHttpStatusCode() {
        return responseHttpStatusCode;
    }

    public void setResponseHttpStatusCode(Integer responseHttpStatusCode) {
        this.responseHttpStatusCode = responseHttpStatusCode;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public long getRequestSizeBytes() {
        return requestSizeBytes;
    }

    public void setRequestSizeBytes(long requestSizeBytes) {
        this.requestSizeBytes = requestSizeBytes;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getParentRequest() {
        return parentRequest;
    }

    public void setParentRequest(String parentRequest) {
        this.parentRequest = parentRequest;
    }

}
