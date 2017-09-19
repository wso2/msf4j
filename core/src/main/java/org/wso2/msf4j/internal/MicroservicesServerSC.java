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
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.kernel.startupresolver.RequiredCapabilityListener;
import org.wso2.carbon.kernel.startupresolver.StartupServiceUtils;
import org.wso2.carbon.messaging.ServerConnector;
import org.wso2.msf4j.DefaultSessionManager;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.MicroservicesRegistry;
import org.wso2.msf4j.SessionManager;
import org.wso2.msf4j.SwaggerService;
import org.wso2.msf4j.exception.OSGiDeclarativeServiceException;
import org.wso2.msf4j.interceptor.OSGiInterceptorConfig;
import org.wso2.msf4j.util.RuntimeAnnotations;

import java.util.Arrays;
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
    private boolean isAllRequiredCapabilitiesAvailable;

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
        /*
        Some Microservices might get register even after #onAllRequiredCapabilitiesAvailable
        That is due to the UUF doesn't know the actual service count before hand.
        Therefore we need to handle those services separately.
         */
        if (isAllRequiredCapabilitiesAvailable) {
            Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
            Object contextPath = properties.get("contextPath");
            addMicroserviceToRegistry(service, channelId, contextPath);
        }
        StartupServiceUtils.updateServiceCache("wso2-microservices-server", Microservice.class);
    }

    protected void removeService(Microservice service, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                DataHolder.getInstance().getMicroservicesRegistries();
        if (channelId != null) {
            MicroservicesRegistryImpl microservicesRegistry = microservicesRegistries.get(channelId.toString());
            if (microservicesRegistry != null) {
                microservicesRegistry.removeService(service);
            }
        } else {
            microservicesRegistries.values().forEach(registry -> registry.removeService(service));
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
                microservicesRegistry.removeService(service);
            }
        }
    }

    @Reference(
            name = "http-connector-provider",
            service = ServerConnector.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeCarbonTransport"
    )
    protected void addCarbonTransport(ServerConnector serverConnector) {
        MicroservicesRegistryImpl microservicesRegistry = new MicroservicesRegistryImpl();
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                DataHolder.getInstance().getMicroservicesRegistries();
        Dictionary<String, String> properties = new Hashtable<>();
        properties.put(MSF4JConstants.CHANNEL_ID, serverConnector.getId());
        microservicesRegistries.put(serverConnector.getId(), microservicesRegistry);
        DataHolder.getInstance().getBundleContext()
                  .registerService(MicroservicesRegistry.class, microservicesRegistry, properties);
        StartupServiceUtils.updateServiceCache("wso2-microservices-server", ServerConnector.class);
    }

    protected void removeCarbonTransport(ServerConnector serverConnector) {
        DataHolder.getInstance().getMicroservicesRegistries().remove(serverConnector.getId());
    }

    @Reference(
            name = "carbon.config.provider",
            service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigProvider"
    )
    protected void registerConfigProvider(ConfigProvider configProvider) {
        DataHolder.getInstance().setConfigProvider(configProvider);
        StartupServiceUtils.updateServiceCache("wso2-microservices-server", ConfigProvider.class);
    }

    protected void unregisterConfigProvider(ConfigProvider configProvider) {
        DataHolder.getInstance().setConfigProvider(null);
    }

    @Reference(
            name = "interceptor-config",
            service = OSGiInterceptorConfig.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeInterceptorConfig"
    )
    protected void addInterceptorConfig(OSGiInterceptorConfig interceptorConfig, Map properties) {
        StartupServiceUtils.updateServiceCache("wso2-microservices-server", OSGiInterceptorConfig.class);
    }

    protected void removeInterceptorConfig(OSGiInterceptorConfig interceptorConfig, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                DataHolder.getInstance().getMicroservicesRegistries();
        if (channelId != null) {
            MicroservicesRegistryImpl microServicesRegistry = microservicesRegistries.get(channelId.toString());
            Arrays.stream(interceptorConfig.getGlobalRequestInterceptorArray()).forEach(
                    microServicesRegistry::removeGlobalRequestInterceptor);
            Arrays.stream(interceptorConfig.getGlobalResponseInterceptorArray()).forEach(
                    microServicesRegistry::removeGlobalResponseInterceptor);
        } else {
            microservicesRegistries.values().forEach(registry -> {
                Arrays.stream(interceptorConfig.getGlobalRequestInterceptorArray()).forEach(
                        registry::removeGlobalRequestInterceptor);
                Arrays.stream(interceptorConfig.getGlobalResponseInterceptorArray()).forEach(
                        registry::removeGlobalResponseInterceptor);
            });
        }
    }

    @Reference(
            name = "interceptor",
            service = Interceptor.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeInterceptor"
    )
    protected void addInterceptor(Interceptor interceptor, Map properties) {
        StartupServiceUtils.updateServiceCache("wso2-microservices-server", Interceptor.class);
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
        } else {
            microservicesRegistries.values().forEach(registry -> {
                registry.removeGlobalRequestInterceptor(interceptor);
                registry.removeGlobalResponseInterceptor(interceptor);
            });
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
    }

    protected void removeExceptionMapper(ExceptionMapper exceptionMapper, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                DataHolder.getInstance().getMicroservicesRegistries();
        if (channelId != null) {
            microservicesRegistries.get(channelId.toString()).removeExceptionMapper(exceptionMapper);
        } else {
            microservicesRegistries.values().forEach(registry -> registry.removeExceptionMapper(exceptionMapper));
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
    }

    protected void removeSessionManager(SessionManager sessionManager, Map properties) {
        Object channelId = properties.get(MSF4JConstants.CHANNEL_ID);
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                DataHolder.getInstance().getMicroservicesRegistries();
        sessionManager.stop();
        DefaultSessionManager defaultSessionManager = new DefaultSessionManager();
        defaultSessionManager.init();
        if (channelId != null) {
            microservicesRegistries.get(channelId.toString()).setSessionManager(defaultSessionManager);
        } else {
            microservicesRegistries.values().forEach(registry -> registry.setSessionManager(defaultSessionManager));
        }
    }

    @Override
    public void onAllRequiredCapabilitiesAvailable() {
        try {
            ServiceReference[] serviceReferences = DataHolder.getInstance().getBundleContext()
                                                             .getServiceReferences(Microservice.class.getName(), null);
            if (serviceReferences != null && serviceReferences.length > 0) {
                Arrays.stream(serviceReferences).forEach(serviceReference -> {
                    Microservice service =
                            (Microservice) DataHolder.getInstance().getBundleContext().getService(serviceReference);
                    Object channelId = serviceReference.getProperty(MSF4JConstants.CHANNEL_ID);
                    Object contextPath = serviceReference.getProperty("contextPath");
                    addMicroserviceToRegistry(service, channelId, contextPath);
                });
            }

            // Add request and response interceptors
            serviceReferences = DataHolder.getInstance().getBundleContext()
                                          .getServiceReferences(OSGiInterceptorConfig.class.getName(), null);
            if (serviceReferences != null && serviceReferences.length > 0) {
                Arrays.stream(serviceReferences).forEach(serviceReference -> {
                    OSGiInterceptorConfig interceptorConfig =
                            (OSGiInterceptorConfig) DataHolder.getInstance().getBundleContext()
                                                              .getService(serviceReference);
                    Object channelId = serviceReference.getProperty(MSF4JConstants.CHANNEL_ID);
                    addRequestResponseInterceptorsToRegistry(interceptorConfig, channelId);
                });
            }

            serviceReferences =
                    DataHolder.getInstance().getBundleContext().getServiceReferences(Interceptor.class.getName(), null);
            if (serviceReferences != null && serviceReferences.length > 0) {
                Arrays.stream(serviceReferences).forEach(serviceReference -> {
                    Interceptor interceptor =
                            (Interceptor) DataHolder.getInstance().getBundleContext().getService(serviceReference);
                    Object channelId = serviceReference.getProperty(MSF4JConstants.CHANNEL_ID);
                    addInterceptorToRegistry(interceptor, channelId);
                });
            }

            serviceReferences = DataHolder.getInstance().getBundleContext()
                                          .getServiceReferences(ExceptionMapper.class.getName(), null);
            if (serviceReferences != null && serviceReferences.length > 0) {
                Arrays.stream(serviceReferences).forEach(serviceReference -> {
                    ExceptionMapper exceptionMapper =
                            (ExceptionMapper) DataHolder.getInstance().getBundleContext().getService(serviceReference);
                    Object channelId = serviceReference.getProperty(MSF4JConstants.CHANNEL_ID);
                    addExceptionMapperToRegistry(exceptionMapper, channelId);
                });
            }

            serviceReferences = DataHolder.getInstance().getBundleContext()
                                          .getServiceReferences(SessionManager.class.getName(), null);
            if (serviceReferences != null && serviceReferences.length > 0) {
                Arrays.stream(serviceReferences).forEach(serviceReference -> {
                    SessionManager sessionManager =
                            (SessionManager) DataHolder.getInstance().getBundleContext().getService(serviceReference);
                    Object channelId = serviceReference.getProperty(MSF4JConstants.CHANNEL_ID);
                    addSessionManagerToRegistry(sessionManager, channelId);
                });
            }
        } catch (InvalidSyntaxException e) {
            log.error("Error while registering required capabilities. " + e.getMessage());
        } finally {
            isAllRequiredCapabilitiesAvailable = true;
        }

        /*DataHolder.getInstance().getBundleContext()
                  .registerService(CarbonMessageProcessor.class, new MSF4JMessageProcessor(), null);*/
        DataHolder.getInstance().getBundleContext().registerService(MicroservicesServerSC.class, this, null);
        log.info("All microservices are available");
    }

    private void addMicroserviceToRegistry(Microservice service, Object channelId, Object contextPath) {
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

    /**
     * Add request and response interceptors to registry.
     *
     * @param interceptorConfig interceptor configuration
     * @param channelId         micro-service channel id
     */
    private void addRequestResponseInterceptorsToRegistry(OSGiInterceptorConfig interceptorConfig, Object channelId) {
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                DataHolder.getInstance().getMicroservicesRegistries();
        if (channelId != null) {
            MicroservicesRegistryImpl microservicesRegistry = microservicesRegistries.get(channelId.toString());
            if (microservicesRegistry == null) {
                throw new OSGiDeclarativeServiceException("Couldn't find the registry for channel ID " +
                                                          channelId);
            }
            microservicesRegistry.addGlobalRequestInterceptor(interceptorConfig.getGlobalRequestInterceptorArray());
            microservicesRegistry.addGlobalResponseInterceptor(interceptorConfig.getGlobalResponseInterceptorArray());
        } else {
            microservicesRegistries.values().forEach(registry -> {
                registry.addGlobalRequestInterceptor(interceptorConfig.getGlobalRequestInterceptorArray());
                registry.addGlobalResponseInterceptor(interceptorConfig.getGlobalResponseInterceptorArray());
            });
        }
    }

    /**
     * Add interceptor to registry.
     *
     * @param interceptor interceptor
     * @param channelId   micro-service channel it
     */
    @Deprecated
    private void addInterceptorToRegistry(Interceptor interceptor, Object channelId) {
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                DataHolder.getInstance().getMicroservicesRegistries();
        if (channelId != null) {
            MicroservicesRegistryImpl microservicesRegistry = microservicesRegistries.get(channelId.toString());
            if (microservicesRegistry == null) {
                throw new OSGiDeclarativeServiceException("Couldn't found the registry for channel ID " + channelId);
            }
            microservicesRegistry.addGlobalRequestInterceptor(interceptor);
            microservicesRegistry.addGlobalResponseInterceptor(interceptor);
        } else {
            microservicesRegistries.values().forEach(registry -> {
                registry.addGlobalRequestInterceptor(interceptor);
                registry.addGlobalResponseInterceptor(interceptor);
            });
        }
    }

    private void addExceptionMapperToRegistry(ExceptionMapper exceptionMapper, Object channelId) {
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

    private void addSessionManagerToRegistry(SessionManager sessionManager, Object channelId) {
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                DataHolder.getInstance().getMicroservicesRegistries();
        sessionManager.init();
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
}
