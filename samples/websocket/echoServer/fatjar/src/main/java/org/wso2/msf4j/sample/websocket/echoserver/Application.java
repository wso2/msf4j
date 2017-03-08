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

import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.websocket.exception.WebSocketEndpointAnnotationException;

/**
 * This is a echo server fat jar runner class
 */
public class Application {
    public static void main(String[] args) throws WebSocketEndpointAnnotationException {
        new MicroservicesRunner().deployWebSocketEndpoint(new EchoEndpoint()).start();
    }
}
