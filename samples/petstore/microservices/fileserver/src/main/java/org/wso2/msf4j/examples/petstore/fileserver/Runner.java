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

package org.wso2.msf4j.examples.petstore.fileserver;

import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.analytics.httpmonitoring.HTTPMonitoringInterceptor;
import org.wso2.msf4j.analytics.metrics.MetricsInterceptor;

/**
 * Microservice runner for file server
 */
public class Runner {

    public static void main(String[] args) {
        HTTPMonitoringInterceptor httpMonitoringInterceptor = new HTTPMonitoringInterceptor();
        MetricsInterceptor metricsInterceptor = new MetricsInterceptor();
        new MicroservicesRunner()
                .addGlobalRequestInterceptor(httpMonitoringInterceptor, metricsInterceptor)
                .addGlobalResponseInterceptor(httpMonitoringInterceptor, metricsInterceptor)
                .deploy(new FileServerService())
                .start();
    }
}
