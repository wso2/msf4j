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

import org.wso2.msf4j.MicroservicesRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * DataHolder for Swagger component.
 */
public class DataHolder {

    private static final DataHolder instance = new DataHolder();

    private Map<String, MicroservicesRegistry> microserviceRegistries = new HashMap<>();

    private DataHolder() {
    }

    /**
     * Get DataHolder object.
     *
     * @return DataHolder instance.
     */
    public static DataHolder getInstance() {
        return instance;
    }

    /**
     * Get available MicroservicesRegistries.
     *
     * @return Map of available MicroservicesRegistries.
     */
    public Map<String, MicroservicesRegistry> getMicroservicesRegistries() {
        return microserviceRegistries;
    }

    /**
     * Get available MicroservicesRegistry for the gievn registry Id.
     *
     * @param registryId of the registry need to be pick
     * @return MicroservicesRegistry object if the given Id.
     */
    public MicroservicesRegistry getMicroserviceRegistry(String registryId) {
        return microserviceRegistries.get(registryId);
    }

    /**
     * Add MicroservicesRegistry with the given registry Id.
     *
     * @param registryId id of the registry.
     * @param microservicesRegistry instance.
     */
    public void addMicroserviceRegistry(String registryId, MicroservicesRegistry microservicesRegistry) {
        microserviceRegistries.put(registryId, microservicesRegistry);
    }

    /**
     * Get all the MicroservicesRegistries
     * @return Map of MicroservicesRegistries and there IDs
     *
     */
    public Map<String, MicroservicesRegistry> getMicroserviceRegistries() {
        return microserviceRegistries;
    }

}
