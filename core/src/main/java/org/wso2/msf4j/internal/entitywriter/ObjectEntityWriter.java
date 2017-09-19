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

package org.wso2.msf4j.internal.entitywriter;

import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.carbon.transport.http.netty.contract.ServerConnectorException;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.internal.beanconversion.BeanConverter;

import java.nio.ByteBuffer;
import javax.ws.rs.core.MediaType;

/**
 * EntityWriter for  entity of type Object.
 */
public class ObjectEntityWriter implements EntityWriter<Object> {

    /**
     * Supported entity type.
     */
    @Override
    public Class<Object> getType() {
        return Object.class;
    }

    /**
     * Write the entity to the carbon message.
     */
    @Override
    public void writeData(HTTPCarbonMessage carbonMessage, Object entity, String mediaType, int chunkSize,
                          HTTPCarbonMessage responder) {
        mediaType = (mediaType != null) ? mediaType : MediaType.WILDCARD;
        ByteBuffer byteBuffer = BeanConverter.getConverter(mediaType).convertToMedia(entity);
        carbonMessage.addMessageBody(byteBuffer);
        carbonMessage.setEndOfMsgAdded(true);
        if (chunkSize == Response.NO_CHUNK) {
            carbonMessage.setHeader(Constants.HTTP_CONTENT_LENGTH, String.valueOf(byteBuffer.remaining()));
        } else {
            carbonMessage.setHeader(Constants.HTTP_TRANSFER_ENCODING, CHUNKED);
        }
        carbonMessage.setHeader(Constants.HTTP_CONTENT_TYPE, mediaType);
        try {
            responder.respond(carbonMessage);
        } catch (ServerConnectorException e) {
            throw new RuntimeException("Error while sending the response.", e);
        }
    }
}
