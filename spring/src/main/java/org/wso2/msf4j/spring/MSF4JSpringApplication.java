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
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.wso2.msf4j.spring.property.YamlFileApplicationContextInitializer;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


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
    private List<ApplicationContextInitializer<?>> initializers = new ArrayList<>();

    public MSF4JSpringApplication(Class sources) {
        initialize(sources);
    }

    private void initialize(Class source) {
        if (source != null) {
            this.source = source;
        }
        setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
        addInitializers(new YamlFileApplicationContextInitializer());
    }


    public void setInitializers(Collection<? extends ApplicationContextInitializer<?>> initializers) {
        this.initializers = new ArrayList<>();
        this.initializers.addAll(initializers);
    }

    private <T> Collection<? extends T> getSpringFactoriesInstances(Class<T> type) {
        return getSpringFactoriesInstances(type, new Class<?>[]{});
    }

    private <T> Collection<? extends T> getSpringFactoriesInstances(Class<T> type,
                                                                    Class<?>[] parameterTypes, Object... args) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Set<String> names = new LinkedHashSet<>(
                SpringFactoriesLoader.loadFactoryNames(type, classLoader));
        List<T> instances = createSpringFactoriesInstances(type, parameterTypes,
                classLoader, args, names);
        AnnotationAwareOrderComparator.sort(instances);
        return instances;
    }

    private <T> List<T> createSpringFactoriesInstances(Class<T> type,
                                                       Class<?>[] parameterTypes, ClassLoader classLoader,
                                                       Object[] args, Set<String> names) {
        List<T> instances = new ArrayList<T>(names.size());
        for (String name : names) {
            try {
                Class<?> instanceClass = ClassUtils.forName(name, classLoader);
                Assert.isAssignable(type, instanceClass);
                Constructor<?> constructor = instanceClass
                        .getDeclaredConstructor(parameterTypes);
                T instance = (T) BeanUtils.instantiateClass(constructor, args);
                instances.add(instance);
            } catch (Throwable ex) {
                throw new IllegalArgumentException(
                        "Cannot instantiate " + type + " : " + name, ex);
            }
        }
        return instances;
    }

    public void addInitializers(ApplicationContextInitializer<?>... initializers) {
        this.initializers.addAll(Arrays.asList(initializers));
    }

    public static ConfigurableApplicationContext run(Class sources, String... args) {
        MSF4JSpringApplication application = new MSF4JSpringApplication(sources);
        ConfigurableApplicationContext context = application.run(args);
        application.applyInitializers(context);
        return context;
    }

    private void applyInitializers(ConfigurableApplicationContext context) {
        for (ApplicationContextInitializer initializer : getInitializers()) {
            Class<?> requiredType = GenericTypeResolver.resolveTypeArgument(
                    initializer.getClass(), ApplicationContextInitializer.class);
            Assert.isInstanceOf(requiredType, context, "Unable to call initializer.");
            initializer.initialize(context);
        }
    }

    protected ConfigurableApplicationContext run(String... args) {
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


