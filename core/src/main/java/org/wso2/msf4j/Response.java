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

import com.google.common.io.Files;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.FaultHandler;
import org.wso2.msf4j.internal.mime.MimeMapper;
import org.wso2.msf4j.internal.mime.MimeMappingException;
import org.wso2.msf4j.internal.router.beanconversion.BeanConversionException;
import org.wso2.msf4j.internal.router.beanconversion.BeanConverter;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

public class Response {

    private final String COMMA_SEPARATOR = ", ";
    private final int NULL_STATUS_CODE = -1;
    public static final String CHUNKED = "chunked";

    private final CarbonMessage carbonMessage;
    private final CarbonCallback carbonCallback;
    private int statusCode = NULL_STATUS_CODE;
    private String mediaType = null;
    private Object entity;

    private boolean isSendChunked = false;

    public Response(CarbonCallback carbonCallback) {
        carbonMessage = new DefaultCarbonMessage();
        this.carbonCallback = carbonCallback;
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

    public CarbonMessage getCarbonMessage() {
        return carbonMessage;
    }

    /**
     * Set the status code of the HTTP response.
     *
     * @param statusCode HTTP status code
     */
    public void setStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Set HTTP media type of the response.
     *
     * @param mediaType HTTP media type string
     */
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Set http body for the HTTP response.
     *
     * @param entity object that should be set as the response body
     */
    public void setEntity(Object entity) {
        if (!carbonMessage.isEmpty()) {
            throw new IllegalStateException("CarbonMessage should not contain a message body");
        }
        if (entity instanceof javax.ws.rs.core.Response) {
            javax.ws.rs.core.Response response = (javax.ws.rs.core.Response) entity;
            this.entity = response.getEntity();
            MultivaluedMap<String, String> multivaluedMap = response.getStringHeaders();
            if (multivaluedMap != null) {
                multivaluedMap.forEach((key, strings) -> setHeader(key, String.join(COMMA_SEPARATOR, strings)));
            }
            setStatus(response.getStatus());
            if (response.getMediaType() != null) {
                setMediaType(response.getMediaType().toString());
            }
        } else {
            this.entity = entity;
        }
    }

    /**
     * Specify to send the response in chunks or as a whole. By default
     * the response will be sent as a whole.
     *
     * @param isSendChunked true to send response in chunks
     */
    public void setSendChunked(boolean isSendChunked) {
        isSendChunked = isSendChunked;
    }

    /**
     * Send the HTTP response using the content in this object.
     */
    public void send() throws BeanConversionException {
        processStatusCode();
        processEntity();
        carbonCallback.done(carbonMessage);
    }

    private void processStatusCode() {
        if (statusCode != NULL_STATUS_CODE) {
            carbonMessage.setProperty(Constants.HTTP_STATUS_CODE, statusCode);
        } else if (entity != null) {
            carbonMessage.setProperty(Constants.HTTP_STATUS_CODE,
                    javax.ws.rs.core.Response.Status.OK.getStatusCode());
        } else {
            carbonMessage.setProperty(Constants.HTTP_STATUS_CODE,
                    javax.ws.rs.core.Response.Status.NO_CONTENT.getStatusCode());
        }
    }

    // TODO: come up with a way to send async file / async stream
    private void processEntity() throws BeanConversionException {
        if (entity != null) {
            if (entity instanceof File) {
                File file = (File) entity;
                mediaType = getMediaType(file);
                // TODO: send file in chunks or in whole
            } else {
                mediaType = (mediaType != null) ? mediaType : MediaType.WILDCARD;
                ByteBuffer byteBuffer = BeanConverter.instance(mediaType).convertToMedia(entity);
                carbonMessage.addMessageBody(byteBuffer);
                carbonMessage.setEomAdded(true);
                if (isSendChunked) {
                    carbonMessage.setHeader(Constants.HTTP_TRANSFER_ENCODING, CHUNKED);
                } else {
                    carbonMessage.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(byteBuffer.remaining()));
                }
            }
            carbonMessage.setHeader(Constants.HTTP_CONTENT_TYPE, mediaType);
        }
    }

    private String getMediaType(File file) {
        if (mediaType == null || mediaType.equals(MediaType.WILDCARD)) {
            try {
                return MimeMapper.getMimeType(Files.getFileExtension(file.getName()));
            } catch (MimeMappingException e) {
                return MediaType.WILDCARD;
            }
        }
        return mediaType;
    }
}
