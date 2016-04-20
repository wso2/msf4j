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
package org.wso2.msf4j.analytics.internal;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.kernel.startupresolver.CapabilityProvider;

/**
 * This class signals Startup Order Resolver module in kernel that this bundle provides
 * two services of type {@code Interceptor}
 */
@Component(
        name = "org.wso2.msf4j.analytics.internal.InterceptorCapabilityProvider",
        immediate = true,
        property = "capability-name=org.wso2.msf4j.Interceptor"
)
public class InterceptorCapabilityProvider implements CapabilityProvider {

    /**
     * Returns the count of {@code Interceptor} OSGi services registered by this bundle.
     * <p>
     * This bundle registers two Interceptors
     * 1. {@code HTTPMonitoringInterceptor}
     * 2. {@code MetricsInterceptor}
     *
     * @return count of {@code Interceptor} services registered by this bundle.
     */
    @Override
    public int getCount() {
        return 2;
    }
}
