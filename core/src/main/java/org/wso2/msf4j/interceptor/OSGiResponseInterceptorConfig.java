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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * OSGi response interceptor registration component.
 */
public class OSGiResponseInterceptorConfig {

    private final List<MSF4JResponseInterceptor> responseInterceptorList = new ArrayList<>();
    private final List<MSF4JResponseInterceptor> globalResponseInterceptorList = new ArrayList<>();

    /**
     * Add non global response interceptor.
     * Order in which the interceptors are added are the execution priority order if the interceptors
     *
     * @param responseInterceptors {@link MSF4JResponseInterceptor}
     */
    protected final void addResponseInterceptors(MSF4JResponseInterceptor... responseInterceptors) {
        responseInterceptorList.addAll(Arrays.asList(responseInterceptors));
    }

    /**
     * Add global request interceptors.
     * Order in which the interceptors are added are the execution priority order if the interceptors
     *
     * @param globalResponseInterceptors {@link MSF4JResponseInterceptor}
     */
    protected final void addGlobalResponseInterceptors(MSF4JResponseInterceptor... globalResponseInterceptors) {
        globalResponseInterceptorList.addAll(Arrays.asList(globalResponseInterceptors));
    }

    /**
     * Get non-global interceptors added.
     *
     * @return {@link MSF4JRequestInterceptor}
     */
    public final MSF4JResponseInterceptor[] getResponseInterceptorArray() {
        return responseInterceptorList
                .toArray(new MSF4JResponseInterceptor[responseInterceptorList.size()]);
    }

    /**
     * Get global interceptors added.
     *
     * @return {@link MSF4JResponseInterceptor}
     */
    public final MSF4JResponseInterceptor[] getGlobalResponseInterceptorArray() {
        return globalResponseInterceptorList
                .toArray(new MSF4JResponseInterceptor[globalResponseInterceptorList.size()]);
    }
}
