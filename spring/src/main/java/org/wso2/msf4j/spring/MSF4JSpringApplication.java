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

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.core.io.ResourceLoader;

import java.util.List;

/**
 * This class is responsible to construct Microservice application.
 *
 * @since 2.0.0
 */
public class MSF4JSpringApplication {

    private static final String DEFAULT_CONTEXT_CLASS = "org.springframework.context."
                                                        + "annotation.AnnotationConfigApplicationContext";

    private Class source;
    private Class<?> configurationClass;
    private Class<? extends ConfigurableApplicationContext> applicationContextClass;
    private ResourceLoader resourceLoader;
    private List<ApplicationContextInitializer<?>> initializers;

    public MSF4JSpringApplication(Class sources) {
        initialize(sources);
    }

    private void initialize(Class source) {
        if (source != null) {
            this.source = source;
        }
    }

    public static ConfigurableApplicationContext run(Class sources, String... args) {
        return new MSF4JSpringApplication(sources).run(args);
    }

    public ConfigurableApplicationContext run(String... args) {
        ConfigurableApplicationContext context = createApplicationContext();
        if (configurationClass != null) {
            registerIfAnnotationConfigApplicationContext(context);
        } else {
            scanIfAnnotationConfigApplicationContext(context);
        }

        context.getEnvironment().getPropertySources().addFirst(new SimpleCommandLinePropertySource(args));

        context.refresh();
        return context;
    }

    private void registerIfAnnotationConfigApplicationContext(ConfigurableApplicationContext context) {
        if (context instanceof AnnotationConfigApplicationContext) {
//            String packagesForScan = getPackagesForScan();
            ((AnnotationConfigApplicationContext) context).register(MSF4JSpringConfiguration.class,
                                                                    configurationClass);
        }
    }

    private void scanIfAnnotationConfigApplicationContext(ConfigurableApplicationContext context) {
        if (context instanceof AnnotationConfigApplicationContext) {
            String packagesForScan = getPackagesForScan();
            ((AnnotationConfigApplicationContext) context).register(MSF4JSpringConfiguration.class);
            ((AnnotationConfigApplicationContext) context).scan(packagesForScan);
        }
    }

    private String getPackagesForScan() {
        return source.getPackage().getName();
    }


    protected ConfigurableApplicationContext createApplicationContext() {
        Class<?> contextClass = this.applicationContextClass;
        if (contextClass == null) {
            try {
                contextClass = Class
                        .forName(DEFAULT_CONTEXT_CLASS);
            } catch (ClassNotFoundException ex) {
                throw new IllegalStateException(
                        "Unable to create a default ApplicationContext, "
                        + "please specify an ApplicationContextClass", ex);
            }
        }
        return (ConfigurableApplicationContext) BeanUtils.instantiate(contextClass);
    }

    public Class<?> getConfigurationClass() {
        return configurationClass;
    }

    public void setConfigurationClass(Class<?> configurationClass) {
        this.configurationClass = configurationClass;
    }

    public Class<? extends ConfigurableApplicationContext> getApplicationContextClass() {
        return applicationContextClass;
    }

    public void setApplicationContextClass(
            Class<? extends ConfigurableApplicationContext> applicationContextClass) {
        this.applicationContextClass = applicationContextClass;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public List<ApplicationContextInitializer<?>> getInitializers() {
        return initializers;
    }

    public void setInitializers(List<ApplicationContextInitializer<?>> initializers) {
        this.initializers = initializers;
    }

    /**
     * This will add a given service class to the running instnace with given base path.
     *
     * @param configurableApplicationContext ConfigurableApplicationContext of running app
     * @param serviceClass Service class
     * @param basePath Base path teh servuce get registered
     */
    public void addService(ConfigurableApplicationContext configurableApplicationContext, Class serviceClass,
                           String basePath) {
        ClassPathBeanDefinitionScanner classPathBeanDefinitionScanner =
                new ClassPathBeanDefinitionScanner((BeanDefinitionRegistry) configurableApplicationContext);
        classPathBeanDefinitionScanner.scan(serviceClass.getPackage().getName());
        SpringMicroservicesRunner springMicroservicesRunner =
                configurableApplicationContext.getBean(SpringMicroservicesRunner.class);
        springMicroservicesRunner.deploy(basePath, configurableApplicationContext.getBean(serviceClass));
    }
}


