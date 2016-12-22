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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.websocket.WebSocketCarbonMessage;
import org.wso2.carbon.messaging.websocket.WebSocketResponder;
import org.wso2.carbon.transport.http.netty.common.Constants;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.Session;

/**
 * Internal session manager for WebSocket messages
 */
public class SessionManager {

    Logger log = LoggerFactory.getLogger(SessionManager.class);

    private static SessionManager sessionManager = new SessionManager();
    private final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        return sessionManager;
    }

    /**
     * @param webSocketCarbonMessage incoming {@link WebSocketCarbonMessage}
     * @return requested {@link Session} for given channel
     */
    public Session getSession(WebSocketCarbonMessage webSocketCarbonMessage) {
        String sessionId = getSessionId(webSocketCarbonMessage);
        if (sessionMap.containsKey(sessionId)) {
            return sessionMap.get(sessionId);
        } else {
            return createSession(webSocketCarbonMessage);
        }
    }

    /**
     * This method creates session for a given channel
     * Here unlike http channel ID was taken as the session ID
     * @param webSocketCarbonMessage incoming {@link WebSocketCarbonMessage}
     * @return created {@link Session} for given channel
     */
    public Session createSession(WebSocketCarbonMessage webSocketCarbonMessage) {
        String sessionId = getSessionId(webSocketCarbonMessage);
        WebSocketResponder webSocketResponder = webSocketCarbonMessage.getWebSocketResponder();
        URI uri = (URI) webSocketCarbonMessage.getProperty(Constants.TO);
        SessionImpl session = new SessionImpl(webSocketResponder, sessionId, uri, true, true, null, null, null);
        sessionMap.put(sessionId, session);
        log.info("Session created for channel " + sessionId);
        return session;
    }

    public boolean containsSession(WebSocketCarbonMessage webSocketCarbonMessage) {
        String sessionId = getSessionId(webSocketCarbonMessage);
        return sessionMap.containsKey(sessionId);
    }

    /**
     * Close the channel for given session and remove session from the session manager
     * @param webSocketCarbonMessage
     */
    public void removeSession(WebSocketCarbonMessage webSocketCarbonMessage) throws IOException {
        String sessionId = getSessionId(webSocketCarbonMessage);
        if (sessionMap.containsKey(sessionId)) {
            sessionMap.remove(sessionId).close();
            log.info("Removed session ID for channel " + sessionId);
        } else {
            log.info("There is no session created to remove for channel " + sessionId);
        }
    }


    private String getSessionId(WebSocketCarbonMessage webSocketCarbonMessage) {
        return (String) webSocketCarbonMessage.getProperty(Constants.CHANNEL_ID);
    }


}
