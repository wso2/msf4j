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
 * OSGi request interceptor registration component.
 */
public abstract class OSGiRequestInterceptorConfig {

    private final List<MSF4JRequestInterceptor> requestInterceptorList = new ArrayList<>();
    private final List<MSF4JRequestInterceptor> globalRequestInterceptorList = new ArrayList<>();

    /**
     * Add non global request interceptor.
     * Order in which the interceptors are added are the execution priority order if the interceptors
     *
     * @param requestInterceptors {@link MSF4JRequestInterceptor}
     */
    protected final void addRequestInterceptors(MSF4JRequestInterceptor... requestInterceptors) {
        requestInterceptorList.addAll(Arrays.asList(requestInterceptors));
    }

    /**
     * Add global request interceptors.
     * Order in which the interceptors are added are the execution priority order if the interceptors
     *
     * @param globalRequestInterceptors {@link MSF4JRequestInterceptor}
     */
    protected final void addGlobalRequestInterceptors(MSF4JRequestInterceptor... globalRequestInterceptors) {
        globalRequestInterceptorList.addAll(Arrays.asList(globalRequestInterceptors));
    }

    /**
     * Get non-global interceptors added.
     *
     * @return {@link MSF4JRequestInterceptor}
     */
    public final MSF4JRequestInterceptor[] getRequestInterceptorArray() {
        return requestInterceptorList
                .toArray(new MSF4JRequestInterceptor[requestInterceptorList.size()]);
    }

    /**
     * Get global interceptors added.
     *
     * @return {@link MSF4JRequestInterceptor}
     */
    public final MSF4JRequestInterceptor[] getGlobalRequestInterceptorArray() {
        return globalRequestInterceptorList
                .toArray(new MSF4JRequestInterceptor[globalRequestInterceptorList.size()]);
    }
}
