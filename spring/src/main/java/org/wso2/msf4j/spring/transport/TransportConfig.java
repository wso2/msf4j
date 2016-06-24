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
public abstract class TransportConfig {

    private String id;
    private boolean enabled;
    private int port;
    private String host;
    private String scheme;
    private String keyStoreFile;
    private String keyStorePass;
    private String certPass;
    private int bossThreadPoolSize;
    private int workerThreadPoolSize;
    private int execHandlerThreadPoolSize;
    private Map<String, String> parameters = new HashMap<>();

    @Autowired
    Environment env;

    @PostConstruct
    public void init() {
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

    public String getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getScheme() {
        return scheme;
    }

    public String getKeyStoreFile() {
        return keyStoreFile;
    }

    public String getKeyStorePass() {
        return keyStorePass;
    }

    public String getCertPass() {
        return certPass;
    }

    public int getBossThreadPoolSize() {
        return bossThreadPoolSize;
    }

    public int getWorkerThreadPoolSize() {
        return workerThreadPoolSize;
    }

    public int getExecHandlerThreadPoolSize() {
        return execHandlerThreadPoolSize;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public void setKeyStoreFile(String keyStoreFile) {
        this.keyStoreFile = keyStoreFile;
    }

    public void setKeyStorePass(String keyStorePass) {
        this.keyStorePass = keyStorePass;
    }

    public void setCertPass(String certPass) {
        this.certPass = certPass;
    }

    public void setBossThreadPoolSize(int bossThreadPoolSize) {
        this.bossThreadPoolSize = bossThreadPoolSize;
    }

    public void setWorkerThreadPoolSize(int workerThreadPoolSize) {
        this.workerThreadPoolSize = workerThreadPoolSize;
    }

    public void setExecHandlerThreadPoolSize(int execHandlerThreadPoolSize) {
        this.execHandlerThreadPoolSize = execHandlerThreadPoolSize;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
