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

import org.wso2.msf4j.MicroservicesRunner;

/**
 * This is the runner of the Fatjar. This should be configured as the main class in the pom.xml
 */
public class Application {
    public static void main(String[] args) {
        new MicroservicesRunner().deployWebSocketEndpoint(new ChatAppEndpoint()).start();
    }
}
