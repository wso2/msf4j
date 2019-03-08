/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.msf4j;

import io.netty.buffer.ByteBuf;
import org.wso2.msf4j.internal.HttpHeadersImpl;
import org.wso2.msf4j.internal.MSF4JConstants;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;
import org.wso2.transport.http.netty.message.HttpMessageDataStreamer;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.core.HttpHeaders;

/**
 * Class that represents an HTTP request in MSF4J level.
 */
public class Request {

    private final HttpCarbonMessage httpCarbonMessage;
    private List<String> acceptTypes = null;
    private String contentType = null;
    private SessionManager sessionManager;
    private Session session;

    public Request(HttpCarbonMessage httpCarbonMessage) {
        this.httpCarbonMessage = httpCarbonMessage;
        // find accept types
        String acceptHeaderStr = httpCarbonMessage.getHeader(javax.ws.rs.core.HttpHeaders.ACCEPT);
        acceptTypes = (acceptHeaderStr != null) ?
                Arrays.stream(acceptHeaderStr.split("\\s*,\\s*"))
                        .map(mediaType -> mediaType.split("\\s*;\\s*")[0])
                        .collect(Collectors.toList()) :
                null;
        //find content type
        String contentTypeHeaderStr = httpCarbonMessage.getHeader(javax.ws.rs.core.HttpHeaders.CONTENT_TYPE);
        //Trim specified charset since UTF-8 is assumed
        contentType = (contentTypeHeaderStr != null) ? contentTypeHeaderStr.split("\\s*;\\s*")[0] : null;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * @return true if the request does not have body content
     */
    public boolean isEmpty() {
        return httpCarbonMessage.isEmpty();
    }

    /**
     * @return next available message body chunk
     */
    @Deprecated
    public ByteBuf getMessageBody() {
        return httpCarbonMessage.getMessageBody();
    }

    /**
     * @return map of headers of the HTTP request
     */
    public HttpHeaders getHeaders() {
        return new HttpHeadersImpl(httpCarbonMessage.getHeaders());
    }

    /**
     * Get an HTTP header of the HTTP request.
     *
     * @param key name of the header
     * @return value of the header
     */
    public String getHeader(String key) {
        return httpCarbonMessage.getHeader(key);
    }

    /**
     * Set a property in the underlining Carbon Message.
     *
     * @param key property key
     * @return value of the property key
     */
    public Object getProperty(String key) {
        return httpCarbonMessage.getProperty(key);
    }

    /**
     * @return property map of the underlining CarbonMessage
     */
    public Map<String, Object> getProperties() {
        return httpCarbonMessage.getProperties();
    }

    /**
     * Set a property in the underlining Carbon Message.
     *
     * @param key   property key
     * @param value property value
     */
    public void setProperty(String key, Object value) {
        httpCarbonMessage.setProperty(key, value);
    }

    /**
     * Remove a property from the underlining CarbonMessage object.
     *
     * @param key property key
     */
    public void removeProperty(String key) {
        httpCarbonMessage.removeProperty(key);
    }

    /**
     * @return URL of the request.
     */
    public String getUri() {
        return (String) httpCarbonMessage.getProperty(Constants.TO);
    }

    /**
     * @return HTTP method of the request.
     */
    public String getHttpMethod() {
        return (String) httpCarbonMessage
                .getProperty(org.wso2.transport.http.netty.contract.Constants.HTTP_METHOD);
    }

    /**
     * @return accept type of the request.
     */
    public List<String> getAcceptTypes() {
        return acceptTypes;
    }

    /**
     * @return request body content type.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Returns the current session associated with this request, or if the request does not have a session,
     * creates one.
     *
     * @return Session
     */
    public Session getSession() {
        if (sessionManager == null) {
            throw new IllegalStateException("SessionManager has not been set");
        }
        if (session != null) {
            return session.setAccessed();
        }
        String cookieHeader = getHeader("Cookie");
        if (cookieHeader != null) {
            session = Arrays.stream(cookieHeader.split(";")).map(String::trim)
                    .filter(cookie -> cookie.startsWith(MSF4JConstants.SESSION_ID))
                    .findFirst()
                    .map(jsession -> sessionManager.getSession(jsession.substring(MSF4JConstants.SESSION_ID.length())))
                    .orElseGet(sessionManager::createSession);
            return session.setAccessed();
        }
        return session = sessionManager.createSession();
    }

    /**
     * Returns the current HttpSession associated with this request or, if there is no current session and create is
     * true, returns a new session.
     *
     * @param create Create a new session or not
     * @return Session
     */
    public Session getSession(boolean create) {
        if (sessionManager == null) {
            throw new IllegalStateException("SessionManager has not been set");
        }
        if (session != null) {
            return session.setAccessed();
        }
        String cookieHeader = getHeader("Cookie");
        if (cookieHeader != null) {
            session = Arrays.stream(cookieHeader.split(";")).map(String::trim)
                    .filter(cookie -> cookie.startsWith(MSF4JConstants.SESSION_ID))
                    .findFirst()
                    .map(jsession -> sessionManager.getSession(jsession.substring(MSF4JConstants.SESSION_ID.length())))
                    .orElseGet(() -> {
                        if (create) {
                            return sessionManager.createSession();
                        }
                        return null;
                    });
            return session.setAccessed();
        } else if (create) {
            return session = sessionManager.createSession();
        }
        return null;
    }

    Session getSessionInternal() {
        return session;
    }

    /**
     * Get underlying HttpCarbonMessage.
     *
     * @return HttpCarbonMessage instance of the Request
     */
    HttpCarbonMessage getHttpCarbonMessage() {
        return httpCarbonMessage;
    }

    /**
     * Method use to send the response to the caller.
     *
     * @param carbonMessage Response message
     * @return true if no errors found, else otherwise
     * @throws ServerConnectorException server connector exception.
     */
    public boolean respond(HttpCarbonMessage carbonMessage) throws ServerConnectorException {
        HttpResponseFuture statusFuture = httpCarbonMessage.respond(carbonMessage);
        return statusFuture.getStatus().getCause() == null;
    }

    /**
     * Returns InputStream of the ByteBuffers in message content.
     *
     * @return InputStream of the ByteBuffers
     */
    public InputStream getMessageContentStream() {
        return new HttpMessageDataStreamer(httpCarbonMessage).getInputStream();
    }
}
