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


import javax.websocket.CloseReason;

/**
 * {@link javax.websocket.CloseReason.CloseCode} implementation for WebSocket in MSF4J
 *
 * @since 1.0.0
 */
public class CloseCodeImpl implements CloseReason.CloseCode {
    /**
     * Returns the code number, for example the integer '1000' for normal closure.
     *
     * @return the code number
     */

    private final int closeCode;

    /**
     * @param closeCode close code for the reason of closure.
     */
    public CloseCodeImpl(int closeCode) {
        this.closeCode = closeCode;
    }

    @Override
    public int getCode() {
        return this.closeCode;
    }
}
