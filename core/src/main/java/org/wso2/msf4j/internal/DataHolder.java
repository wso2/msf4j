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
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.transports.CarbonTransport;
import org.wso2.carbon.transport.http.netty.listener.CarbonNettyServerInitializer;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * DataHolder for MSF4J.
 */
public class DataHolder {
    private static final Logger log = LoggerFactory.getLogger(DataHolder.class);

    private static final String CHANNEL_ID_KEY = "channel.id";

    private static DataHolder instance = new DataHolder();
    private BundleContext bundleContext;
    private Map<String, ServiceRegistration<CarbonNettyServerInitializer>> carbonTransports = new HashMap<>();

    private DataHolder() {
    }

    public static DataHolder getInstance() {
        return instance;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    // TODO: Fix OSGi mode
    public void addCarbonTransport(CarbonTransport carbonTransport) {
        if (bundleContext == null) {
            log.error("BundleContext is null. Transport dispatching will fail.");
            return;
        }
        String channelKey = carbonTransport.getId();
        Hashtable<String, String> httpInitParams = new Hashtable<>();
        httpInitParams.put(CHANNEL_ID_KEY, channelKey);
        CarbonNettyServerInitializer gatewayNettyInitializer =
                new CarbonNettyServerInitializer(null);
        ServiceRegistration<CarbonNettyServerInitializer> service =
                bundleContext.registerService(CarbonNettyServerInitializer.class,
                        gatewayNettyInitializer, httpInitParams);
        carbonTransports.put(channelKey, service);
    }

    public void removeCarbonTransport(CarbonTransport carbonTransport) {
        carbonTransports.get(carbonTransport.getId()).unregister();
    }
}
