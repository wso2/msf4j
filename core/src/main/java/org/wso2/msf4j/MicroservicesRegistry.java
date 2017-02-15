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

import java.util.Optional;
import java.util.Set;

/**
 * Interface that needs to be implemented for handle the registry.
 */
public interface MicroservicesRegistry {

    /**
     * Get service context for a given base path
     *
     * @param path unique http path
     * @return Optional micro-service context
     */
    Optional<MicroServiceContext> getServiceContextForBasePath(String path);

    /**
     * Get http micro-services.
     *
     * @return http micro-services
     */
    Set<Object> getHttpServices();

    /**
     * Get http micro-service contexts
     *
     * @return http micro-service contexts
     */
    Set<MicroServiceContext> getHttpServiceContexts();

}
