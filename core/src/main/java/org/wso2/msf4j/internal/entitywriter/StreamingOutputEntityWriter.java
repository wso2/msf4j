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

import java.io.IOException;
import java.util.concurrent.Executors;
import javax.ws.rs.core.StreamingOutput;

/**
 * EntityWriter for entity of type {@link javax.ws.rs.core.StreamingOutput}.
 */
public class StreamingOutputEntityWriter implements EntityWriter<StreamingOutput> {

    /**
     * Supported entity type.
     */
    @Override
    public Class<StreamingOutput> getType() {
        return StreamingOutput.class;
    }

    /**
     * Write the entity to the carbon message.
     */
    @Override
    public void writeData(HTTPCarbonMessage carbonMessage, StreamingOutput output,
                          String mediaType, int chunkSize, HTTPCarbonMessage responder) {
        try {
            carbonMessage.setHeader(Constants.HTTP_CONTENT_TYPE, mediaType);
            carbonMessage.setHeader(Constants.HTTP_TRANSFER_ENCODING, CHUNKED);
            Executors.newSingleThreadExecutor().execute(() -> {
                try {
                    responder.respond(carbonMessage);
                } catch (ServerConnectorException e) {
                    throw new RuntimeException("Error while sending the response.", e);
                }
            });
            output.write(carbonMessage.getOutputStream());
            carbonMessage.setEndOfMsgAdded(true);
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while streaming output", e);
        }
    }
}
