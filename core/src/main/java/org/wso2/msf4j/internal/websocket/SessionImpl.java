/*
 *   Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.msf4j.internal.websocket;

import org.wso2.carbon.messaging.websocket.CloseWebSocketMessage;
import org.wso2.carbon.messaging.websocket.WebSocketResponder;

import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.websocket.CloseReason;
import javax.websocket.Extension;
import javax.websocket.MessageHandler;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

/**
 * {@link Session} implementation of MSF4J WebSocket
 */
public class SessionImpl implements Session {

    private final String sessionId;
    private final URI uri;
    private final boolean isSecure;
    private final boolean isOpen;
    private final RemoteEndpoint.Basic basicRemoteEndpoint;
    private final WebSocketResponder webSocketResponder;
    private long maxIdleTimeout = 1000;
    private int maxBinaryMessageBufferSize = 8940;
    private int maxTextMessageBufferSize = 8940;
    private String protocolVersion;
    private String negotiatedSubProtocol;
    private List<Extension> negotiatedExtensionList;


    public SessionImpl(WebSocketResponder webSocketResponder, String sessionId, URI uri,
                       boolean isSecure, boolean isOpen, String negotiatedSubProtocol,
                       String protocolVersion, List<Extension> negotiatedExtensionList) {
        this.webSocketResponder = webSocketResponder;
        this.sessionId = sessionId;
        this.uri = uri;
        this.isSecure = isSecure;
        this.isOpen = isOpen;
        this.basicRemoteEndpoint = new BasicRemoteEndpoint(webSocketResponder);
        this.negotiatedSubProtocol = negotiatedSubProtocol;
        this.protocolVersion = protocolVersion;
        this.negotiatedExtensionList = negotiatedExtensionList;
    }

    @Override
    public WebSocketContainer getContainer() {
        //TODO : Should this be implemented?
        return null;
    }

    @Override
    public void addMessageHandler(MessageHandler messageHandler) throws IllegalStateException {
        //TODO : Implementation? THis is needed for Api Class implementation. Not for annotations
    }

    @Override
    public <T> void addMessageHandler(Class<T> aClass, MessageHandler.Whole<T> whole) {
        //TODO : Implementation? THis is needed for Api Class implementation. Not for annotations
    }

    @Override
    public <T> void addMessageHandler(Class<T> aClass, MessageHandler.Partial<T> partial) {
        //TODO : Implementation? THis is needed for Api Class implementation. Not for annotations
    }

    @Override
    public Set<MessageHandler> getMessageHandlers() {
        //TODO : Implementation? THis is needed for Api Class implementation. Not for annotations
        return null;
    }

    @Override
    public void removeMessageHandler(MessageHandler messageHandler) {
        //TODO : Implementation? THis is needed for Api Class implementation. Not for annotations
    }

    @Override
    public String getProtocolVersion() {
        return protocolVersion;
    }

    @Override
    public String getNegotiatedSubprotocol() {
        return negotiatedSubProtocol;
    }

    @Override
    public List<Extension> getNegotiatedExtensions() {
        return negotiatedExtensionList;
    }

    @Override
    public boolean isSecure() {
        return isSecure;
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public long getMaxIdleTimeout() {
        return maxIdleTimeout;
    }

    @Override
    public void setMaxIdleTimeout(long l) {
        this.maxIdleTimeout = l;
    }

    @Override
    public void setMaxBinaryMessageBufferSize(int i) {
        this.maxBinaryMessageBufferSize = i;
    }

    @Override
    public int getMaxBinaryMessageBufferSize() {
        return maxBinaryMessageBufferSize;
    }

    @Override
    public void setMaxTextMessageBufferSize(int i) {
        this.maxTextMessageBufferSize = i;
    }

    @Override
    public int getMaxTextMessageBufferSize() {
        return maxTextMessageBufferSize;
    }

    @Override
    public RemoteEndpoint.Async getAsyncRemote() {
        //TODO : Implementation
        return null;
    }

    @Override
    public RemoteEndpoint.Basic getBasicRemote() {
        return basicRemoteEndpoint;
    }

    @Override
    public String getId() {
        return sessionId;
    }

    @Override
    public void close() throws IOException {
        webSocketResponder.pushToClient(new CloseWebSocketMessage(
                CloseReason.CloseCodes.GOING_AWAY.getCode(),
                null,
                null
        ));
    }

    @Override
    public void close(CloseReason closeReason) throws IOException {
        webSocketResponder.pushToClient(new CloseWebSocketMessage(
                closeReason.getCloseCode().getCode(),
                closeReason.getReasonPhrase(),
                null
        ));
    }

    @Override
    public URI getRequestURI() {
        return uri;
    }

    @Override
    public Map<String, List<String>> getRequestParameterMap() {
        //TODO : Implementation
        return null;
    }

    @Override
    public String getQueryString() {
        //TODO : Implementation
        return null;
    }

    @Override
    public Map<String, String> getPathParameters() {
        //TODO : Implementation
        return null;
    }

    @Override
    public Map<String, Object> getUserProperties() {
        //TODO : Implementation
        return null;
    }

    @Override
    public Principal getUserPrincipal() {
        //TODO : Implementation
        return null;
    }

    @Override
    public Set<Session> getOpenSessions() {
        //TODO : Implementation
        return null;
    }
}
