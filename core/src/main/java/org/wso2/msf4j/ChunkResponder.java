/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import io.netty.buffer.ByteBuf;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A responder for sending chunk-encoded response.
 */
public interface ChunkResponder extends Closeable {

    /**
     * Adds a chunk of data to the response. The content will be sent to the client asynchronously.
     *
     * @param chunk content to send
     * @throws IOException if the connection is already closed
     */
    void sendChunk(ByteBuffer chunk) throws IOException;

    /**
     * Adds a chunk of data to the response. The content will be sent to the client asynchronously.
     *
     * @param chunk content to send
     * @throws IOException if this {@link ChunkResponder} already closed or the connection is closed
     */
    void sendChunk(ByteBuf chunk) throws IOException;

    /**
     * Closes this responder which signals the end of the chunk response.
     */
    @Override
    void close() throws IOException;
}
