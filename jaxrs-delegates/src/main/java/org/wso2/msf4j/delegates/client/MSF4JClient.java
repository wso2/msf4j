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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;

/**
 * Implementation class of JAX-RS client.
 */
// TODO: Complete the spec implementation
public class MSF4JClient implements Client {

    private List<Object> providerComponents = new ArrayList<>();

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    /**
     * Create a WebTarget from URL string.
     *
     * @param s URI string of the WeTarget
     * @return WebTarget instance
     */
    @Override
    public WebTarget target(String s) {
        return this.target(URI.create(s));
    }

    /**
     * Create a WebTarget from URL.
     *
     * @param uri URI of the WeTarget
     * @return WebTarget instance
     */
    @Override
    public WebTarget target(URI uri) {
        return new MSF4JWebTarget(this, uri, providerComponents);
    }

    @Override
    public WebTarget target(UriBuilder uriBuilder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebTarget target(Link link) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Invocation.Builder invocation(Link link) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SSLContext getSslContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Configuration getConfiguration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Client property(String s, Object o) {
        throw new UnsupportedOperationException();
    }

    /**
     * Register a provider class that is supported by the Runtime.
     *
     * @param aClass Provider class that is supported by the Runtime
     * @return This Client instance
     */
    @Override
    public Client register(Class<?> aClass) {
        try {
            providerComponents.add(aClass.newInstance());
            return this;
        } catch (InstantiationException e) {
            throw new RuntimeException("Failed to initialize provider", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access provider implementation", e);
        }
    }

    @Override
    public Client register(Class<?> aClass, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Client register(Class<?> aClass, Class<?>... classes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Client register(Class<?> aClass, Map<Class<?>, Integer> map) {
        throw new UnsupportedOperationException();
    }

    /**
     * Register an instance of a provider class that is supported by the Runtime.
     *
     * @param o Instance of the provider class that is supported by the Runtime
     * @return This Client instance
     */
    @Override
    public Client register(Object o) {
        providerComponents.add(o);
        return this;
    }

    @Override
    public Client register(Object o, int i) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Client register(Object o, Class<?>... classes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Client register(Object o, Map<Class<?>, Integer> map) {
        throw new UnsupportedOperationException();
    }
}
