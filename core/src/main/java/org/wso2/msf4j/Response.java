/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.msf4j;

import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.FaultHandler;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Response {

    private final DefaultCarbonMessage defaultCarbonMessage = new DefaultCarbonMessage();

    public boolean isEomAdded() {
        return defaultCarbonMessage.isEomAdded();
    }

    public void setEomAdded(boolean eomAdded) {
        defaultCarbonMessage.setEomAdded(eomAdded);
    }

    public boolean isEmpty() {
        return defaultCarbonMessage.isEmpty();
    }

    public ByteBuffer getMessageBody() {
        return defaultCarbonMessage.getMessageBody();
    }

    public List<ByteBuffer> getFullMessageBody() {
        return defaultCarbonMessage.getFullMessageBody();
    }

    public void addMessageBody(ByteBuffer msgBody) {
        defaultCarbonMessage.addMessageBody(msgBody);
    }

    public Map<String, String> getHeaders() {
        return defaultCarbonMessage.getHeaders();
    }

    public String getHeader(String key) {
        return defaultCarbonMessage.getHeader(key);
    }

    public void setHeader(String key, String value) {
        defaultCarbonMessage.setHeader(key, value);
    }

    public void setHeaders(Map<String, String> headerMap) {
        defaultCarbonMessage.setHeaders(headerMap);
    }

    public Object getProperty(String key) {
        return defaultCarbonMessage.getProperty(key);
    }

    public Map<String, Object> getProperties() {
        return defaultCarbonMessage.getProperties();
    }

    public void setProperty(String key, Object value) {
        defaultCarbonMessage.setProperty(key, value);
    }

    public void removeHeader(String key) {
        defaultCarbonMessage.removeHeader(key);
    }

    public void removeProperty(String key) {
        defaultCarbonMessage.removeProperty(key);
    }

    public Stack<FaultHandler> getFaultHandlerStack() {
        return defaultCarbonMessage.getFaultHandlerStack();
    }

    public void setFaultHandlerStack(Stack<FaultHandler> faultHandlerStack) {
        defaultCarbonMessage.setFaultHandlerStack(faultHandlerStack);
    }

    public void setStringMessageBody(String stringMessageBody) {
        defaultCarbonMessage.setStringMessageBody(stringMessageBody);
    }

    public CarbonMessage getCarbonMessage() {
        return defaultCarbonMessage;
    }

    public void send() {

    }
}
