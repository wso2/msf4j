package org.wso2.msf4j;

import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Constants;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ws.rs.core.HttpHeaders;

/**
 * Class that represents an HTTP request in MSF4J level.
 */
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

    /**
     * @return returns true if the object contains the complete request body
     */
    public boolean isEomAdded() {
        return carbonMessage.isEndOfMsgAdded();
    }

    /**
     * @return true if the request does not have body content
     */
    public boolean isEmpty() {
        return carbonMessage.isEmpty();
    }

    /**
     * @return next available message body chunk
     */
    public ByteBuffer getMessageBody() {
        return carbonMessage.getMessageBody();
    }

    /**
     * @return full message body of the Request
     */
    public List<ByteBuffer> getFullMessageBody() {
        return carbonMessage.getFullMessageBody();
    }

    /**
     * @return map of headers of the HTTP request
     */
    public Map<String, String> getHeaders() {
        return carbonMessage.getHeaders();
    }

    /**
     * Get an HTTP header of the HTTP request.
     *
     * @param key name of the header
     * @return value of the header
     */
    public String getHeader(String key) {
        return carbonMessage.getHeader(key);
    }

    /**
     * Set a property in the underlining Carbon Message.
     *
     * @param key property key
     * @return value of the property key
     */
    public Object getProperty(String key) {
        return carbonMessage.getProperty(key);
    }

    /**
     * @return property map of the underlining CarbonMessage
     */
    public Map<String, Object> getProperties() {
        return carbonMessage.getProperties();
    }

    /**
     * Set a property in the underlining Carbon Message.
     *
     * @param key   property key
     * @param value property value
     */
    public void setProperty(String key, Object value) {
        carbonMessage.setProperty(key, value);
    }

    /**
     * Remove a property from the underlining CarbonMessage object.
     *
     * @param key property key
     */
    public void removeProperty(String key) {
        carbonMessage.removeProperty(key);
    }

    /**
     * @return URL of the request.
     */
    public String getUri() {
        return (String) carbonMessage.getProperty(Constants.TO);
    }

    /**
     * @return HTTP method of the request.
     */
    public String getHttpMethod() {
        return (String) carbonMessage.getProperty(Constants.HTTP_METHOD);
    }

    /**
     * @return accept type of the request.
     */
    public List<String> getAcceptTypes() {
        return acceptTypes;
    }

    /**
     * @return request body content type.
     */
    public String getContentType() {
        return contentType;
    }

}
