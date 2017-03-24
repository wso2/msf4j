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
import org.wso2.msf4j.MicroservicesRegistry;
import org.wso2.msf4j.SessionManager;
import org.wso2.msf4j.SwaggerService;
import org.wso2.msf4j.exception.OSGiDeclarativeServiceException;
import org.wso2.msf4j.interceptor.OSGiInterceptorConfig;
import org.wso2.msf4j.interceptor.RequestInterceptor;
import org.wso2.msf4j.interceptor.ResponseInterceptor;
import org.wso2.msf4j.util.RuntimeAnnotations;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.ws.rs.Path;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * OSGi service component for MicroServicesServer.
 */
@Component(
        name = "org.wso2.msf4j.internal.MicroServicesServerSC",
        immediate = true,
        property = {
                "componentName=wso2-microservices-server"
        }
)
@SuppressWarnings("unused")
public class MicroservicesServerSC implements RequiredCapabilityListener {
    private static final Logger log = LoggerFactory.getLogger(MicroservicesServerSC.class);

    @Activate
    protected void start(final BundleContext bundleContext) {

    }

    @Reference(
            name = "microservice",
            service = Microservice.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeService"
    )
    protected void addService(Microservice service, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        Object contextPath = properties.get("contextPath");
        if (contextPath != null) {
            Map<String, Object> valuesMap = new HashMap<>();
            valuesMap.put("value", contextPath);
            RuntimeAnnotations.putAnnotation(service.getClass(), Path.class, valuesMap);
        }
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                DataHolder.getInstance().getMicroservicesRegistries();
        if (channelId != null) {
            MicroservicesRegistryImpl microservicesRegistry = microservicesRegistries.get(channelId.toString());
            if (microservicesRegistry == null) {
                throw new RuntimeException("Couldn't found the registry for channel ID " + channelId);
            }
            if (contextPath == null) {
                microservicesRegistry.addService(service);
            } else {
                microservicesRegistry.addService(contextPath.toString(), service);
            }
        } else {
            if (contextPath == null) {
                microservicesRegistries.values().forEach(registry -> registry.addService(service));
            } else {
                microservicesRegistries.values()
                        .forEach(registry -> registry.addService(contextPath.toString(), service));
            }
        }
    }

    protected void removeService(Microservice service, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                DataHolder.getInstance().getMicroservicesRegistries();
        if (channelId != null) {
            MicroservicesRegistryImpl microservicesRegistry = microservicesRegistries.get(channelId.toString());
            if (microservicesRegistry != null) {
                String serviceKey = service.getClass().getAnnotation(Path.class).value();
                microservicesRegistry.removeService(serviceKey);
            }
        }
    }

    @Reference(
            name = "swaggerservice",
            service = SwaggerService.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeSwaggerService"
    )
    protected void addSwaggerService(SwaggerService service, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                DataHolder.getInstance().getMicroservicesRegistries();
        if (channelId != null) {
            MicroservicesRegistryImpl microservicesRegistry = microservicesRegistries.get(channelId.toString());
            if (microservicesRegistry == null) {
                throw new RuntimeException("Couldn't found the registry for channel ID " + channelId);
            }
            microservicesRegistry.addService(service);
        } else {
            microservicesRegistries.values().forEach(registry -> registry.addService(service));
        }
    }

    protected void removeSwaggerService(SwaggerService service, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                DataHolder.getInstance().getMicroservicesRegistries();
        if (channelId != null) {
            MicroservicesRegistryImpl microservicesRegistry = microservicesRegistries.get(channelId.toString());
            if (microservicesRegistry != null) {
                String serviceKey = service.getClass().getAnnotation(Path.class).value();
                microservicesRegistry.removeService(serviceKey);
            }
        }
    }

    @Reference(
            name = "carbon-transport",
            service = CarbonTransport.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeCarbonTransport"
    )
    protected void addCarbonTransport(CarbonTransport carbonTransport) {
        MicroservicesRegistryImpl microservicesRegistry = new MicroservicesRegistryImpl();
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                DataHolder.getInstance().getMicroservicesRegistries();
        Dictionary<String, String> properties = new Hashtable<>();
        properties.put(MSF4JConstants.CHANNEL_ID, carbonTransport.getId());
        microservicesRegistries.put(carbonTransport.getId(), microservicesRegistry);
        DataHolder.getInstance().getBundleContext()
                .registerService(MicroservicesRegistry.class, microservicesRegistry, properties);
    }

    protected void removeCarbonTransport(CarbonTransport carbonTransport) {
        DataHolder.getInstance().getMicroservicesRegistries().remove(carbonTransport.getId());
    }

