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
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.mss.ChunkResponder;
import org.wso2.carbon.mss.HttpResponder;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;

/**
 * Wrap HttpResponder to call post handler hook.
 */
final class WrappedHttpResponder implements HttpResponder {
    private static final Logger LOG = LoggerFactory.getLogger(WrappedHttpResponder.class);

    private final HttpResponder delegate;
    private final Iterable<? extends HandlerHook> handlerHooks;
    private final HttpRequest httpRequest;
    private final HandlerInfo handlerInfo;

    public WrappedHttpResponder(HttpResponder delegate, Iterable<? extends HandlerHook> handlerHooks,
                                HttpRequest httpRequest, HandlerInfo handlerInfo) {
        this.delegate = delegate;
        this.handlerHooks = handlerHooks;
        this.httpRequest = httpRequest;
        this.handlerInfo = handlerInfo;
    }


    @Override
    public void sendJson(HttpResponseStatus status, Object object) {
        delegate.sendJson(status, object);
        runHook(status);
    }

    @Override
    public void sendJson(HttpResponseStatus status, Object object, Type type) {
        delegate.sendJson(status, object, type);
        runHook(status);
    }

    @Override
    public void sendJson(HttpResponseStatus status, Object object, Type type, Gson gson) {
        delegate.sendJson(status, object, type, gson);
        runHook(status);
    }

    @Override
    public void sendString(HttpResponseStatus status, String data) {
        delegate.sendString(status, data);
        runHook(status);
    }

    @Override
    public void sendString(HttpResponseStatus status, String data, @Nullable Multimap<String, String> headers) {
        delegate.sendString(status, data, headers);
        runHook(status);
    }

    @Override
    public void sendStatus(HttpResponseStatus status) {
        delegate.sendStatus(status);
        runHook(status);
    }

    @Override
    public void sendStatus(HttpResponseStatus status, Multimap<String, String> headers) {
        delegate.sendStatus(status, headers);
        runHook(status);
    }

    @Override
    public void sendByteArray(HttpResponseStatus status, byte[] bytes, Multimap<String, String> headers) {
        delegate.sendByteArray(status, bytes, headers);
        runHook(status);
    }

    @Override
    public void sendBytes(HttpResponseStatus status, ByteBuffer buffer, Multimap<String, String> headers) {
        delegate.sendBytes(status, buffer, headers);
        runHook(status);
    }

    @Override
    public ChunkResponder sendChunkStart(final HttpResponseStatus status, Multimap<String, String> headers) {
        final ChunkResponder chunkResponder = delegate.sendChunkStart(status, headers);
        return new ChunkResponder() {
            @Override
            public void sendChunk(ByteBuffer chunk) throws IOException {
                chunkResponder.sendChunk(chunk);
            }

            @Override
            public void sendChunk(ByteBuf chunk) throws IOException {
                chunkResponder.sendChunk(chunk);
            }

            @Override
            public void close() throws IOException {
                chunkResponder.close();
                runHook(status);
            }
        };
    }

    @Override
    public void sendContent(HttpResponseStatus status, ByteBuf content, String contentType,
                            Multimap<String, String> headers) {
        delegate.sendContent(status, content, contentType, headers);
        runHook(status);
    }

    @Override
    public void sendFile(File file, Multimap<String, String> headers) {
        delegate.sendFile(file, headers);
        runHook(HttpResponseStatus.OK);
    }

    private void runHook(HttpResponseStatus status) {
        for (HandlerHook hook : handlerHooks) {  //TODO: Fixme Azeez
            try {
                hook.postCall(httpRequest, status, handlerInfo);
            } catch (Throwable t) {
                LOG.error("Post handler hook threw exception: ", t);
            }
        }
    }
}
