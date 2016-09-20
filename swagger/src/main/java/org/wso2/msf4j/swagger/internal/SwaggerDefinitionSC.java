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
import org.wso2.msf4j.MicroservicesRegistry;
import org.wso2.msf4j.SwaggerService;
import org.wso2.msf4j.swagger.SwaggerDefinitionService;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

/**
 * OSGi service component for SwaggerDefinitionService.
 */
@Component(name = "SwaggerDefinitionSC",
        service = SwaggerDefinitionSC.class,
        immediate = true)
public class SwaggerDefinitionSC {
    private static final String CHANNEL_ID = "CHANNEL_ID";

    @Reference(
            name = "microserviceregsitry",
            service = MicroservicesRegistry.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeRegistry")
    protected void addRegistry(MicroservicesRegistry registry, Map properties) {
        DataHolder.getInstance().addMicroserviceRegistry(properties.get(CHANNEL_ID).toString(), registry);
        Dictionary<String, String> serviceProperties = new Hashtable<>();
        serviceProperties.put(CHANNEL_ID, properties.get(CHANNEL_ID).toString());

        BundleContext bundleContext = DataHolder.getInstance().getBundleContext();
        if (bundleContext != null) {
            bundleContext.registerService(SwaggerService.class,
                    new SwaggerDefinitionService(registry), serviceProperties);
        }
    }

    protected void removeRegistry(MicroservicesRegistry registry) {
    }

    @Activate
    protected void start(final BundleContext bundleContext) {
        DataHolder.getInstance().setBundleContext(bundleContext);
        DataHolder.getInstance().getMicroserviceRegistries().forEach((registryId, registry) -> {
            Dictionary<String, String> properties = new Hashtable<>();
            properties.put(CHANNEL_ID, registryId);
            bundleContext.registerService(SwaggerService.class, new SwaggerDefinitionService(registry), properties);
        });
    }
}
