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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.DefaultSessionManager;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.MicroservicesRegistry;
import org.wso2.msf4j.SessionManager;
import org.wso2.msf4j.SwaggerService;
import org.wso2.msf4j.interceptor.RequestInterceptor;
import org.wso2.msf4j.interceptor.ResponseInterceptor;
import org.wso2.msf4j.internal.router.MicroserviceMetadata;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.Path;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * MicroservicesRegistry for the MSF4J component.
 */
public class MicroservicesRegistryImpl implements MicroservicesRegistry {

    private static final Logger log = LoggerFactory.getLogger(MicroservicesRegistryImpl.class);
    private final Map<String, Object> services = new HashMap<>();
    private List<RequestInterceptor> globalRequestInterceptorList = new ArrayList<>();
    private List<ResponseInterceptor> globalResponseInterceptorList = new ArrayList<>();
    private volatile MicroserviceMetadata metadata = new MicroserviceMetadata(Collections.emptyMap());
    private Map<Class, ExceptionMapper> exceptionMappers = new TreeMap<>(new ClassComparator());
    private SessionManager sessionManager = new DefaultSessionManager();

    public MicroservicesRegistryImpl() {
        /* In non OSGi mode, if we can find the SwaggerDefinitionService, Deploy the Swagger definition service which
        will return the Swagger definition.*/
        if (DataHolder.getInstance().getBundleContext() == null) {
            ServiceLoader<SwaggerService> swaggerServices = ServiceLoader.load(SwaggerService.class);
            Iterator<SwaggerService> iterator = swaggerServices.iterator();
            if (iterator.hasNext()) {
                SwaggerService swaggerService = iterator.next();
                swaggerService.init(this);
                services.put(Utils.normalizePath("swagger"), swaggerService);
            }
        }
    }

    public void addService(Object... service) {
        for (Object svc : service) {
            services.put(Utils.normalizePath(svc.getClass().getAnnotation(Path.class).value()), svc);
        }
        updateMetadata();
        Arrays.stream(service).forEach(svc -> log.info("Added microservice: " + svc));
    }

    public void addService(String basePath, Object service) {
        services.put(Utils.normalizePath(basePath), service);
        metadata.addMicroserviceMetadata(service, basePath);
        log.info("Added microservice: " + service);
    }

    public Optional<Map.Entry<String, Object>> getServiceWithBasePath(String path) {
        return services.entrySet().stream().filter(svc -> svc.getKey().equals(path)).findAny();
    }

    public void removeService(Object service) {
        if (service == null) {
            log.error("Service cannot be null.");
            return;
        }
        Path path = service.getClass().getAnnotation(Path.class);
        if (path == null) {
            log.warn("Service removal failed. Microservice class '" + service.getClass().getName() +
                     "' doesn't contain a root Path.");
            return;
        }
        services.remove(Utils.normalizePath(path.value()));
        updateMetadata();
    }

    public void setSessionManager(SessionManager sessionManager) {
        if (sessionManager == null) {
            throw new IllegalArgumentException("SessionManager cannot be null");
        }
        this.sessionManager = sessionManager;
    }

    public void addInterceptor(Interceptor... interceptor) {
        Collections.addAll(globalRequestInterceptorList, interceptor);
        Collections.addAll(globalResponseInterceptorList, interceptor);
    }

    public void removeInterceptor(Interceptor interceptor) {
        globalRequestInterceptorList.remove(interceptor);
        globalResponseInterceptorList.remove(interceptor);
    }

    public MicroserviceMetadata getMetadata() {
        return metadata;
    }

    public Set<Object> getHttpServices() {
        return Collections.unmodifiableSet(services.values().stream().collect(Collectors.toSet()));
    }

    /**
     * Register request interceptors.
     *
     * @param requestInterceptor interceptor instances.
     */
    public void addGlobalRequestInterceptor(RequestInterceptor... requestInterceptor) {
        Collections.addAll(globalRequestInterceptorList, requestInterceptor);
    }

    /**
     * Register response interceptors.
     *
     * @param responseInterceptor interceptor instances.
     */
    public void addGlobalResponseInterceptor(ResponseInterceptor... responseInterceptor) {
        Collections.addAll(globalResponseInterceptorList, responseInterceptor);
    }

