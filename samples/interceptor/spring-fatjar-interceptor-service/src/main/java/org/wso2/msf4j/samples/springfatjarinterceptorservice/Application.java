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
package org.wso2.msf4j.samples.springfatjarinterceptorservice;

import org.springframework.context.ConfigurableApplicationContext;
import org.wso2.msf4j.samples.interceptor.common.LogTextRequestInterceptor;
import org.wso2.msf4j.samples.interceptor.common.LogTextResponseInterceptor;
import org.wso2.msf4j.samples.interceptor.common.PropertyAddRequestInterceptor;
import org.wso2.msf4j.samples.interceptor.common.PropertyGetResponseInterceptor;
import org.wso2.msf4j.spring.MSF4JSpringApplication;

/**
 * Main class of starting the spring interceptor demo.
 *
 * @since 2.4.2
 */
public class Application {
    public static void main(String[] args) {
        MSF4JSpringApplication msf4JSpringApplication = new MSF4JSpringApplication(ReceptionService.class);
        ConfigurableApplicationContext configurableApplicationContext =
                MSF4JSpringApplication.run(ReceptionService.class, "--http.port=8090");
        msf4JSpringApplication.addService(configurableApplicationContext, ReceptionService.class,
                "/reception-service")
                .addGlobalRequestInterceptor(new LogTextRequestInterceptor(), new PropertyAddRequestInterceptor())
                .addGlobalResponseInterceptor(new LogTextResponseInterceptor(), new PropertyGetResponseInterceptor());
    }
}
