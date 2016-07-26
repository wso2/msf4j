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
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;

/**
 * Implementation class of JAX-RS client.
 */
public class MSF4JClient implements Client {

    @Override
    public void close() {

    }

    @Override
    public WebTarget target(String s) {
        return null;
    }

    @Override
    public WebTarget target(URI uri) {
        return null;
    }

    @Override
    public WebTarget target(UriBuilder uriBuilder) {
        return null;
    }

    @Override
    public WebTarget target(Link link) {
        return null;
    }

    @Override
    public Invocation.Builder invocation(Link link) {
        return null;
    }

    @Override
    public SSLContext getSslContext() {
        return null;
    }

    @Override
    public HostnameVerifier getHostnameVerifier() {
        return null;
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }

    @Override
    public Client property(String s, Object o) {
        return null;
    }

    @Override
    public Client register(Class<?> aClass) {
        return null;
    }

    @Override
    public Client register(Class<?> aClass, int i) {
        return null;
    }

    @Override
    public Client register(Class<?> aClass, Class<?>... classes) {
        return null;
    }

    @Override
    public Client register(Class<?> aClass, Map<Class<?>, Integer> map) {
        return null;
    }

    @Override
    public Client register(Object o) {
        return null;
    }

    @Override
    public Client register(Object o, int i) {
        return null;
    }

    @Override
    public Client register(Object o, Class<?>... classes) {
        return null;
    }

    @Override
    public Client register(Object o, Map<Class<?>, Integer> map) {
        return null;
    }
}
