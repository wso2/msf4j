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

package org.wso2.carbon.mss;

import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;

/**
 * HttpResponder is used to send response back to clients.
 */
public interface HttpResponder {

    /**
     * Sends json response back to the client.
     *
     * @param status Status of the response.
     * @param object Object that will be serialized into Json and sent back as content.
     */
    void sendJson(HttpResponseStatus status, Object object);

    /**
     * Sends json response back to the client.
     *
     * @param status Status of the response.
     * @param object Object that will be serialized into Json and sent back as content.
     * @param type   Type of object.
     */
    void sendJson(HttpResponseStatus status, Object object, Type type);

    /**
     * Sends json response back to the client using the given gson object.
     *
     * @param status Status of the response.
     * @param object Object that will be serialized into Json and sent back as content.
     * @param type   Type of object.
     * @param gson   Gson object for serialization.
     */
    void sendJson(HttpResponseStatus status, Object object, Type type, Gson gson);

    /**
     * Send a string response back to the http client.
     *
     * @param status status of the Http response.
     * @param data   string data to be sent back.
     */
    void sendString(HttpResponseStatus status, String data);

    /**
     * Send a string response back to the http client.
     *
     * @param status  status of the Http response.
     * @param data    string data to be sent back.
     * @param headers Headers to send.
     */
    void sendString(HttpResponseStatus status, String data, @Nullable Multimap<String, String> headers);

    /**
     * Send only a status code back to client without any content.
     *
     * @param status status of the Http response.
     */
    void sendStatus(HttpResponseStatus status);

    /**
     * Send only a status code back to client without any content.
     *
     * @param status  status of the Http response.
     * @param headers Headers to send.
     */
    void sendStatus(HttpResponseStatus status, @Nullable Multimap<String, String> headers);

    /**
     * Send a response containing raw bytes. Sets "application/octet-stream" as content type header.
     *
     * @param status  status of the Http response.
     * @param bytes   bytes to be sent back.
     * @param headers headers to be sent back. This will overwrite any headers set by the framework.
     */
    void sendByteArray(HttpResponseStatus status, byte[] bytes, @Nullable Multimap<String, String> headers);

    /**
     * Sends a response containing raw bytes. Default content type is "application/octet-stream", but can be
     * overridden in the headers.
     *
     * @param status  status of the Http response
     * @param buffer  bytes to send
     * @param headers Headers to send.
     */
    void sendBytes(HttpResponseStatus status, ByteBuffer buffer, @Nullable Multimap<String, String> headers);

    /**
     * Respond to the client saying the response will be in chunks. The response body can be sent in chunks
     * using the {@link ChunkResponder} returned.
     *
     * @param status  the status code to respond with
     * @param headers additional headers to send with the response. May be null.
     * @return ChunkResponder
     */
    ChunkResponder sendChunkStart(HttpResponseStatus status, @Nullable Multimap<String, String> headers);

    /**
     * Send response back to client.
     *
     * @param status      Status of the response.
     * @param content     Content to be sent back.
     * @param contentType Type of content.
     * @param headers     Headers to be sent back.
     */
    void sendContent(HttpResponseStatus status, ByteBuf content, String contentType,
                     @Nullable Multimap<String, String> headers);


    /**
     * Sends a file content back to client with response status 200.
     *
     * @param file    The file to send
     * @param headers Headers to be sent back.
     */
    void sendFile(File file, String contentType,
                  @Nullable Multimap<String, String> headers);
}
