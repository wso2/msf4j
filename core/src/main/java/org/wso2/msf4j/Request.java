package org.wso2.msf4j;

import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.FaultHandler;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Request {

    private final CarbonMessage carbonMessage;

    public Request(CarbonMessage carbonMessage) {
        this.carbonMessage = carbonMessage;
    }

    public boolean isEomAdded() {
        return carbonMessage.isEomAdded();
    }

    public void setEomAdded(boolean eomAdded) {
        carbonMessage.setEomAdded(eomAdded);
    }

    public boolean isEmpty() {
        return carbonMessage.isEmpty();
    }

    public ByteBuffer getMessageBody() {
        return carbonMessage.getMessageBody();
    }

    public List<ByteBuffer> getFullMessageBody() {
        return carbonMessage.getFullMessageBody();
    }

    public void addMessageBody(ByteBuffer msgBody) {
        carbonMessage.addMessageBody(msgBody);
    }

    public Map<String, String> getHeaders() {
        return carbonMessage.getHeaders();
    }

    public String getHeader(String key) {
        return carbonMessage.getHeader(key);
    }

    public void setHeader(String key, String value) {
        carbonMessage.setHeader(key, value);
    }

    public void setHeaders(Map<String, String> headerMap) {
        carbonMessage.setHeaders(headerMap);
    }

    public Object getProperty(String key) {
        return carbonMessage.getProperty(key);
    }

    public Map<String, Object> getProperties() {
        return carbonMessage.getProperties();
    }

    public void setProperty(String key, Object value) {
        carbonMessage.setProperty(key, value);
    }

    public void removeHeader(String key) {
        carbonMessage.removeHeader(key);
    }

    public void removeProperty(String key) {
        carbonMessage.removeProperty(key);
    }

    public Stack<FaultHandler> getFaultHandlerStack() {
        return carbonMessage.getFaultHandlerStack();
    }

    public void setFaultHandlerStack(Stack<FaultHandler> faultHandlerStack) {
        carbonMessage.setFaultHandlerStack(faultHandlerStack);
    }
}
