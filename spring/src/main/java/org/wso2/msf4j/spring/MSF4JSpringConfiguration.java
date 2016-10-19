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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Default Spring configuration class for Microservices, it's possible to provide alternative Spring configuration
 * classes by application developers.
 *
 * @since 2.0.0
 */
@Configuration
@ComponentScan
@PropertySources({
        @PropertySource(value = "file:application.properties", ignoreResourceNotFound = true,
                name = "applicationProperties"),
        @PropertySource(value = "classpath:application.properties", ignoreResourceNotFound = true,
                name = "applicationProperties"),
        @PropertySource(value = "file:application-${spring.profiles.active}.properties", ignoreResourceNotFound = true,
                name = "applicationProperties"),
        @PropertySource(value = "classpath:application-${spring.profiles.active}.properties",
                ignoreResourceNotFound = true, name = "applicationProperties")
})
public class MSF4JSpringConfiguration {

    @Bean
    public MSF4JBeanDefinitionRegistryPostProcessor msf4JBeanDefinitionRegistryPostProcessor() {
        return new MSF4JBeanDefinitionRegistryPostProcessor();
    }

    @Bean
    public SpringMicroservicesRunner springMicroservicesRunner() {
        return new SpringMicroservicesRunner();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigurer() {
        PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
        pspc.setIgnoreUnresolvablePlaceholders(true);
        return pspc;
    }
}
