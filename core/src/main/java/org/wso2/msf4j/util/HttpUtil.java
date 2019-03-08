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

package org.wso2.msf4j.util;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import javax.ws.rs.core.HttpHeaders;

/**
 * Utility methods related to HTTP.
 */
public class HttpUtil {

    public static final String EMPTY_BODY = "";
    public static final String CLOSE = "close";
    public static final String KEEP_ALIVE = "keep-alive";

    /**
     * Create a CarbonMessage for a specific status code.
     *
     * @param status HTTP status code
     * @param msg message text
     * @return CarbonMessage representing the status
     */
    public static HttpCarbonMessage createTextResponse(int status, String msg) {
        HttpCarbonMessage response = new HttpCarbonMessage(
                new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(status)));
        response.setProperty(Constants.HTTP_STATUS_CODE, status);
        if (msg != null) {
            response.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(msg.length()));
            byte[] msgArray = null;
            try {
                msgArray = msg.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("Failed to get the byte array from responseValue", e);
            }
            ByteBuffer byteBuffer = ByteBuffer.allocate(msgArray.length);
            byteBuffer.put(msgArray);
            byteBuffer.flip();
            response.addHttpContent(new DefaultLastHttpContent(Unpooled.wrappedBuffer(byteBuffer)));
        } else {
            response.setHeader(HttpHeaders.CONTENT_LENGTH, "0");
        }
        return response;
    }

    /**
     * Set connection header of the response object according to the
     * connection header of the request.
     *
     * @param request  HTTP request object
     * @param response HTTP response object
     */
    public static void setConnectionHeader(Request request, Response response) {
        String connection = request.getHeader(HttpHeaderNames.CONNECTION.toString());
        if (connection != null && CLOSE.equalsIgnoreCase(connection)) {
            response.setHeader(HttpHeaderNames.CONNECTION.toString(), CLOSE);
        } else {
            response.setHeader(HttpHeaderNames.CONNECTION.toString(), KEEP_ALIVE);
        }
    }
}
