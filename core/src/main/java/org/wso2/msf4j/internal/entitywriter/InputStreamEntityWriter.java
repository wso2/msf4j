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

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.common.Constants;
import org.wso2.msf4j.Response;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * EntityWriter for entity of type InputStream.
 */
public class InputStreamEntityWriter implements EntityWriter<InputStream> {

    public static final int DEFAULT_CHUNK_SIZE = 1024;

    /**
     * Supported entity type.
     */
    @Override
    public Class<InputStream> getType() {
        return InputStream.class;
    }

    /**
     * Write the entity to the carbon message.
     */
    @Override
    public void writeData(CarbonMessage carbonMessage, InputStream ipStream,
                          String mediaType, int chunkSize, CarbonCallback cb) {
        try {
            if (chunkSize == Response.NO_CHUNK || chunkSize == Response.DEFAULT_CHUNK_SIZE) {
                chunkSize = DEFAULT_CHUNK_SIZE;
            }
            carbonMessage.setHeader(Constants.HTTP_TRANSFER_ENCODING, CHUNKED);
            carbonMessage.setHeader(Constants.HTTP_CONTENT_TYPE, mediaType);
            carbonMessage.setBufferContent(false);
            cb.done(carbonMessage);

            byte[] data = new byte[chunkSize];
            int len;
            while ((len = ipStream.read(data)) != -1) {
                carbonMessage.addMessageBody(ByteBuffer.wrap(data, 0, len));
            }

            ipStream.close();
            carbonMessage.setEndOfMsgAdded(true);
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while reading from InputStream", e);
        }
    }
}
