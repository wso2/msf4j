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

package org.wso2.msf4j.analytics.common.tracing;

/**
 * Class to hold tracing start event data.
 */
public class TraceEvent {

    private final String type;
    private final String traceId;
    private final String originId;
    private final long time;
    private int statusCode;
    private String httpMethod;
    private String instanceId;
    private String instanceName;
    private String parentId;
    private String url;

    public TraceEvent(String type, String traceId, String originId, long time) {
        this.type = type;
        this.traceId = traceId;
        this.originId = originId;
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getOriginId() {
        return originId;
    }

    public long getTime() {
        return time;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "TraceEvent{" +
                "type='" + type + '\'' +
                ", traceId='" + traceId + '\'' +
                ", originId='" + originId + '\'' +
                ", time=" + time +
                ", statusCode=" + statusCode +
                ", httpMethod='" + httpMethod + '\'' +
                ", instanceId='" + instanceId + '\'' +
                ", instanceName='" + instanceName + '\'' +
                ", parentId='" + parentId + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
