/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.msf4j.swagger;

import io.swagger.jaxrs.Reader;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Contact;
import io.swagger.models.Info;
import io.swagger.models.License;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Swagger BeanConfig for MS4J. We don't need to do a package scan because we know the services in advance, hence
 * we have overridden the classes method.
 */
public class MSF4JBeanConfig extends BeanConfig {
    private Set<Class<?>> classes = new HashSet<>();
    private Reader reader;

    public MSF4JBeanConfig(Reader reader) {
        this.reader = reader;
    }

    @Override
    public Set<Class<?>> classes() {
        return Collections.unmodifiableSet(classes);
    }

    public void addServiceClass(Class<?> clazz) {
        classes.add(clazz);
    }

    @Override
    public void scanAndRead() {
        Swagger swagger = reader.read(classes());
        if (StringUtils.isNotBlank(getHost())) {
            swagger.setHost(getHost());
        }

        if (StringUtils.isNotBlank(getBasePath())) {
            swagger.setBasePath(getBasePath());
        }

        updateInfoFromConfig();
    }

    private void updateInfoFromConfig() {
        if (getSwagger().getInfo() == null) {
            setInfo(new Info());
        }

        if (StringUtils.isNotBlank(getDescription())) {
            getSwagger().getInfo().setDescription(getDescription());
        }

        if (StringUtils.isNotBlank(getTitle())) {
            getSwagger().getInfo().setTitle(getTitle());
        }

        if (StringUtils.isNotBlank(getVersion())) {
            getSwagger().getInfo().setVersion(getVersion());
        }

        if (StringUtils.isNotBlank(getTermsOfServiceUrl())) {
            getSwagger().getInfo().setTermsOfService(getTermsOfServiceUrl());
        }

        if (getContact() != null) {
            getSwagger().getInfo().setContact((new Contact()).name(getContact()));
        }

        if (getLicense() != null && getLicenseUrl() != null) {
            getSwagger().getInfo().setLicense((new License()).name(getLicense()).url(getLicenseUrl()));
        }

        if (getSchemes() != null) {
            for (String scheme : getSchemes()) {
                reader.getSwagger().scheme(Scheme.forValue(scheme));
            }
        }

        reader.getSwagger().setInfo(getInfo());
    }

    @Override
    public Swagger getSwagger() {
        return reader.getSwagger();
    }
}
