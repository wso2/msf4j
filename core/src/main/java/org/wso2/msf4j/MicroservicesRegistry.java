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

package org.wso2.msf4j;

import org.wso2.msf4j.interceptor.RequestInterceptor;
import org.wso2.msf4j.interceptor.ResponseInterceptor;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Interface that needs to be implemented for handle the registry.
 */
public interface MicroservicesRegistry {

    /**
     * Get the service object with given base path.
     *
     * @param path base path of the service
     * @return service obj for the given base path or Optional null if no service found for given path.
     */
    Optional<Map.Entry<String, Object>> getServiceWithBasePath(String path);

    /**
     * Get all the available service.
     *
     * @return Set of all the available services
     */
    Set<Object> getHttpServices();

    /**
     * Register service instances.
     *
     * @param service instances
     */
    void addService(Object... service);

    /**
     * Register a service instance to given basepath. This will discard the class level Path annotation.
     *
     * @param basePath path string which services need to be registered.
     * @param service instance.
     */
    void addService(String basePath, Object service);

    /**
     * Remove the given service instance.
     *
     * @param service instance to be remove.
     */
    void removeService(Object service);

    /**
     * Register SessionManager.
     *
     * @param sessionManager SessionManager instance.
     */
    void setSessionManager(SessionManager sessionManager);

    /**
     * Register request interceptors.
     *
     * @param requestInterceptor interceptor instances.
     */
    void addGlobalRequestInterceptor(RequestInterceptor... requestInterceptor);

    /**
     * Remove msf4j request interceptor.
     *
     * @param requestInterceptor MSF4J interceptor instance.
     */
    void removeGlobalRequestInterceptor(RequestInterceptor requestInterceptor);

    /**
     * Register response interceptors.
     *
     * @param responseInterceptors interceptor instances.
     */
    void addGlobalResponseInterceptor(ResponseInterceptor... responseInterceptors);

    /**
     * Remove msf4j response interceptor.
     *
     * @param responseInterceptor MSF4J interceptor instance.
     */
    void removeGlobalResponseInterceptor(ResponseInterceptor responseInterceptor);

    /**
     * Add msf4j global request and response interceptor.
     *
     * @param interceptor instance.
     */
    @Deprecated
    void addInterceptor(Interceptor... interceptor);

    /**
     * Remove msf4j global request and response interceptor.
     *
     * @param interceptor instance.
     */
    @Deprecated
    void removeInterceptor(Interceptor interceptor);

    /**
     * Register exception mappers.
     *
     * @param mapper ExceptionMapper instances.
     */
    void addExceptionMapper(ExceptionMapper... mapper);

    /**
     * Remove exception mapper.
     *
     * @param em ExceptionMapper instance.
     */
    void removeExceptionMapper(ExceptionMapper em);

    /**
     * Invoke post construct life cycle state for all the service.
     */
    void initServices();

    /**
     * Invoke post construct life cycle state for given service.
     *
     * @param httpService service instance
     */
    void initService(Object httpService);

    /**
     * Invoke predestroy life cycle state for all the service.
     */
    void preDestroyServices();

    /**
     * Invoke predestroy life cycle state for given service.
     *
     * @param httpService service instance
     */
    void preDestroyService(Object httpService);
}
