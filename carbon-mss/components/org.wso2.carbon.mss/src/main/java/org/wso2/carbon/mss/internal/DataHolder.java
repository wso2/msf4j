/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.transports.CarbonTransport;
import org.wso2.carbon.messaging.CarbonTransportInitializer;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

/**
 * DataHolder for MSS.
 */
public class DataHolder {
    private static final Logger log = LoggerFactory.getLogger(DataHolder.class);

    private static final String CHANNEL_ID_KEY = "channel.id";

    private static DataHolder instance = new DataHolder();
    private BundleContext bundleContext;
    private Map<String, ServiceRegistration<CarbonTransportInitializer>> carbonTransports = new HashMap<>();

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

    public void addCarbonTransport(CarbonTransport carbonTransport) {
        if (bundleContext == null) {
            log.error("BundleContext is null. Transport dispatching will fail.");
            return;
        }
        String channelKey = carbonTransport.getId();
        Hashtable<String, String> httpInitParams = new Hashtable<>();
        httpInitParams.put(CHANNEL_ID_KEY, channelKey);
        MSSNettyServerInitializer gatewayNettyInitializer =
                new MSSNettyServerInitializer(MicroservicesRegistry.getInstance());
        ServiceRegistration<CarbonTransportInitializer> service =
                bundleContext.registerService(CarbonTransportInitializer.class,
                        gatewayNettyInitializer, httpInitParams);
        carbonTransports.put(channelKey, service);
    }

    public void removeCarbonTransport(CarbonTransport carbonTransport) {
        carbonTransports.get(carbonTransport.getId()).unregister();
    }
}
