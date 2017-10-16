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

import org.wso2.msf4j.Request;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Wrapper {@link InputStream} for {@link Request}.
 */
public class MSF4JRequestInputStream extends InputStream {
    private Request request;
    private ByteBuffer buffer;

    public MSF4JRequestInputStream(Request request) {
        this.request = request;
        buffer = request.getMessageBody().nioBuffer();
    }

    @Override
    public int read() throws IOException {
        if (request.isEomAdded() && request.isEmpty() && !buffer.hasRemaining()) {
            return -1;
        } else if (!buffer.hasRemaining()) {
            buffer = request.getMessageBody().nioBuffer();
        }
        return buffer.get() & 0xFF;
    }

    @Override
    public int available() throws IOException {
        return buffer.remaining();
    }
}
