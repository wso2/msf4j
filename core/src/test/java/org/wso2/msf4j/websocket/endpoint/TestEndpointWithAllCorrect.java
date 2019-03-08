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

import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;

import java.nio.ByteBuffer;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * This endpoint include all necessary methods and all the acceptable parameters of those methods according to
 * JSR-356 specification
 */
@ServerEndpoint("/allCorrect/{param1}/{param2}")
public class TestEndpointWithAllCorrect {

    @OnOpen
    public void onOpen(@PathParam("param1") String param1, @PathParam("param2") String param2,
                       WebSocketConnection webSocketConnection) {
    }

    @OnMessage
    public byte[] onString(@PathParam("param1") String param1, @PathParam("param2") String param2, String text,
                           WebSocketConnection webSocketConnection) {
        return new byte[4];
    }

    @OnMessage
    public String onBinary(@PathParam("param1") String param1, @PathParam("param2") String param2, ByteBuffer buffer,
                           WebSocketConnection webSocketConnection) {
        return "test";
    }

    @OnMessage
    public PongMessage onPong(@PathParam("param1") String param1, @PathParam("param2") String param2,
                              PongMessage pongMessage, WebSocketConnection webSocketConnection) {
        return pongMessage;
    }

    @OnClose
    public void onClose(@PathParam("param1") String param1, @PathParam("param2") String param2, CloseReason closeReason,
                        WebSocketConnection webSocketConnection) {

    }

    @OnError
    public void onError(@PathParam("param1") String param1, @PathParam("param2") String param2, Throwable throwable,
                        WebSocketConnection webSocketConnection) {
    }
}
