/*
 * Copyright © 2014 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.wso2.carbon.mss.internal.router;

import io.netty.handler.codec.http.HttpRequest;

/**
 * A base implementation of {@link HttpHandler} that provides a method for sending a request to other
 * handlers that exist in the same server.
 */
public abstract class AbstractHttpHandler implements HttpHandler {
    private HttpResourceHandler httpResourceHandler;

    @Override
    public void init(HandlerContext context) {
        this.httpResourceHandler = context.getHttpResourceHandler();
    }

    @Override
    public void destroy(HandlerContext context) {
        // No-op
    }

    /**
     * Send a request to another handler internal to the server, getting back the response body and response code.
     *
     * @param request request to send to another handler.
     * @return {@link BasicInternalHttpResponse} containing the response code and body.
     */
    protected InternalHttpResponse sendInternalRequest(HttpRequest request) {
        InternalHttpResponder responder = new InternalHttpResponder();
        httpResourceHandler.handle(request, responder);
        return responder.getResponse();
    }
}
