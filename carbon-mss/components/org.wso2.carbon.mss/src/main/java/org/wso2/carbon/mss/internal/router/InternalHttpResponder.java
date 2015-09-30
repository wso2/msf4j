/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.mss.internal.router;

import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import com.google.common.io.InputSupplier;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.wso2.carbon.mss.ChunkResponder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;

/**
 * InternalHttpResponder is used when a handler is being called internally by some other handler, and thus there
 * is no need to go through the network.  It stores the status code and content in memory and returns them when asked.
 */
public class InternalHttpResponder extends AbstractHttpResponder {

    private int statusCode;
    private InputSupplier<? extends InputStream> inputSupplier;

    public InternalHttpResponder() {
        statusCode = 0;
    }

    @Override
    public ChunkResponder sendChunkStart(HttpResponseStatus status, @Nullable Multimap<String, String> headers) {
        statusCode = status.code();
        return new ChunkResponder() {

            private ByteBuf contentChunks = Unpooled.EMPTY_BUFFER;
            private boolean closed;

            @Override
            public void sendChunk(ByteBuffer chunk) throws IOException {
                sendChunk(Unpooled.wrappedBuffer(chunk));
            }

            @Override
            public synchronized void sendChunk(ByteBuf chunk) throws IOException {
                if (closed) {
                    throw new IOException("ChunkResponder already closed.");
                }
                contentChunks = Unpooled.wrappedBuffer(contentChunks, chunk);
            }

            @Override
            public synchronized void close() throws IOException {
                if (closed) {
                    return;
                }
                closed = true;
                inputSupplier = createContentSupplier(contentChunks);
            }
        };
    }

    @Override
    public void sendContent(HttpResponseStatus status, @Nullable ByteBuf content, String contentType,
                            @Nullable Multimap<String, String> headers) {
        statusCode = status.code();
        inputSupplier = createContentSupplier(content == null ? Unpooled.EMPTY_BUFFER : content);
    }

    @Override
    public void sendFile(File file, @Nullable Multimap<String, String> headers) {
        statusCode = HttpResponseStatus.OK.code();
        inputSupplier = Files.newInputStreamSupplier(file);
    }

    public InternalHttpResponse getResponse() {
        return new BasicInternalHttpResponse(statusCode, inputSupplier);
    }

    private InputSupplier<InputStream> createContentSupplier(ByteBuf content) {
        final ByteBuf responseContent = content.duplicate();    // Have independent pointers.
        responseContent.markReaderIndex();
        return new HttpResponderInputSupplier(responseContent).invoke();
    }

    private static class HttpResponderInputSupplier {
        private final ByteBuf responseContent;

        public HttpResponderInputSupplier(ByteBuf responseContent) {
            this.responseContent = responseContent;
        }

        public InputSupplier<InputStream> invoke() {
            return new InputSupplier<InputStream>() {
                @Override
                public InputStream getInput() throws IOException {
                    responseContent.resetReaderIndex();
                    return new ByteBufInputStream(responseContent);
                }
            };
        }
    }
}
