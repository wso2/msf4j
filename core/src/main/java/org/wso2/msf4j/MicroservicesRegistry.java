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
    Optional<Map.Entry<String, Object>> getServiceWithBasePath(String path);

    Set<Object> getHttpServices();

    void addService(Object... service);

    void addService(String basePath, Object service);

    void removeService(Object service);

    void setSessionManager(SessionManager sessionManager);

    void addGlobalRequestInterceptor(RequestInterceptor... requestInterceptor);

    void removeGlobalRequestInterceptor(RequestInterceptor requestInterceptor);

    void addGlobalResponseInterceptor(ResponseInterceptor... responseInterceptors);

    void removeGlobalResponseInterceptor(ResponseInterceptor responseInterceptor);

    void addInterceptor(Interceptor... interceptor);

    void removeInterceptor(Interceptor interceptor);

    void addExceptionMapper(ExceptionMapper... mapper);

    void removeExceptionMapper(ExceptionMapper em);

    void initServices();

    void initService(Object httpService);

    void preDestroyServices();

    void preDestroyService(Object httpService);
}
