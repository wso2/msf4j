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
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.internal.router.MicroserviceMetadata;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.Path;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * MicroservicesRegistry for the MSF4J component.
 */
public class MicroservicesRegistry {

    private static final Logger log = LoggerFactory.getLogger(MicroservicesRegistry.class);
    private final Set<Object> services = new HashSet<>();

    private final List<Interceptor> interceptors = new ArrayList<>();
    private volatile MicroserviceMetadata metadata = new MicroserviceMetadata(Collections.emptyList());
    private Map<Class, ExceptionMapper> exceptionMappers = new TreeMap<>(new ClassComparator());

    public void addService(Object... service) {
        Collections.addAll(services, service);
        updateMetadata();
        Arrays.stream(service).forEach(svc -> log.info("Added microservice: " + svc));
    }

    public Optional<Object> getServiceWithBasePath(String path) {
        return services.stream().
                filter(svc -> svc.getClass().getAnnotation(Path.class).value().equals(path)).
                findAny();
    }

    public void removeService(Object service) {
        services.remove(service);
        updateMetadata();
    }

    public MicroserviceMetadata getMetadata() {
        return metadata;
    }

    public Set<Object> getHttpServices() {
        return Collections.unmodifiableSet(services);
    }

    public void addInterceptor(Interceptor... interceptor) {
        Collections.addAll(interceptors, interceptor);
        updateMetadata();
    }

    public void addExceptionMapper(ExceptionMapper... mapper) {
        Arrays.stream(mapper).forEach(em -> {
            Arrays.stream(em.getClass().getMethods()).
                    filter(method -> "toResponse".equals(method.getName()) && method.getParameterCount() == 1).
                    findAny().
                    ifPresent(method -> {
                        try {
                            exceptionMappers.put(Class.forName(method.getGenericParameterTypes()[0].getTypeName()), em);
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
                        exceptionMappers.remove(Class.forName(method.getGenericParameterTypes()[0].getTypeName()));
                    } catch (ClassNotFoundException e) {
                        log.error("Could not load class", e);
                    }
                });
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    public void removeInterceptor(Interceptor interceptor) {
        interceptors.remove(interceptor);
        updateMetadata();
    }

    public int getServiceCount() {
        return services.size();
    }

    private void updateMetadata() {
        metadata = new MicroserviceMetadata(Collections.unmodifiableSet(services));
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

    private void invokeLifecycleMethods(Class lcAnnotation) {
        services.stream().forEach(httpService -> invokeLifecycleMethod(httpService, lcAnnotation));
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
