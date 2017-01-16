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
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.Session;

/**
 * Internal session manager for WebSocket messages
 * @since 1.0.0
 */
public class SessionManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionManager.class);

    private static SessionManager sessionManager = new SessionManager();
    private Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        return sessionManager;
    }

    /**
     * @param carbonMessage incoming WebSocketMessage
     * @return requested {@link Session} for given channel
     */
    public Session getSession(CarbonMessage carbonMessage) {
        String sessionId = getSessionId(carbonMessage);
        if (sessionMap.containsKey(sessionId)) {
            return sessionMap.get(sessionId);
        } else {
            return null;
        }
    }

    /**
     * This method creates session for a given channel.
     * Here unlike http channel ID was taken as the session ID.
     * @param carbonMessage Request carbon Message.
     * @return Created new {@link Session}.
     * @throws URISyntaxException throws if URI syntax is wrong.
     */
    public Session add(CarbonMessage carbonMessage) throws URISyntaxException {
        Session session = (Session) carbonMessage.getProperty(Constants.WEBSOCKET_SESSION);
        sessionMap.put(getSessionId(carbonMessage), session);
        LOGGER.info("Added session for channel " + session.getId());
        return session;
    }

    /**
     * Checks whether the session is contained in the session manager.
     * @param webSocketMessage incoming {@link CarbonMessage} with websocket details.
     * @return true if the session is in the {@link SessionManager}.
     */
    public boolean containsSession(CarbonMessage webSocketMessage) {
        String sessionId = getSessionId(webSocketMessage);
        return sessionMap.containsKey(sessionId);
    }

    /**
     * Close the channel for given session.
     * Remove session from the session manager.
     * @param carbonMessage {@link CarbonMessage} which includes the session ID.
     */
    public void removeSession(CarbonMessage carbonMessage) throws IOException {
        String sessionId = getSessionId(carbonMessage);
        if (sessionMap.containsKey(sessionId)) {
            sessionMap.remove(sessionId).close();
            LOGGER.info("Removed session ID for channel " + sessionId);
        } else {
            LOGGER.info("There is no session created to remove for channel " + sessionId);
        }
    }


    private String getSessionId(CarbonMessage carbonMessage) {
        return (String) carbonMessage.getProperty(Constants.CHANNEL_ID);
    }


}
