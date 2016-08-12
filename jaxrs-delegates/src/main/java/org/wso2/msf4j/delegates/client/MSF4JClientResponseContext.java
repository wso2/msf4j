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

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

/**
 * Implementation class of JAX-RS ClientResponseContext.
 */
// TODO: Complete the spec implementation
public class MSF4JClientResponseContext implements ClientResponseContext {

    private InputStream entityStream;
    private int statusCode;
    private MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();

    /**
     * Constructor of MSF4JClientResponseContext.
     */
    public MSF4JClientResponseContext(CloseableHttpResponse response) throws IOException {
        this.statusCode = response.getStatusLine().getStatusCode();
        for (Header header : response.getAllHeaders()) {
            headers.add(header.getName(), header.getValue());
        }
        this.entityStream = response.getEntity().getContent();
    }

    /**
     * Get the status of the response.
     *
     * @return Status of the response
     */
    @Override
    public int getStatus() {
        return statusCode;
    }

    /**
     * Modify the status of the response. Can be used inside filters etc.
     *
     * @param code New status code
     */
    @Override
    public void setStatus(int code) {
        this.statusCode = code;
    }

    @Override
    public Response.StatusType getStatusInfo() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setStatusInfo(Response.StatusType statusInfo) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the headers of the response.
     *
     * @return Response headers
     */
    @Override
    public MultivaluedMap<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String getHeaderString(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getAllowedMethods() {
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

    @Override
    public int getLength() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MediaType getMediaType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, NewCookie> getCookies() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityTag getEntityTag() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Date getLastModified() {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI getLocation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Link> getLinks() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasLink(String relation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Link getLink(String relation) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Link.Builder getLinkBuilder(String relation) {
        throw new UnsupportedOperationException();
    }

    /**
     * CHeck whether the response has an entity.
     *
     * @return Response entity
     */
    @Override
    public boolean hasEntity() {
        return entityStream != null;
    }

    /**
     * Get the entity stream of the response.
     *
     * @return Entity of the response
     */
    @Override
    public InputStream getEntityStream() {
        return entityStream;
    }

    /**
     * Modify the entity stream of the ClientResponseContext.
     *
     * @param input New entity stream
     */
    @Override
    public void setEntityStream(InputStream input) {
        this.entityStream = input;
    }
}
