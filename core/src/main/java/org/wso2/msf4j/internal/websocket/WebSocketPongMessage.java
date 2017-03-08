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

package org.wso2.msf4j.internal.websocket;

import java.nio.ByteBuffer;
import javax.websocket.PongMessage;

/**
 * WebSocket pong message implementation of {@link PongMessage}.
 */
public class WebSocketPongMessage implements PongMessage {

    private final ByteBuffer applicationData;

    /**
     * @param byteBuffer application data of the {@link PongMessage}
     */
    public WebSocketPongMessage(ByteBuffer byteBuffer) {
        applicationData = byteBuffer;
    }

    @Override
    public ByteBuffer getApplicationData() {
        return applicationData;
    }
}
