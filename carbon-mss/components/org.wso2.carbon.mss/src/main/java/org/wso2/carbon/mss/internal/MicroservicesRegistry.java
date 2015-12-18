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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.mss.Interceptor;
import org.wso2.carbon.mss.internal.router.ExceptionHandler;
import org.wso2.carbon.mss.internal.router.MicroserviceMetadata;
import org.wso2.carbon.mss.internal.router.URLRewriter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * MicroservicesRegistry for the MSS component.
 */
public class MicroservicesRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(MicroservicesRegistry.class);
    private static final MicroservicesRegistry instance = new MicroservicesRegistry();
    private final Set<Object> httpServices = new HashSet<>();
    private final List<Interceptor> interceptors = new ArrayList<>();
    private URLRewriter urlRewriter = null;
    private volatile MicroserviceMetadata httpResourceHandler =
            new MicroserviceMetadata(Collections.emptyList(),
                    interceptors, urlRewriter, new ExceptionHandler());

    private MicroservicesRegistry() {
    }

    /**
     * Always returns the same MicroservicesRegistry instance.
     *
     * @return the singleton MicroservicesRegistry instance
     */
    public static MicroservicesRegistry getInstance() {
        return instance;
    }

    /**
     * Every call to this method will result in the creation of a new MicroservicesRegistry instance.
     *
     * @return a new MicroservicesRegistry instance
     */
    public static MicroservicesRegistry newInstance() {
        return new MicroservicesRegistry();
    }

    public void addHttpService(Object httpHandler) {
        httpServices.add(httpHandler);
        updateHttpResourceHandler();
        LOG.info("Added microservice: " + httpHandler);
    }

    public void removeHttpService(Object httpService) {
        httpServices.remove(httpService);
        updateHttpResourceHandler();
    }

    public MicroserviceMetadata getHttpResourceHandler() {
        return httpResourceHandler;
    }

    public void addInterceptor(Interceptor interceptor) {
        interceptors.add(interceptor);
        updateHttpResourceHandler();
    }

    public void removeInterceptor(Interceptor interceptor) {
        interceptors.remove(interceptor);
        updateHttpResourceHandler();
    }

    public void setUrlRewriter(URLRewriter urlRewriter) {
        this.urlRewriter = urlRewriter;
        updateHttpResourceHandler();
    }

    public int getServiceCount() {
        return httpServices.size();
    }

    private void updateHttpResourceHandler() {
        httpResourceHandler =
                new MicroserviceMetadata(Collections.unmodifiableSet(httpServices),
                        interceptors, urlRewriter, new ExceptionHandler());
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
        httpServices.stream().forEach(httpService -> {
            invokeLifecycleMethod(httpService, lcAnnotation);
        });
    }

    private void invokeLifecycleMethod(Object httpService, Class lcAnnotation) {
        Optional<Method> lcMethod = Optional.ofNullable(getLifecycleMethod(httpService, lcAnnotation));
        if (lcMethod.isPresent()) {
            try {
                lcMethod.get().invoke(httpService, null);
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
