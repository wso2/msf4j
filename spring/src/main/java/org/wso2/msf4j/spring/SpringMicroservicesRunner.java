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
package org.wso2.msf4j.spring;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.MicroservicesRunner;

import java.util.Map;
import javax.ws.rs.Path;

/**
 * This runner initializes the microservices runtime based on provided Spring configuration, deploys microservices,
 * service interceptors and starts the relevant transports.
 */

@Component
public class SpringMicroservicesRunner extends MicroservicesRunner
        implements ApplicationContextAware, InitializingBean {

    private final Log log = LogFactory.getLog(getClass());


    private ApplicationContext applicationContext;

    public void init() {
        for (Map.Entry<String, Object> entry : applicationContext.getBeansWithAnnotation(Path.class).entrySet()) {
            log.info("Deploying " + entry.getKey() + " bean as a resource");
            deploy(entry.getValue());
        }

        Map<String, Interceptor> interceptors = applicationContext.getBeansOfType(Interceptor.class);
        for (Map.Entry<String, Interceptor> entry : interceptors.entrySet()) {
            log.info("Adding " + entry.getKey() + "  Interceptor");
            addInterceptor(entry.getValue());
        }
        start();
    }


    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    public void afterPropertiesSet() throws Exception {
        init();
    }
}
