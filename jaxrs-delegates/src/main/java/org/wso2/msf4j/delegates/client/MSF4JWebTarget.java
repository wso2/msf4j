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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

/**
 * Implementation class of JAX-RS WebTarget.
 */
// TODO: Complete the spec implementation
public class MSF4JWebTarget implements WebTarget {

    private Client client;
    private URI uri;
    private List<Object> providerComponents = new ArrayList<>();
    private static final String PATH_SEPARATOR = "/";

    /**
     * Constructor of the MSF4JWebTarget
     *
     * @param client             Client of the WebTarget
     * @param uri                URI of the WebTarget
     * @param providerComponents List of provider components to inherit
     */
    public MSF4JWebTarget(Client client, URI uri, List<Object> providerComponents) {
        this.client = client;
        this.uri = uri;
        this.providerComponents.addAll(providerComponents);
    }

    /**
     * Get the URI of the WebTarget.
     *
     * @return URI of the WebTarget
     */
    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public UriBuilder getUriBuilder() {
        throw new UnsupportedOperationException();
    }

    /**
     * Create a new WebTarget by appending the given path.
     *
     * @param path Path to append
     * @return New WebTarget with the appended path
     */
    @Override
    public WebTarget path(String path) {
        if (!path.isEmpty()) {
            return new MSF4JWebTarget(client, uri.resolve((path.startsWith(PATH_SEPARATOR)) ? path : PATH_SEPARATOR +
                    path), providerComponents);
        } else {
            return new MSF4JWebTarget(client, uri, providerComponents);
        }
    }

    @Override
    public WebTarget resolveTemplate(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebTarget resolveTemplate(String name, Object value, boolean encodeSlashInPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebTarget resolveTemplateFromEncoded(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebTarget resolveTemplates(Map<String, Object> templateValues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebTarget resolveTemplates(Map<String, Object> templateValues, boolean encodeSlashInPath) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebTarget resolveTemplatesFromEncoded(Map<String, Object> templateValues) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebTarget matrixParam(String name, Object... values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebTarget queryParam(String name, Object... values) {
        throw new UnsupportedOperationException();
    }

    /**
     * Create an Invocation.Builder from the WebTarget.
     *
     * @return Invocation.Builder instance
     */
    @Override
    public Invocation.Builder request() {
        return this.request(MediaType.WILDCARD_TYPE);
    }

    /**
     * Create an Invocation.Builder from the WebTarget with the given array of accepted response types.
     *
     * @param acceptedResponseTypes Array of accepted response types as Strings
     * @return Invocation.Builder instance
     */
    @Override
    public Invocation.Builder request(String... acceptedResponseTypes) {
        return new MSF4JInvocation.Builder(new MSF4JClientRequestContext(client, uri)
                .setAcceptResponseTypesStr(Arrays.asList(acceptedResponseTypes)), providerComponents);
    }

    /**
     * Create an Invocation.Builder from the WebTarget with the given array of accepted response types.
     *
     * @param acceptedResponseTypes Array of accepted response types as MediaTypes
     * @return Invocation.Builder instance
     */
    @Override
    public Invocation.Builder request(MediaType... acceptedResponseTypes) {
        return new MSF4JInvocation.Builder(new MSF4JClientRequestContext(client, uri)
                .setAcceptResponseTypes(Arrays.asList(acceptedResponseTypes)), providerComponents);
    }

    @Override
    public Configuration getConfiguration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebTarget property(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebTarget register(Class<?> componentClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebTarget register(Class<?> componentClass, int priority) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebTarget register(Class<?> componentClass, Class<?>... contracts) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebTarget register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebTarget register(Object component) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebTarget register(Object component, int priority) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebTarget register(Object component, Class<?>... contracts) {
        throw new UnsupportedOperationException();
    }

    @Override
    public WebTarget register(Object component, Map<Class<?>, Integer> contracts) {
        throw new UnsupportedOperationException();
    }
}
