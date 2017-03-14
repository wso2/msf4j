/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.msf4j.websocket.endpoint.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * This is a Sample class for WebSocket.
 * This provides a chat with multiple users.
 */

@ServerEndpoint(value = "/chat/{name}")
public class TestEndpointWithOnError {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestEndpointWithOnError.class);
    private List<Session> sessions = new LinkedList<Session>();

    @OnOpen
    public void onOpen(@PathParam("name") String name, Session session) {
        sessions.add(session);
        String msg = name + " connected to chat";
        LOGGER.info(msg);
        sendMessageToAll(msg);
    }

    @OnMessage
    public void onTextMessage(@PathParam("name") String name, String text, Session session) throws IOException {
        String msg = name + ":" + text;
        LOGGER.info("Received Text: " + text + " from  " + name + session.getId());
        sendMessageToAll(msg);
    }

    @OnClose
    public void onClose(@PathParam("name") String name, CloseReason closeReason, Session session) {
        LOGGER.info("Connection is closed with status code: " + closeReason.getCloseCode().getCode()
                            + " On reason " + closeReason.getReasonPhrase());
        sessions.remove(session);
        String msg = name + " left the chat";
        sendMessageToAll(msg);
    }

    @OnError
    public void onError(Throwable throwable, Session session, String errorValue) {
        LOGGER.error("Error found in method: " + throwable.toString());
    }

    private void sendMessageToAll(String message) {
        sessions.forEach(
                session -> {
                    try {
                        session.getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        LOGGER.error(e.toString());
                    }
                }
        );
    }
}
