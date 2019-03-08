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

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.internal.beanconversion.BeanConverter;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

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
    public void writeData(HttpCarbonMessage carbonMessage, Object entity, String mediaType, int chunkSize,
                          HttpCarbonMessage responder) {
        mediaType = (mediaType != null) ? mediaType : MediaType.WILDCARD;
        ByteBuffer byteBuffer = BeanConverter.getConverter(mediaType).convertToMedia(entity);
        carbonMessage.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(byteBuffer)));
        if (chunkSize == Response.NO_CHUNK) {
            carbonMessage.setHeader(HttpHeaderNames.CONTENT_LENGTH.toString(), String.valueOf(byteBuffer.remaining()));
        } else {
            carbonMessage.setHeader(HttpHeaderNames.TRANSFER_ENCODING.toString(), CHUNKED);
        }
        carbonMessage.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), mediaType);
        try {
            responder.respond(carbonMessage);
        } catch (ServerConnectorException e) {
            throw new RuntimeException("Error while sending the response.", e);
        }
    }
}
