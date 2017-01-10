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

import org.wso2.msf4j.WebSocketEndpoint;

import java.lang.reflect.Method;

/**
 * After dispatching the endpoint all the methods will be found and stored in here
 */
public class DispatchedEndpoint {
    private final String uri;
    private final Method onOpenMethod;
    private final Method onStringMessageMethod;
    private final Method onBinaryMessageMethod;
    private final Method onPongMessageMethod;
    private final Method onCloseMethod;
    private final WebSocketEndpoint webSocketEndpoint;

    public DispatchedEndpoint(String uri, Method onOpenMethod, Method onStringMessageMethod,
                              Method onBinaryMessageMethod, Method onPongMessageMethod,
                              Method onCloseMethod, WebSocketEndpoint webSocketEndpoint) {
        this.uri = uri;
        this.onOpenMethod = onOpenMethod;
        this.onStringMessageMethod = onStringMessageMethod;
        this.onBinaryMessageMethod = onBinaryMessageMethod;
        this.onPongMessageMethod = onPongMessageMethod;
        this.onCloseMethod = onCloseMethod;
        this.webSocketEndpoint = webSocketEndpoint;
    }

    public String getUri() {
        return uri;
    }

    public Method getOnOpenMethod() {
        return onOpenMethod;
    }

    public Method getOnStringMessageMethod() {
        return onStringMessageMethod;
    }

    public Method getOnBinaryMessageMethod() {
        return onBinaryMessageMethod;
    }

    public Method getOnPongMessageMethod() {
        return onPongMessageMethod;
    }

    public Method getOnCloseMethod() {
        return onCloseMethod;
    }

    public WebSocketEndpoint getWebSocketEndpoint() {
        return webSocketEndpoint;
    }
}
