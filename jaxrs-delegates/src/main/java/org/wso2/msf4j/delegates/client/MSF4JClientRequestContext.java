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

import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * Implementation class of JAX-RS ClientRequestContext.
 */
// TODO: Complete the spec implementation
public class MSF4JClientRequestContext implements ClientRequestContext {

    private Client client;
    private URI uri;
    private String httpMethod;
    private MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
    private List<MediaType> acceptResponseTypes;
    private MediaType mediaType;
    private Object entity;

    /**
     * Constructor of MSF4JClientRequestContext.
     *
     * @param client Client instance that created the WebTarget that created the MSF4JClientRequestContext
     * @param uri    URI of the WebTarget that create the MSF4JClientRequestContext
     */
    public MSF4JClientRequestContext(Client client, URI uri) {
        this.client = client;
        this.uri = uri;
    }

    /**
     * Set accept media types of the request.
     *
     * @param acceptResponseTypes List of media types as MediaType instances
     * @return This MSF4JClientRequestContext instance
     */
    public MSF4JClientRequestContext setAcceptResponseTypes(List<MediaType> acceptResponseTypes) {
        this.acceptResponseTypes = acceptResponseTypes;
        return this;
    }

    /**
     * Set accept media types of the request.
     *
     * @param acceptResponseTypes List of media types as Strings
     * @return This MSF4JClientRequestContext instance
     */
    public MSF4JClientRequestContext setAcceptResponseTypesStr(List<String> acceptResponseTypes) {
        this.acceptResponseTypes = acceptResponseTypes.stream()
                .map(MediaType::valueOf).collect(Collectors.toList());
        return this;
    }

    /**
     * Set media type of the request.
     *
     * @param mediaType MediaType of the request
     * @return This MSF4JClientRequestContext instance
     */
    public MSF4JClientRequestContext setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    @Override
    public Object getProperty(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<String> getPropertyNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setProperty(String name, Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeProperty(String name) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the URI of the ClientRequestContext.
     *
     * @return URI of the ClientRequestContext
     */
    @Override
    public URI getUri() {
        return uri;
    }

    /**
     * Set the URI of ClientRequestContext.
     *
     * @param uri New URI
     */
    @Override
    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     * Get the HTTP request method.
     *
     * @return HTTP request method as a string
     */
    @Override
    public String getMethod() {
        return httpMethod;
    }

    /**
     * Set HTTP request method.
     *
     * @param method HTTP request method as a string
     */
    @Override
    public void setMethod(String method) {
        if (HttpMethod.GET.equals(method)) {
            httpMethod = HttpMethod.GET;
        } else if (HttpMethod.POST.equals(method)) {
            httpMethod = HttpMethod.POST;
        } else if (HttpMethod.PUT.equals(method)) {
            httpMethod = HttpMethod.PUT;
        } else if (HttpMethod.DELETE.equals(method)) {
            httpMethod = HttpMethod.DELETE;
        } else {
            throw new RuntimeException("Unexpected HttpMethod");
        }
    }

    /**
     * Get headers of the ClientRequestContext.
     *
     * @return HTTP headers
     */
    @Override
    public MultivaluedMap<String, Object> getHeaders() {
        return headers;
    }

    @Override
    public MultivaluedMap<String, String> getStringHeaders() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getHeaderString(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getDate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Locale getLanguage() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get media type of the ClientRequestContext.
     *
     * @return Media type
     */
    @Override
    public MediaType getMediaType() {
        return mediaType;
    }

    /**
     * Get acceptable media types of the ClientRequestContext.
     *
     * @return List of Media types
     */
    @Override
    public List<MediaType> getAcceptableMediaTypes() {
        return acceptResponseTypes;
    }

    @Override
    public List<Locale> getAcceptableLanguages() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Cookie> getCookies() {
        throw new UnsupportedOperationException();
    }

    /**
     * Check whether the ClientRequestContext has an entity.
     */
    @Override
    public boolean hasEntity() {
        return entity != null;
    }

    /**
     * Get the entity of the ClientRequestContext.
     *
     * @return Entity of the ClientRequestContext
     */
    @Override
    public Object getEntity() {
        return entity;
    }

    @Override
    public Class<?> getEntityClass() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type getEntityType() {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the entity of ClientRequestContext.
     *
     * @param entity Entity object
     */
    @Override
    public void setEntity(Object entity) {
        this.entity = entity;
    }

    @Override
    public void setEntity(Object entity, Annotation[] annotations, MediaType mediaType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Annotation[] getEntityAnnotations() {
        throw new UnsupportedOperationException();
    }

    @Override
    public OutputStream getEntityStream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEntityStream(OutputStream outputStream) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the Client of the ClientRequestContext.
     *
     * @return Client instance
     */
    @Override
    public Client getClient() {
        return client;
    }

    @Override
    public Configuration getConfiguration() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void abortWith(Response response) {
        throw new UnsupportedOperationException();
    }
}
