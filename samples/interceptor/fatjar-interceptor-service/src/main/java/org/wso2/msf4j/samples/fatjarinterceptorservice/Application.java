/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.msf4j.samples.fatjarinterceptorservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.config.YAMLTransportConfigurationBuilder;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.samples.interceptor.common.LogTextRequestInterceptor;
import org.wso2.msf4j.samples.interceptor.common.LogTextResponseInterceptor;
import org.wso2.msf4j.samples.interceptor.common.PropertyAddRequestInterceptor;
import org.wso2.msf4j.samples.interceptor.common.PropertyGetResponseInterceptor;

import java.net.URL;

/**
 * Main class of starting the interceptor micro-service.
 */
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private Application() {
    }

    public static void main(String[] args) {
        URL resource = Thread.currentThread().getContextClassLoader().getResource("netty-transports.yml");
        if (resource != null) {
            System.setProperty(YAMLTransportConfigurationBuilder.NETTY_TRANSPORT_CONF, resource.getPath());
        } else {
            log.error("netty-transports.yml not found in resources - proceeding with default netty configuration");
        }
        new MicroservicesRunner()
                .deploy(new InterceptorService())
                .addGlobalRequestInterceptor(new LogTextRequestInterceptor(), new PropertyAddRequestInterceptor())
                .addGlobalResponseInterceptor(new LogTextResponseInterceptor(), new PropertyGetResponseInterceptor())
                .start();
    }
}
