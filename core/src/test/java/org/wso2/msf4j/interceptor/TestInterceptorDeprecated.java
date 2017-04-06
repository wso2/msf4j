/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.msf4j.interceptor;

import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Interceptor class using deprecated interceptor.
 */
public class TestInterceptorDeprecated implements Interceptor {

    private static AtomicInteger preCallInterceptorCalls = new AtomicInteger(0);
    private static AtomicInteger postCallInterceptorCalls = new AtomicInteger(0);

    /**
     * Reset interceptor call count.
     */
    public static void reset() {
        preCallInterceptorCalls.set(0);
        postCallInterceptorCalls.set(0);
    }

    /**
     * Get the number of interceptor pre calls.
     *
     * @return number of pre calls
     */
    public static int getPreCallInterceptorCallsCount() {
        return preCallInterceptorCalls.get();
    }

    /**
     * Get the number of interceptor post calls.
     *
     * @return number of post calls
     */
    public static int getPostCallInterceptorCallsCount() {
        return postCallInterceptorCalls.get();
    }

    @Override
    public boolean preCall(Request request, Response responder, ServiceMethodInfo serviceMethodInfo) throws Exception {
        preCallInterceptorCalls.incrementAndGet();
        PriorityDataHolder.setPriorityOrder(PriorityDataHolder.getPriorityOrder() + this.getClass().getSimpleName()
                + " - [PRE CALL]");
        return true;
    }

    @Override
    public void postCall(Request request, int status, ServiceMethodInfo serviceMethodInfo) throws Exception {
        postCallInterceptorCalls.incrementAndGet();
        PriorityDataHolder.setPriorityOrder(PriorityDataHolder.getPriorityOrder() + this.getClass().getSimpleName()
                + " - [POST CALL]");
    }
}
