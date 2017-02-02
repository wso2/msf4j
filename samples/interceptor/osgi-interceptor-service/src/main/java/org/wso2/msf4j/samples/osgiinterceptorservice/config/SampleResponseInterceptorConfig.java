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
package org.wso2.msf4j.samples.osgiinterceptorservice.config;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.wso2.msf4j.interceptor.OSGiResponseInterceptorConfig;
import org.wso2.msf4j.samples.interceptor.common.HTTPResponseLogger;
import org.wso2.msf4j.samples.interceptor.common.LogTextResponseInterceptor;
import org.wso2.msf4j.samples.interceptor.common.PropertyGetResponseInterceptor;

/**
 * OSGi response interceptor configuration.
 */
@Component(
        name = "org.wso2.msf4j.samples.osgiinterceptorservice.config.SampleResponseInterceptorConfig",
        service = OSGiResponseInterceptorConfig.class,
        immediate = true
)
public class SampleResponseInterceptorConfig extends OSGiResponseInterceptorConfig {

    @Activate
    public void activate(BundleContext bundleContext) {
        addResponseInterceptors(new HTTPResponseLogger(), new LogTextResponseInterceptor());
        addGlobalResponseInterceptors(new LogTextResponseInterceptor(), new PropertyGetResponseInterceptor());
    }
}
