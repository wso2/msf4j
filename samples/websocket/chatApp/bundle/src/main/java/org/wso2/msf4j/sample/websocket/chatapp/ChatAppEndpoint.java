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

package org.wso2.msf4j.sample.websocket.chatapp;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.websocket.WebSocketEndpoint;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * This is a Sample class for WebSocket.
 * This provides a chat with multiple users.
 */
@Component(
        name = "org.wso2.msf4j.chatApp",
        service = WebSocketEndpoint.class,
        immediate = true
)
@ServerEndpoint("/chat/{name}")
public class ChatAppEndpoint implements WebSocketEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatAppEndpoint.class);
    /* Stores all the active sessions in a list */
    private List<WebSocketConnection> webSocketConnections = new ArrayList<>();

    @OnOpen
    public void onOpen(@PathParam("name") String name, WebSocketConnection webSocketConnection) {
        webSocketConnections.add(webSocketConnection);
        String msg = name + " connected to chat";
        LOGGER.info(msg);
        sendMessageToAll(msg);
    }

    @OnMessage
    public void onTextMessage(@PathParam("name") String name, String text, WebSocketConnection webSocketConnection)
            throws IOException {
        String msg = name + " : " + text;
        LOGGER.info("Received Text : " + text + " from  " + name + webSocketConnection.getChannelId());
        sendMessageToAll(msg);
    }

    @OnClose
    public void onClose(@PathParam("name") String name, CloseReason closeReason,
                        WebSocketConnection webSocketConnection) {
        LOGGER.info("Connection is closed with status code : " + closeReason.getCloseCode().getCode()
                            + " On reason " + closeReason.getReasonPhrase());
        webSocketConnections.remove(webSocketConnection);
        String msg = name + " left the chat";
        sendMessageToAll(msg);
    }

    @OnError
    public void onError(Throwable throwable) {
        LOGGER.error("Error found in method : " + throwable.toString());
    }


    private void sendMessageToAll(String message) {
        webSocketConnections.forEach(
                webSocketConnection -> {
                    webSocketConnection.pushText(message);
                }
        );
    }
}
