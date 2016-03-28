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

import java.nio.ByteBuffer;

/**
 * HttpHandler would extend this abstract class and implement methods to stream the body directly.
 * chunk method would receive the http-chunks of the body and finished would be called
 * on receipt of the last chunk.
 */
public interface HttpStreamHandler {

    /**
     * Initialize the stream handler.
     *
     * @param response response object that should be used to send response
     */
    void init(Response response);

    /**
     * Http request content will be streamed directly to this method.
     *
     * @param content content of chunks
     */
    void chunk(ByteBuffer content) throws Exception;

    /**
     * This method will be called when all chunks
     * have been completely streamed.
     *
     * @throws Exception
     */
    void end() throws Exception;

    /**
     * When there is exception on netty while streaming, it will be propagated to handler
     * so the handler can do the cleanup.
     *
     * @param cause Cause of the Exception
     */
    void error(Throwable cause);
}
