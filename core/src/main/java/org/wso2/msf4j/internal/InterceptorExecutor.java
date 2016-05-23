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

package org.wso2.msf4j.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;
import org.wso2.msf4j.internal.router.HttpResourceModel;

import java.util.List;

/**
 * Execute Interceptors. preCall and postCall
 */
public class InterceptorExecutor {

    private static final Logger log = LoggerFactory.getLogger(InterceptorExecutor.class);

    private Request request;
    private Response response;
    private List<Interceptor> interceptors;
    private ServiceMethodInfo serviceMethodInfo;

    public InterceptorExecutor(HttpResourceModel httpResourceModel,
                               Request request,
                               Response response,
                               List<Interceptor> interceptors) {
        this.request = request;
        this.response = response;
        this.interceptors = interceptors;
        serviceMethodInfo = new ServiceMethodInfo(httpResourceModel.getMethod().getDeclaringClass().getName(),
                httpResourceModel.getMethod());
    }

    /**
     * Execute preCalls in all interceptors and return true
     * if and only if all preCalls return true.
     *
     * @return true if all preCalls return true
     */
    public boolean execPreCalls() throws InterceptorException {
        try {
            for (Interceptor interceptor : interceptors) {
                if (!interceptor.preCall(request, response, serviceMethodInfo)) {
                    // Terminate further request processing if preCall returns false.
                    return false;
                }
            }
        } catch (Exception e) {
            throw new InterceptorException("Exception while executing preCalls", e);
        }
        return true;
    }

    /**
     * Execute postCalls of all interceptors.
     *
     * @param status status that was returned to the client
     */
    public void execPostCalls(int status) throws InterceptorException {
        for (Interceptor interceptor : interceptors) {
            try {
                interceptor.postCall(request, status, serviceMethodInfo);
            } catch (Exception e) {
                log.error("Exception while executing a postCall", e);
            }
        }
    }
}
