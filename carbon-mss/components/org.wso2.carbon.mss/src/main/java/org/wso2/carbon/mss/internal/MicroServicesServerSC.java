/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.mss.internal;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.mss.Microservice;
import org.wso2.carbon.transport.http.netty.listener.CarbonNettyServerInitializer;

import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

/**
 * OSGi service component for MicroServicesServer.
 */
@Component(
        name = "org.wso2.carbon.mss.internal.MicroServicesServerSC",
        immediate = true
)
@SuppressWarnings("unused")
public class MicroServicesServerSC {
    public static final String CHANNEL_ID_KEY = "channel.id";
    private static final Logger LOG = LoggerFactory.getLogger(MicroServicesServerSC.class);
    private final MicroservicesRegistry microservicesRegistry = MicroservicesRegistry.getInstance();

    private BundleContext bundleContext;
    private int jaxRsServiceCount;

    @Activate
    protected void start(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        try {
            countJaxrsServices();

            new Thread(() -> {
                while (true) {
                    if (microservicesRegistry.getServiceCount() == jaxRsServiceCount) {
                        LOG.info("Starting micro services server ++++++++++++ ...");

                        // Create an OSGi services (HTTP/HTTPS) & register it with the relevant CHANNEL_ID_KEY

                        Hashtable<String, String> httpInitParams = new Hashtable<>();
                        httpInitParams.put(CHANNEL_ID_KEY, "jaxrs-http");
                        bundleContext.registerService(CarbonNettyServerInitializer.class,
                                new MSSNettyServerInitializer(MicroservicesRegistry.getInstance()), httpInitParams);

                        /*Hashtable<String, String> httpsInitParams = new Hashtable<>();
                        httpsInitParams.put(CHANNEL_ID_KEY, "netty-jaxrs-https");
                        bundleContext.
                                registerService(CarbonNettyServerInitializer.class,
                                        new MSSNettyServerInitializer(MicroservicesRegistry.getInstance()),
                                        httpsInitParams);*/

                        LOG.info("Micro services server started");
                        break;
                    } else {
                        try {
                            TimeUnit.MILLISECONDS.sleep(10);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }).start();
        } catch (Throwable e) {
            LOG.error("Could not start MicroServicesServerSC", e);
        }
    }


    private void countJaxrsServices() {
        Bundle[] bundles = bundleContext.getBundles();
        for (Bundle bundle : bundles) {
            String jaxRsServices = bundle.getHeaders().get("JAXRS-Services");
            if (jaxRsServices != null) {
                jaxRsServiceCount += Integer.parseInt(jaxRsServices);
            }
        }
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
}