    /**
     * Remove msf4j request interceptor.
     *
     * @param requestInterceptor MSF4J interceptor instance.
     */
    public void removeGlobalRequestInterceptor(RequestInterceptor requestInterceptor) {
        globalRequestInterceptorList.remove(requestInterceptor);
    }

    /**
     * Remove msf4j response interceptor.
     *
     * @param responseInterceptor MSF4J interceptor instance.
     */
    public void removeGlobalResponseInterceptor(ResponseInterceptor responseInterceptor) {
        globalResponseInterceptorList.remove(responseInterceptor);
    }

    /**
     * Get global request interceptor list.
     *
     * @return global request interceptor list
     */
    public List<RequestInterceptor> getGlobalRequestInterceptorList() {
        return globalRequestInterceptorList;
    }

    /**
     * Get global response interceptor list.
     *
     * @return global response interceptor list
     */
    public List<ResponseInterceptor> getGlobalResponseInterceptorList() {
        return globalResponseInterceptorList;
    }

    public void addExceptionMapper(ExceptionMapper... mapper) {
        Arrays.stream(mapper).forEach(em -> {
            Arrays.stream(em.getClass().getMethods()).
                    filter(method -> "toResponse".equals(method.getName()) && method.getParameterCount() == 1 &&
                            !Throwable.class.getName().equals(method.getParameterTypes()[0].getTypeName())).
                    findAny().
                    ifPresent(method -> {
                        try {
                            exceptionMappers.put(Class.forName(method.getParameterTypes()[0].getTypeName(), false,
                                    em.getClass().getClassLoader()), em);
                        } catch (ClassNotFoundException e) {
                            log.error("Could not load class", e);
                        }
                    });
        });
    }

    Optional<ExceptionMapper> getExceptionMapper(Throwable throwable) {
        return exceptionMappers.entrySet().
                stream().
                filter(entry -> entry.getKey().isAssignableFrom(throwable.getClass())).
                findFirst().
                flatMap(entry -> Optional.ofNullable(entry.getValue()));
    }

    public void removeExceptionMapper(ExceptionMapper em) {
        Arrays.stream(em.getClass().getMethods()).
                filter(method -> method.getName().equals("toResponse") && method.getParameterCount() == 1).
                findAny().
                ifPresent(method -> {
                    try {
                        exceptionMappers.remove(Class.forName(method.getGenericParameterTypes()[0].getTypeName(),
                                false, em.getClass().getClassLoader()));
                    } catch (ClassNotFoundException e) {
                        log.error("Could not load class", e);
                    }
                });
    }

    public int getServiceCount() {
        return services.size();
    }

    private void updateMetadata() {
        metadata = new MicroserviceMetadata(Collections.unmodifiableMap(services));
    }

    public void initServices() {
        invokeLifecycleMethods(PostConstruct.class);
    }

    public void initService(Object httpService) {
        invokeLifecycleMethod(httpService, PostConstruct.class);
    }

    public void preDestroyServices() {
        invokeLifecycleMethods(PreDestroy.class);
    }

    public void preDestroyService(Object httpService) {
        invokeLifecycleMethod(httpService, PreDestroy.class);
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    private void invokeLifecycleMethods(Class lcAnnotation) {
        services.values().stream().forEach(httpService -> invokeLifecycleMethod(httpService, lcAnnotation));
    }

    private void invokeLifecycleMethod(Object httpService, Class lcAnnotation) {
        Optional<Method> lcMethod = Optional.ofNullable(getLifecycleMethod(httpService, lcAnnotation));
        if (lcMethod.isPresent()) {
            try {
                lcMethod.get().invoke(httpService);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new MicroservicesLCException("Exception occurs calling lifecycle method", e);
            }
        }
    }

    private Method getLifecycleMethod(Object httpService, Class lcAnnotation) {
        return Arrays.stream(httpService.getClass().getDeclaredMethods()).filter(m -> isValidLifecycleMethod
                (Optional.of(m), lcAnnotation)).findFirst().orElse(null);
    }

    private boolean isValidLifecycleMethod(Optional<Method> method, Class lcAnnotation) {
        return method.filter(m -> Modifier.isPublic(m.getModifiers())
                && m.getAnnotation(lcAnnotation) != null).isPresent();
    }
}
