/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.msf4j.util.client.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

/**
 * WebSocket Client Handler for Testing.
 */
public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketClientHandler.class);

    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    private ChannelHandlerContext ctx;

    private String textReceived = "";
    private ByteBuffer bufferReceived = null;
    private boolean isOpen;
    private CountDownLatch latch;

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker, CountDownLatch latch) {
        this.handshaker = handshaker;
        this.latch = latch;
        this.isOpen = true;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
        this.ctx = ctx;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws InterruptedException {
        logger.debug("WebSocket Client disconnected!");
        ctx.channel().close().sync();
        isOpen = false;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            logger.debug("WebSocket Client connected!");
            handshakeFuture.setSuccess();
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                    ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            logger.debug("WebSocket Client received text message: " + textFrame.text());
            textReceived = textFrame.text();
        } else if (frame instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) frame;
            bufferReceived = binaryFrame.content().nioBuffer();
            logger.debug("WebSocket Client received  binary message: " + bufferReceived.toString());
        } else if (frame instanceof PongWebSocketFrame) {
            logger.debug("WebSocket Client received pong");
            PongWebSocketFrame pongFrame = (PongWebSocketFrame) frame;
            bufferReceived = pongFrame.content().nioBuffer();
        } else if (frame instanceof CloseWebSocketFrame) {
            logger.debug("WebSocket Client received closing");
            isOpen = false;
        }
        countDownLatch();
    }

    /**
     * @return the text received from the server.
     */
    public String getTextReceived() {
        return textReceived;
    }

    /**
     * @return the binary data received from the server.
     */
    public ByteBuffer getBufferReceived() {
        return bufferReceived;
    }

    /**
     * Check whether the connection is still open or not.
     *
     * @return true if connection is still open.
     */
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }

    public void shutDown() throws InterruptedException {
        ctx.channel().writeAndFlush(new CloseWebSocketFrame());
        ctx.channel().closeFuture().sync();
    }

    private void countDownLatch() {
        if (this.latch == null) {
            return;
        }
        latch.countDown();
    }


}
