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

package org.wso2.msf4j.websocket.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.websocket.WebSocketEndpoint;

import java.io.IOException;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * This is a Sample class for WebSocket.
 */
@ServerEndpoint("/echo")
public class EchoEndpoint implements WebSocketEndpoint {
    private static final Logger log = LoggerFactory.getLogger(EchoEndpoint.class);

    @OnOpen
    public void onOpen(Session session) {
        log.info(session.getId() + " connected to repeat-app");
    }

    /**
     * Echo the same {@link String} to user.
     */
    @OnMessage
    public String onTextMessage(@PathParam("name") String name, String text, Session session) throws IOException {
        log.info("Received Text : " + text + " from  " + session.getId());
        return text;
    }

    /**
     * Echo the same ByteBuffer to user.
     */
    @OnMessage
    public byte[] onBinaryMessage(byte[] buffer, Session session) {
        String values = "";
        int bufferLength = buffer.length;
        byte[] bytes = new byte[buffer.length];
        for (int i = 0; i < buffer.length; i++) {
            byte b = buffer[i];
            values = values.concat(" " + b);
        }
        log.info("Binary message values from " + session.getId() + System.lineSeparator() +
                         "buffer length: " + bufferLength + System.lineSeparator() + "values : " + values);
        return buffer;
    }

    @OnMessage
    public PongMessage onPongMessage(PongMessage pongMessage, Session session) {
        log.info("Received a pong message.");
        return pongMessage;
    }

    @OnClose
    public void onClose(CloseReason closeReason, Session session) {
        log.info("Connection is closed with status code: " + closeReason.getCloseCode().getCode()
                            + " On reason " + closeReason.getReasonPhrase());
    }

}
