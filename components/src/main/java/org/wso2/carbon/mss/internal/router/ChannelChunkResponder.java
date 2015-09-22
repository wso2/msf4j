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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.DefaultHttpContent;
import org.wso2.carbon.mss.ChunkResponder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link org.wso2.carbon.mss.ChunkResponder} that writes chunks to a {@link Channel}.
 */
final class ChannelChunkResponder implements ChunkResponder {

    private final Channel channel;
    private final boolean keepAlive;
    private final AtomicBoolean closed;

    ChannelChunkResponder(Channel channel, boolean keepAlive) {
        this.channel = channel;
        this.keepAlive = keepAlive;
        this.closed = new AtomicBoolean(false);
    }

    @Override
    public void sendChunk(ByteBuffer chunk) throws IOException {
        sendChunk(Unpooled.wrappedBuffer(chunk));
    }

    @Override
    public void sendChunk(ByteBuf chunk) throws IOException {
        if (closed.get()) {
            throw new IOException("ChunkResponder already closed.");
        }
        if (!channel.isWritable()) {
            throw new IOException("Connection already closed.");
        }
        channel.write(new DefaultHttpContent(chunk));
    }

    @Override
    public void close() throws IOException {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        //TODO: azeez Fix
    /*ChannelFuture future = channel.write(new DefaultHttpChunkTrailer());
    if (!keepAlive) {
      future.addListener(ChannelFutureListener.CLOSE);
    }*/
    }
}
