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

package org.wso2.msf4j.sample.websocket.echoserver;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.websocket.WebSocketEndpoint;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * This is a Sample class for WebSocket.
 */
@Component(
        name = "org.wso2.msf4j.echoSever",
        service = WebSocketEndpoint.class,
        immediate = true
)
@ServerEndpoint("/echo")
public class EchoEndpoint implements WebSocketEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(EchoEndpoint.class);
    private List<Session> sessions = new ArrayList<>();

    @OnOpen
    public void onOpen(Session session) {
        LOGGER.info(session.getId() + " connected to repeat-app");
        sessions.add(session);
    }

    /**
     * Echo the same {@link String} to user.
     */
    @OnMessage
    public String onTextMessage(String text, Session session) throws IOException {
        LOGGER.info("Received Text : " + text + " from  " + session.getId());
        String msg =  "You said : " + text;
        return msg;
    }

    /**
     * Echo the same {@link ByteBuffer} X 2 to user.
     */
    @OnMessage
    public byte[] onBinaryMessage(ByteBuffer buffer, Session session) {
        String values = "";
        byte[] bytes = new byte[buffer.capacity()];
        for (int i = 0; i < buffer.capacity(); i++) {
            byte b = buffer.get();
            bytes[i] = (byte) (b * 2);
            values = values.concat(" " + b);
        }
        LOGGER.info("Binary message values from " + session.getId() + " : " + values);
        return bytes;
    }

    @OnClose
    public void onClose(CloseReason closeReason, Session session) {
        LOGGER.info("Connection is closed with status code : " + closeReason.getCloseCode().getCode()
                            + " On reason " + closeReason.getReasonPhrase());
        sessions.remove(session);
    }

}
