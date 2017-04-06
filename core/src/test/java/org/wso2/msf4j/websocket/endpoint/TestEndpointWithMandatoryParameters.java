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

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.server.ServerEndpoint;

/**
 *This test class include all the methods of WebSocket with only mandatory parameters according to JSR-356
 * specification
 */
@ServerEndpoint("/test-with-mandatory-params")
public class TestEndpointWithMandatoryParameters {

    @OnOpen
    public void onOpen() {
    }

    @OnMessage
    public byte[] onString(String text) {
        return new byte[4];
    }

    @OnMessage
    public String onBinary(byte[] bytes) {
        return "test";
    }

    @OnMessage
    public PongMessage onPong(PongMessage pongMessage) {
        return pongMessage;
    }

    @OnClose
    public void onClose() {

    }

    @OnError
    public void onError(Throwable throwable) {
    }
}
