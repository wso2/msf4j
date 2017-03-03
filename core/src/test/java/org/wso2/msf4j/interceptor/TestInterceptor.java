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
package org.wso2.msf4j.interceptor;

import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Interceptor used in test.
 */
public class TestInterceptor implements Interceptor {
    private volatile AtomicInteger numPreCalls = new AtomicInteger(0);
    private volatile AtomicInteger numPostCalls = new AtomicInteger(0);

    public int getNumPreCalls() {
        return numPreCalls.get();
    }

    public int getNumPostCalls() {
        return numPostCalls.get();
    }

    public void reset() {
        numPreCalls.set(0);
        numPostCalls.set(0);
    }

    @Override
    public boolean preCall(Request request, Response responder, ServiceMethodInfo serviceMethodInfo)
            throws Exception {
        numPreCalls.incrementAndGet();

        String header = request.getHeader("X-Request-Type");
        if (header != null && header.equals("Reject")) {
            responder.setStatus(javax.ws.rs.core.Response.Status.NOT_ACCEPTABLE.getStatusCode());
            return false;
        }

        if (header != null && header.equals("PreException")) {
            throw new IllegalArgumentException("PreException");
        }

        return true;
    }

    @Override
    public void postCall(Request request, int status, ServiceMethodInfo serviceMethodInfo) {
        numPostCalls.incrementAndGet();
        String header = request.getHeader("X-Request-Type");
        if (header != null && header.equals("PostException")) {
            throw new IllegalArgumentException("PostException");
        }
    }
}