    @Reference(
            name = "interceptor-config",
            service = OSGiInterceptorConfig.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeInterceptorConfig"
    )
    protected void addInterceptorConfig(OSGiInterceptorConfig interceptorConfig, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                DataHolder.getInstance().getMicroservicesRegistries();
        if (channelId != null) {
            MicroservicesRegistryImpl microServicesRegistry = microservicesRegistries.get(channelId.toString());
            if (microServicesRegistry == null) {
                throw new OSGiDeclarativeServiceException("Couldn't found the registry for channel ID " +
                        channelId);
            }
            microServicesRegistry.addGlobalRequestInterceptor(interceptorConfig.getGlobalRequestInterceptorArray());
            microServicesRegistry.addGlobalResponseInterceptor(interceptorConfig.getGlobalResponseInterceptorArray());
        } else {
            microservicesRegistries.values().forEach(registry -> {
                registry.addGlobalRequestInterceptor(interceptorConfig.getGlobalRequestInterceptorArray());
                registry.addGlobalResponseInterceptor(interceptorConfig.getGlobalResponseInterceptorArray());
            });
        }
    }

    protected void removeInterceptorConfig(OSGiInterceptorConfig interceptorConfig, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        if (channelId != null) {
            MicroservicesRegistryImpl microServicesRegistry = DataHolder.getInstance().getMicroservicesRegistries()
                    .get(channelId.toString());
            for (RequestInterceptor requestInterceptor : interceptorConfig.getGlobalRequestInterceptorArray()) {
                microServicesRegistry.removeGlobalRequestInterceptor(requestInterceptor);
            }
            for (ResponseInterceptor responseInterceptor : interceptorConfig.getGlobalResponseInterceptorArray()) {
                microServicesRegistry.removeGlobalResponseInterceptor(responseInterceptor);
            }
        }
    }

    /**
     * Add interceptor.
     * Please note that the order of this interceptor execution is unpredictable
     *
     * @param interceptor interceptor to be added
     * @param properties  map of properties of the interceptor to be added
     * @deprecated
     */
    @Reference(
            name = "interceptor",
            service = Interceptor.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeInterceptor"
    )
    protected void addInterceptor(Interceptor interceptor, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                DataHolder.getInstance().getMicroservicesRegistries();
        if (channelId != null) {
            MicroservicesRegistryImpl microservicesRegistry = microservicesRegistries.get(channelId.toString());
            if (microservicesRegistry == null) {
                throw new RuntimeException("Couldn't found the registry for channel ID " + channelId);
            }
            microservicesRegistry.addGlobalRequestInterceptor(interceptor);
            microservicesRegistry.addGlobalResponseInterceptor(interceptor);
        } else {
            microservicesRegistries.values().forEach(registry -> {
                registry.removeGlobalRequestInterceptor(interceptor);
                registry.removeGlobalResponseInterceptor(interceptor);
            });
        }
    }

    /**
     * Remove interceptor.
     *
     * @param interceptor interceptor to be removed
     * @param properties  map of interceptor component properties
     * @deprecated
     */
    protected void removeInterceptor(Interceptor interceptor, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                DataHolder.getInstance().getMicroservicesRegistries();
        if (channelId != null) {
            microservicesRegistries.get(channelId.toString()).removeGlobalRequestInterceptor(interceptor);
            microservicesRegistries.get(channelId.toString()).removeGlobalResponseInterceptor(interceptor);
        }
    }

    @Reference(
            name = "exception-mapper",
            service = ExceptionMapper.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeExceptionMapper"
    )
    protected void addExceptionMapper(ExceptionMapper exceptionMapper, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                DataHolder.getInstance().getMicroservicesRegistries();
        if (channelId != null) {
            MicroservicesRegistryImpl microservicesRegistry = microservicesRegistries.get(channelId.toString());
            if (microservicesRegistry == null) {
                throw new RuntimeException("Couldn't found the registry for channel ID " + channelId);
            }
            microservicesRegistry.addExceptionMapper(exceptionMapper);
        } else {
            microservicesRegistries.values().forEach(registry -> registry.addExceptionMapper(exceptionMapper));
        }
    }

    protected void removeExceptionMapper(ExceptionMapper exceptionMapper, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                DataHolder.getInstance().getMicroservicesRegistries();
        if (channelId != null) {
            microservicesRegistries.get(channelId.toString()).removeExceptionMapper(exceptionMapper);
        }
    }

    @Reference(
            name = "session-manager",
            service = SessionManager.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeSessionManager"
    )
    protected void addSessionManager(SessionManager sessionManager, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                DataHolder.getInstance().getMicroservicesRegistries();
        if (channelId != null) {
            MicroservicesRegistryImpl microservicesRegistry = microservicesRegistries.get(channelId.toString());
            if (microservicesRegistry == null) {
                throw new RuntimeException("Couldn't found the registry for channel ID " + channelId);
            }
            microservicesRegistry.setSessionManager(sessionManager);
        } else {
            microservicesRegistries.values().forEach(registry -> registry.setSessionManager(sessionManager));
        }
    }

    protected void removeSessionManager(SessionManager sessionManager, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                DataHolder.getInstance().getMicroservicesRegistries();
        if (channelId != null) {
            sessionManager.stop();
            microservicesRegistries.get(channelId.toString()).removeSessionManager();
        }
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        DataHolder.getInstance().getBundleContext().registerService(MicroservicesServerSC.class, this, null);
        log.info("All microservices are available");
    }
}
