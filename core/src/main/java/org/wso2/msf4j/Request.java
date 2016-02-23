package org.wso2.msf4j;

import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.FaultHandler;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;
import javax.ws.rs.core.HttpHeaders;

public class Request {

    private final CarbonMessage carbonMessage;
    private List<String> acceptTypes = null;
    private String contentType = null;

    public Request(CarbonMessage carbonMessage) {
        this.carbonMessage = carbonMessage;
        // find accept types
        String acceptHeaderStr = carbonMessage.getHeader(HttpHeaders.ACCEPT);
        acceptTypes = (acceptHeaderStr != null) ?
                Arrays.asList(acceptHeaderStr.split("\\s*,\\s*"))
                        .stream()
                        .map(mediaType -> mediaType.split("\\s*;\\s*")[0])
                        .collect(Collectors.toList()) :
                null;
        //find content type
        String contentTypeHeaderStr = carbonMessage.getHeader(HttpHeaders.CONTENT_TYPE);
        //Trim specified charset since UTF-8 is assumed
        contentType = (contentTypeHeaderStr != null) ? contentTypeHeaderStr.split("\\s*;\\s*")[0] : null;
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

    public String getUri() {
        return (String) carbonMessage.getProperty(Constants.TO);
    }

    public String getHttpMethod() {
        return (String) carbonMessage.getProperty(Constants.HTTP_METHOD);
    }

    public List<String> getAcceptTypes() {
        return acceptTypes;
    }

    public String getContentType() {
        return contentType;
    }

}
