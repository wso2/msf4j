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

import java.security.KeyStore;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Configuration;

/**
 * Implementation class of JAX-RS client builder.
 */
// TODO: Complete the spec implementation
public class MSF4JClientBuilder extends ClientBuilder {

    @Override
    public ClientBuilder withConfig(Configuration configuration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClientBuilder sslContext(SSLContext sslContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClientBuilder keyStore(KeyStore keyStore, char[] chars) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClientBuilder trustStore(KeyStore keyStore) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClientBuilder hostnameVerifier(HostnameVerifier hostnameVerifier) {
        throw new UnsupportedOperationException();
    }

    /**
     * Create a new Client instance.
     *
     * @return Client instance
     */
    @Override
    public Client build() {
        return new MSF4JClient();
    }

    @Override
    public Configuration getConfiguration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClientBuilder property(String s, Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClientBuilder register(Class<?> aClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClientBuilder register(Class<?> aClass, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClientBuilder register(Class<?> aClass, Class<?>... classes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClientBuilder register(Class<?> aClass, Map<Class<?>, Integer> map) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClientBuilder register(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClientBuilder register(Object o, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClientBuilder register(Object o, Class<?>... classes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClientBuilder register(Object o, Map<Class<?>, Integer> map) {
        throw new UnsupportedOperationException();
    }
}
