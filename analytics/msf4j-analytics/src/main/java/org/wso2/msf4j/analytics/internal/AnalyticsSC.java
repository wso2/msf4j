/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.msf4j.analytics.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.analytics.httpmonitoring.HTTPMonitoringInterceptor;

/**
 *
 * Bundle Activator for msf4j-analytics bundle.
 */
@Component(
        name = "org.wso2.msf4j.analytics.httpmonitoringsc",
        immediate = true,
        property = {
                "componentName=msf4j-analytics-sc"
        }
)
public class AnalyticsSC {

    @Reference(
            name = "carbon-config",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) {
        DataHolder.getInstance().setConfigProvider(configProvider);
    }

    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        DataHolder.getInstance().setConfigProvider(null);
    }

    @Activate
    protected void activate(BundleContext bundleContext) {
        DataHolder.getInstance().setBundleContext(bundleContext);
        bundleContext.registerService(Interceptor.class, new HTTPMonitoringInterceptor(), null);
    }

    @Deactivate
    public void stop(BundleContext bundleContext) throws Exception {
        DataHolder.getInstance().setBundleContext(null);
    }
}
