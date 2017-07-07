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
package org.wso2.msf4j.deployer.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.deployment.engine.Deployer;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.kernel.startupresolver.StartupServiceUtils;
import org.wso2.msf4j.MicroservicesRegistry;

import java.util.Map;

/**
 * Service component for Microservices Deployer.
 */
@Component(
        name = "org.wso2.msf4j.deployer.internal.MSF4JDeployerSC",
        immediate = true,
        property = {
                "componentName=wso2-microservices-deployer"
        }
)
public class MSF4JDeployerSC implements RequiredCapabilityListener {

    private static final Logger log = LoggerFactory.getLogger(MSF4JDeployerSC.class);
    private static final String CHANNEL_ID = "CHANNEL_ID";

    @Activate
    protected void start(BundleContext bundleContext) {
    }

    @Deactivate
    protected void stop() {
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        FrameworkUtil.getBundle(MSF4JDeployerSC.class).getBundleContext()
                     .registerService(Deployer.class, new MicroservicesDeployer(), null);
        if (log.isDebugEnabled()) {
            log.debug("MicroservicesDeployer service is available");
        }
    }

    /**
     * To deploy micro services, we need to have at least one micro service registry in the system.
     * Since microservice registry is added when microservice server is active, added the reference to wait till
     * microservices server is active.
     * Otherwise if deployer registered before at least one registry is added, Deployer will start to deploy services
     * and will fail deployment.
     */
    @Reference(
            name = "microservices-regitry",
            service = MicroservicesRegistry.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeMicroservicesRegistry"
    )
    protected void addMicroservicesRegitry(MicroservicesRegistry registry, Map properties) {
        if (log.isDebugEnabled()) {
            log.debug("MicroservicesRegistry get registered successfully.");
        }
        DataHolder.getInstance().addMicroserviceRegistry(properties.get(CHANNEL_ID).toString(), registry);
        StartupServiceUtils.updateServiceCache("wso2-microservices-deployer", MicroservicesRegistry.class);
    }

    protected void removeMicroservicesRegistry(MicroservicesRegistry microservicesRegistry, Map properties) {
        if (log.isDebugEnabled()) {
            log.debug("MicroservicesRegistry get unregistered successfully.");
        }
        DataHolder.getInstance().getMicroserviceRegistries().remove(properties.get(CHANNEL_ID).toString());
    }
}
