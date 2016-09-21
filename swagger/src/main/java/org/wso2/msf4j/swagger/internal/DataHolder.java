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
package org.wso2.msf4j.swagger.internal;

import org.osgi.framework.BundleContext;
import org.wso2.msf4j.MicroservicesRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * DataHolder for Swagger component.
 */
public class DataHolder {

    private static final DataHolder instance = new DataHolder();

    private Map<String, MicroservicesRegistry> microserviceRegistries = new HashMap<>();

    private BundleContext bundleContext;

    private DataHolder() {
    }

    public static DataHolder getInstance() {
        return instance;
    }

    public MicroservicesRegistry getMicroserviceRegistry(String registryId) {
        return microserviceRegistries.get(registryId);
    }

    public void addMicroserviceRegistry(String registryId, MicroservicesRegistry microservicesRegistry) {
        microserviceRegistries.put(registryId, microservicesRegistry);
    }

    public Map<String, MicroservicesRegistry> getMicroserviceRegistries() {
        return microserviceRegistries;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }
}
