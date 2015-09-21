/*
 * Copyright © 2015 Cask Data, Inc.
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
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * Handles exceptions and provides a response via the {@link HttpResponder}.
 */
public class ExceptionHandler {
    public void handle(Throwable t, HttpRequest request, HttpResponder responder) {
        String message = String.format("Exception encountered while processing request : %s", t.getMessage());
        responder.sendString(HttpResponseStatus.INTERNAL_SERVER_ERROR, message);
    }
}
