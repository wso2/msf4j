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
package org.wso2.msf4j.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.kernel.transports.CarbonTransport;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.Microservice;

/**
 * OSGi service component for MicroServicesServer.
 */
@Component(
        name = "org.wso2.msf4j.internal.MicroServicesServerSC",
        immediate = true,
        service = RequiredCapabilityListener.class,
        property = {
                "capability-name=org.wso2.msf4j.Microservice, org.wso2.msf4j.Interceptor",
                "component-key=wso2-microservices-server"
        }
)
@SuppressWarnings("unused")
public class MicroservicesServerSC implements RequiredCapabilityListener {
    private static final Logger log = LoggerFactory.getLogger(MicroservicesServerSC.class);
    private final MicroservicesRegistry microservicesRegistry = MicroservicesRegistry.getInstance();

    @Activate
    protected void start(final BundleContext bundleContext) {
    }

    @Reference(
            name = "microservice",
            service = Microservice.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeHttpService"
    )
    protected void addHttpService(Microservice httpService) {
        microservicesRegistry.addHttpService(httpService);
    }

    protected void removeHttpService(Microservice httpService) {
        microservicesRegistry.removeHttpService(httpService);
    }

    @Reference(
            name = "carbon-transport",
            service = CarbonTransport.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeCarbonTransport"
    )
    protected void addCarbonTransport(CarbonTransport carbonTransport) {
    }

    protected void removeCarbonTransport(CarbonTransport carbonTransport) {
    }

    @Reference(
            name = "interceptor",
            service = Interceptor.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeInterceptor"
    )
    protected void addInterceptor(Interceptor interceptor) {
        microservicesRegistry.addInterceptor(interceptor);
    }

    protected void removeInterceptor(Interceptor interceptor) {
        microservicesRegistry.removeInterceptor(interceptor);
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        DataHolder.getInstance().getBundleContext().registerService(MicroservicesServerSC.class, this, null);
        log.info("All microservices are available");
    }
}
