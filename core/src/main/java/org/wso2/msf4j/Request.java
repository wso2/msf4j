package org.wso2.msf4j;

import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.Headers;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.msf4j.internal.MSF4JConstants;

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

    private final HTTPCarbonMessage httpCarbonMessage;

    private List<String> acceptTypes = null;
    private String contentType = null;
    private SessionManager sessionManager;
    private Session session;
    public Request(HTTPCarbonMessage httpCarbonMessage) {
        this.httpCarbonMessage = httpCarbonMessage;
        // find accept types
        String acceptHeaderStr = httpCarbonMessage.getHeader(HttpHeaders.ACCEPT);
        acceptTypes = (acceptHeaderStr != null) ?
                Arrays.asList(acceptHeaderStr.split("\\s*,\\s*"))
                        .stream()
                        .map(mediaType -> mediaType.split("\\s*;\\s*")[0])
                        .collect(Collectors.toList()) :
                null;
        //find content type
        String contentTypeHeaderStr = httpCarbonMessage.getHeader(HttpHeaders.CONTENT_TYPE);
        //Trim specified charset since UTF-8 is assumed
        contentType = (contentTypeHeaderStr != null) ? contentTypeHeaderStr.split("\\s*;\\s*")[0] : null;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    /**
     * @return returns true if the object contains the complete request body
     */
    public boolean isEomAdded() {
        return httpCarbonMessage.isEndOfMsgAdded();
    }

    /**
     * @return true if the request does not have body content
     */
    public boolean isEmpty() {
        return httpCarbonMessage.isEmpty();
    }

    /**
     * @return next available message body chunk
     */
    public ByteBuffer getMessageBody() {
        return httpCarbonMessage.getMessageBody();
    }

    /**
     * @return full message body of the Request
     */
    public List<ByteBuffer> getFullMessageBody() {
        return httpCarbonMessage.getFullMessageBody();
    }

    /**
     * @return map of headers of the HTTP request
     */
    public Headers getHeaders() {
        return httpCarbonMessage.getHeaders();
    }

    /**
     * Get an HTTP header of the HTTP request.
     *
     * @param key name of the header
     * @return value of the header
     */
    public String getHeader(String key) {
        return httpCarbonMessage.getHeader(key);
    }

    /**
     * Set a property in the underlining Carbon Message.
     *
     * @param key property key
     * @return value of the property key
     */
    public Object getProperty(String key) {
        return httpCarbonMessage.getProperty(key);
    }

    /**
     * @return property map of the underlining CarbonMessage
     */
    public Map<String, Object> getProperties() {
        return httpCarbonMessage.getProperties();
    }

    /**
     * Set a property in the underlining Carbon Message.
     *
     * @param key   property key
     * @param value property value
     */
    public void setProperty(String key, Object value) {
        httpCarbonMessage.setProperty(key, value);
    }

    /**
     * Remove a property from the underlining CarbonMessage object.
     *
     * @param key property key
     */
    public void removeProperty(String key) {
        httpCarbonMessage.removeProperty(key);
    }

    /**
     * @return URL of the request.
     */
    public String getUri() {
        return (String) httpCarbonMessage.getProperty(Constants.TO);
    }

    /**
     * @return HTTP method of the request.
     */
    public String getHttpMethod() {
        return (String) httpCarbonMessage
                .getProperty(org.wso2.carbon.transport.http.netty.common.Constants.HTTP_METHOD);
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

    /**
     * Returns the current session associated with this request, or if the request does not have a session,
     * creates one.
     *
     * @return Session
     */
    public Session getSession() {
        if (sessionManager == null) {
            throw new IllegalStateException("SessionManager has not been set");
        }
        if (session != null) {
            return session.setAccessed();
        }
        String cookieHeader = getHeader("Cookie");
        if (cookieHeader != null) {
            session = Arrays.stream(cookieHeader.split(";"))
                    .filter(cookie -> cookie.startsWith(MSF4JConstants.SESSION_ID))
                    .findFirst()
                    .map(jsession -> sessionManager.getSession(jsession.substring(MSF4JConstants.SESSION_ID.length())))
                    .orElseGet(sessionManager::createSession);
            return session.setAccessed();
        }
        return session = sessionManager.createSession();
    }

    /**
     * Returns the current HttpSession associated with this request or, if there is no current session and create is
     * true, returns a new session.
     *
     * @param create Create a new session or not
     * @return Session
     */
    public Session getSession(boolean create) {
        if (sessionManager == null) {
            throw new IllegalStateException("SessionManager has not been set");
        }
        if (session != null) {
            return session.setAccessed();
        }
        String cookieHeader = getHeader("Cookie");
        if (cookieHeader != null) {
            session = Arrays.stream(cookieHeader.split(";"))
                    .filter(cookie -> cookie.startsWith(MSF4JConstants.SESSION_ID))
                    .findFirst()
                    .map(jsession -> sessionManager.getSession(jsession.substring(MSF4JConstants.SESSION_ID.length())))
                    .orElseGet(() -> {
                        if (create) {
                            return sessionManager.createSession();
                        }
                        return null;
                    });
            return session.setAccessed();
        } else if (create) {
            return session = sessionManager.createSession();
        }
        return null;
    }

    Session getSessionInternal() {
        return session;
    }

    /**
     * Get underlying HTTPCarbonMessage.
     *
     * @return HTTPCarbonMessage instance of the Request
     */
    public HTTPCarbonMessage getHttpCarbonMessage() {
        return httpCarbonMessage;
    }
}
