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
package org.wso2.msf4j.internal.router;

import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;

/**
 * TODO : add class level comment.
 */
public class TestInterceptor implements Interceptor {
    private volatile int numPreCalls = 0;
    private volatile int numPostCalls = 0;

    public int getNumPreCalls() {
        return numPreCalls;
    }

    public int getNumPostCalls() {
        return numPostCalls;
    }

    public void reset() {
        numPreCalls = 0;
        numPostCalls = 0;
    }

    @Override
    public boolean preCall(Request request, Response responder, ServiceMethodInfo serviceMethodInfo)
            throws Exception {
        ++numPreCalls;

        String header = request.getHeader("X-Request-Type");
        if (header != null && header.equals("Reject")) {
            responder.setStatus(javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE.getStatusCode());
            responder.send();
            return false;
        }

        if (header != null && header.equals("PreException")) {
            throw new IllegalArgumentException("PreException");
        }

        return true;
    }

    @Override
    public void postCall(Request request, int status, ServiceMethodInfo serviceMethodInfo) {
        ++numPostCalls;
        String header = request.getHeader("X-Request-Type");
        if (header != null && header.equals("PostException")) {
            throw new IllegalArgumentException("PostException");
        }
    }
}
