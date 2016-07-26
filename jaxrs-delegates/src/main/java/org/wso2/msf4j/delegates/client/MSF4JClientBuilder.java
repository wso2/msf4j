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

package org.wso2.msf4j.delegates.client;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Configuration;
import java.security.KeyStore;
import java.util.Map;

/**
 * Implementation class of JAX-RS client builder.
 */
public class MSF4JClientBuilder extends ClientBuilder {

    @Override
    public ClientBuilder withConfig(Configuration configuration) {
        return null;
    }

    @Override
    public ClientBuilder sslContext(SSLContext sslContext) {
        return null;
    }

    @Override
    public ClientBuilder keyStore(KeyStore keyStore, char[] chars) {
        return null;
    }

    @Override
    public ClientBuilder trustStore(KeyStore keyStore) {
        return null;
    }

    @Override
    public ClientBuilder hostnameVerifier(HostnameVerifier hostnameVerifier) {
        return null;
    }

    @Override
    public Client build() {
        return null;
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }

    @Override
    public ClientBuilder property(String s, Object o) {
        return null;
    }

    @Override
    public ClientBuilder register(Class<?> aClass) {
        return null;
    }

    @Override
    public ClientBuilder register(Class<?> aClass, int i) {
        return null;
    }

    @Override
    public ClientBuilder register(Class<?> aClass, Class<?>... classes) {
        return null;
    }

    @Override
    public ClientBuilder register(Class<?> aClass, Map<Class<?>, Integer> map) {
        return null;
    }

    @Override
    public ClientBuilder register(Object o) {
        return null;
    }

    @Override
    public ClientBuilder register(Object o, int i) {
        return null;
    }

    @Override
    public ClientBuilder register(Object o, Class<?>... classes) {
        return null;
    }

    @Override
    public ClientBuilder register(Object o, Map<Class<?>, Integer> map) {
        return null;
    }
}
