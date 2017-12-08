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

package org.wso2.msf4j.formparam;

import org.wso2.msf4j.Request;

import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Request information needed for file uploads.
 */
public class RequestContext {

    private Request request;

    public RequestContext(Request request) {
        this.request = request;
    }

    /**
     * Get request's character encoding.
     *
     * @return character encoding of the request
     */
    public String getCharacterEncoding() {
        return Charset.defaultCharset().name();
    }

    /**
     * Get the request's content type.
     *
     * @return String request's content type
     */
    public String getContentType() {
        return this.request.getHeader("Content-Type");
    }

    /**
     * Get the request's inputstream.
     *
     * @return InputStream request's inputstream
     */
    public InputStream getInputStream() {
        return request.getMessageContentStream();
    }

}
