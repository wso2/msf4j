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

package org.wso2.msf4j.spring.transport;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.wso2.msf4j.spring.SpringConstants;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.PostConstruct;

/**
 * abstract TransportConfig bean
 *
 * @since 2.0.0
 */
public abstract class TransportConfig implements BeanNameAware {

    private String id;
    private String enabledProperty;
    private String portProperty;
    private String hostProperty;
    private String schemeProperty;
    private String keyStoreFileProperty;
    private String keyStorePassProperty;
    private String certPassProperty;
    private String bossThreadPoolSizeProperty;
    private String workerThreadPoolSizeProperty;
    private String execHandlerThreadPoolSizeProperty;
    private Boolean enabled;
    private Integer port;
    private String host;
    private String scheme;
    private String keyStoreFile;
    private String keyStorePass;
    private String certPass;
    private Integer bossThreadPoolSize;
    private Integer workerThreadPoolSize;
    private Integer execHandlerThreadPoolSize;
    private String beanName;
    private Map<String, String> parameters = new HashMap<>();

    @Autowired
    Environment env;

    @PostConstruct
    public void init() {
        id = resolveId();
        enabled = resolveEnabled();
        port = resolvePort();
        host = resolveHost();
        if (isHTTPS()) {
            keyStoreFile = resolveKeyStoreFile();
            keyStorePass = resolveKeyStorePass();
            certPass = resolveKeyCertPass();

        }

        for (Iterator it = ((AbstractEnvironment) env).getPropertySources().iterator(); it.hasNext(); ) {
            Object propertySource = it.next();
            if (propertySource instanceof MapPropertySource
                && SpringConstants.APPLICATION_PROPERTIES.equals(((MapPropertySource) propertySource).getName())) {
                MapPropertySource mapPropertySource = (MapPropertySource) propertySource;
                for (Map.Entry<String, Object> entry : mapPropertySource.getSource().entrySet()) {
                    String key = entry.getKey();
                    if (key.startsWith(getScheme()) && key.contains(SpringConstants.PARAMETER_STR)) {
                        parameters.put(key.substring(key.indexOf(SpringConstants.PARAMETER_STR) + 11), (String) entry
                                .getValue());
                    }
                }
            }
        }
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    private String resolveKeyCertPass() {
        if (!keyStorePassProperty.isEmpty()) {
            return keyStorePassProperty;
        }
        return keyStorePass;
    }

    private String resolveKeyStorePass() {
        if (!keyStorePassProperty.isEmpty()) {
            return keyStorePassProperty;
        }
        return keyStorePass;
    }

    private String resolveKeyStoreFile() {
        if (!keyStoreFileProperty.isEmpty()) {
            return keyStoreFileProperty;
        }
        return keyStoreFile;
    }

    private String resolveId() {
        return id.isEmpty() ? beanName : id;
    }

    private String resolveHost() {
        if (!hostProperty.isEmpty()) {
            return hostProperty;
        } else if (host == null) {
            return SpringConstants.DEFAULT_HOST;
        }
        return host;
    }

    private int resolvePort() {
        if (!portProperty.isEmpty()) {
            return Integer.parseInt(portProperty);
        } else if (port == null) {
            return isHTTPS() == true ? 8443 : 8080;
        }
        return port;
    }

    private boolean resolveEnabled() {
        if (!enabledProperty.isEmpty()) {
            return Boolean.valueOf(enabledProperty);
        } else if (enabled == null) {
            return isHTTPS() ? false : true;
        }
        return enabled;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getEnabledProperty() {
        return enabledProperty;
    }

    public void setEnabledProperty(String enabledProperty) {
        this.enabledProperty = enabledProperty;
    }

    public String getPortProperty() {
        return portProperty;
    }

    public void setPortProperty(String portProperty) {
        this.portProperty = portProperty;
    }

    public String getHostProperty() {
        return hostProperty;
    }

    public void setHostProperty(String hostProperty) {
        this.hostProperty = hostProperty;
    }

    public String getSchemeProperty() {
        return schemeProperty;
    }

    public void setSchemeProperty(String schemeProperty) {
        this.schemeProperty = schemeProperty;
    }

    public String getKeyStoreFileProperty() {
        return keyStoreFileProperty;
    }

    public void setKeyStoreFileProperty(String keyStoreFileProperty) {
        this.keyStoreFileProperty = keyStoreFileProperty;
    }

    public String getKeyStorePassProperty() {
        return keyStorePassProperty;
    }

    public void setKeyStorePassProperty(String keyStorePassProperty) {
        this.keyStorePassProperty = keyStorePassProperty;
    }

    public String getCertPassProperty() {
        return certPassProperty;
    }

    public void setCertPassProperty(String certPassProperty) {
        this.certPassProperty = certPassProperty;
    }

    public String getBossThreadPoolSizeProperty() {
        return bossThreadPoolSizeProperty;
    }

    public void setBossThreadPoolSizeProperty(String bossThreadPoolSizeProperty) {
        this.bossThreadPoolSizeProperty = bossThreadPoolSizeProperty;
    }

    public String getWorkerThreadPoolSizeProperty() {
        return workerThreadPoolSizeProperty;
    }

    public void setWorkerThreadPoolSizeProperty(String workerThreadPoolSizeProperty) {
        this.workerThreadPoolSizeProperty = workerThreadPoolSizeProperty;
    }

    public String getExecHandlerThreadPoolSizeProperty() {
        return execHandlerThreadPoolSizeProperty;
    }

    public void setExecHandlerThreadPoolSizeProperty(String execHandlerThreadPoolSizeProperty) {
        this.execHandlerThreadPoolSizeProperty = execHandlerThreadPoolSizeProperty;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getKeyStoreFile() {
        return keyStoreFile;
    }

    public void setKeyStoreFile(String keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }

    public String getKeyStorePass() {
        return keyStorePass;
    }

    public void setKeyStorePass(String keyStorePass) {
        this.keyStorePass = keyStorePass;
    }

    public String getCertPass() {
        return certPass;
    }

    public void setCertPass(String certPass) {
        this.certPass = certPass;
    }

    public int getBossThreadPoolSize() {
        return bossThreadPoolSize;
    }

    public void setBossThreadPoolSize(int bossThreadPoolSize) {
        this.bossThreadPoolSize = bossThreadPoolSize;
    }

    public int getWorkerThreadPoolSize() {
        return workerThreadPoolSize;
    }

    public void setWorkerThreadPoolSize(int workerThreadPoolSize) {
        this.workerThreadPoolSize = workerThreadPoolSize;
    }

    public int getExecHandlerThreadPoolSize() {
        return execHandlerThreadPoolSize;
    }

    public void setExecHandlerThreadPoolSize(int execHandlerThreadPoolSize) {
        this.execHandlerThreadPoolSize = execHandlerThreadPoolSize;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public boolean getDefaultEnabled() {
        return isHTTPS() ? false : true;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public boolean isHTTPS() {
        return SpringConstants.HTTPS_TRANSPORT.equals(getScheme());
    }
}
