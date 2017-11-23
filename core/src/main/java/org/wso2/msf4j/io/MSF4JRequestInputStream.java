package org.wso2.msf4j.io;
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

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpContent;
import org.wso2.msf4j.Request;

import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper {@link InputStream} for {@link Request}.
 */
public class MSF4JRequestInputStream extends InputStream {
    private Request request;
    private ByteBuf byteBuf = null;
    private HttpContent httpContent = null;

    public MSF4JRequestInputStream(Request request) {
        this.request = request;
        httpContent = request.getHttpCarbonMessage().getHttpContent();
        byteBuf = httpContent.content();
    }

    @Override
    public int read() throws IOException {
        if (request.isEmpty() && request.getHttpCarbonMessage().isEmpty() && !byteBuf.isReadable()) {
            httpContent.release();
            return -1;
        } else if (!byteBuf.isReadable()) {
            httpContent.release();
            httpContent = request.getHttpCarbonMessage().getHttpContent();
            byteBuf = httpContent.content();
        }
        return byteBuf.readByte();
    }

    @Override
    public int available() throws IOException {
        return byteBuf.writerIndex() - byteBuf.readerIndex();
    }
}
