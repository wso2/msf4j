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

package org.wso2.msf4j.internal.router;

import org.wso2.msf4j.util.HttpUtil;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import javax.ws.rs.core.Response;

/**
 * Creating Http Response for Exception messages.
 */
public class HandlerException extends Exception {

    private final Response.Status failureStatus;
    private final String message;

    public HandlerException(Response.Status failureStatus, String message) {
        super(message);
        this.failureStatus = failureStatus;
        this.message = message;
    }

    public HandlerException(Response.Status failureStatus, String message, Throwable cause) {
        super(message, cause);
        this.failureStatus = failureStatus;
        this.message = message;
    }

    public HttpCarbonMessage getFailureResponse() {
        return HttpUtil.createTextResponse(failureStatus.getStatusCode(), message);
    }

    public Response.Status getFailureStatus() {
        return failureStatus;
    }
}
