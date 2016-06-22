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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.env.Environment;
import org.wso2.msf4j.spring.transport.HTTPSTransportConfig;
import org.wso2.msf4j.spring.transport.HTTPTransportConfig;

/**
 * MSF4JBeanDefinitionRegistryPostProcessor is used by Spring to add default HTTP and/or HTTPS transports.
 *
 */
public class MSF4JBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor {

    @Autowired
    private Environment environment;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {

        if(!registry.containsBeanDefinition("http")){
            registerBeanDefinition(registry, "http", HTTPTransportConfig.class);
        }

        if(!registry.containsBeanDefinition("https")){
            registerBeanDefinition(registry, "https", HTTPSTransportConfig.class);
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    private void registerBeanDefinition(BeanDefinitionRegistry registry, String beanName, Class beanClass){
        RootBeanDefinition beanDefinition =
                new RootBeanDefinition(beanClass);
        beanDefinition.setTargetType(beanClass);
        beanDefinition.setRole(BeanDefinition.ROLE_APPLICATION);
        registry.registerBeanDefinition(beanName, beanDefinition);
    }
}
