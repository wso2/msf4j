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

package org.wso2.carbon.mss.internal.router;

import io.netty.handler.codec.http.HttpRequest;
import org.wso2.carbon.mss.HttpResponder;

/**
 * Re-writes URL of an incoming request before any handlers or their interceptors are called.
 * This can be used to map an incoming URL to an URL that a handler understands. The re-writer overwrites the incoming
 * URL with the new value.
 * The re-writer can also send response to the clients, eg. redirect header,
 * and then stop further request processing.
 */
public interface URLRewriter {
    /**
     * Implement this to rewrite URL of an incoming request. The re-written URL needs to be updated back in
     * {@code request} using {@link HttpRequest#setUri(String)}.
     *
     * @param request   Incoming HTTP request.
     * @param responder Used to send response to clients.
     * @return true if request processing should continue, false otherwise.
     */
    boolean rewrite(HttpRequest request, HttpResponder responder);
}
