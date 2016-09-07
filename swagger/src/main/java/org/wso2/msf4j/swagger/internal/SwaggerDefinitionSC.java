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
package org.wso2.msf4j.swagger.internal;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.MicroserviceRegistry;
import org.wso2.msf4j.swagger.SwaggerDefinitionService;

/**
 * OSGi service component for SwaggerDefinitionService.
 */
@Component(name = "SwaggerDefinitionSC",
        service = SwaggerDefinitionSC.class,
        immediate = true)
public class SwaggerDefinitionSC {

    @Reference(
            name = "microserviceregsitry",
            service = MicroserviceRegistry.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeRegistry")
    protected void addRegistry(MicroserviceRegistry registry) {
        DataHolder.getInstance().setMicroserviceRegistry(registry);
    }

    protected void removeRegistry(MicroserviceRegistry registry) {
    }

    @Activate
    protected void start(final BundleContext bundleContext) {
        MicroserviceRegistry microserviceRegistry = DataHolder.getInstance().getMicroserviceRegistry();
        if (microserviceRegistry != null) {
            bundleContext.registerService(Microservice.class, new SwaggerDefinitionService(microserviceRegistry), null);
        } else {
            throw new IllegalStateException("MicroserviceRegistry instance should be registered");
        }
    }
}
