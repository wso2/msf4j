/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.mss.example.hook;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mss.HttpResponder;
import org.wso2.carbon.mss.internal.router.HandlerHook;
import org.wso2.carbon.mss.internal.router.HandlerInfo;

import java.util.Iterator;
import java.util.Map;

/**
 * Sample Hook which log HTTP headers of request messages.
 */
public class LoggingHeadersHook implements HandlerHook {

    private final Log log = LogFactory.getLog(LoggingHeadersHook.class);

    @Override
    public boolean preCall(HttpRequest request, HttpResponder responder, HandlerInfo handlerInfo) {
        Iterator<Map.Entry<String, String>> itr = request.headers().iterator();
        while (itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();
            log.info("Header Name: " + entry.getKey() + " value : " + entry.getValue());
        }
        return true;
    }

    @Override
    public void postCall(HttpRequest request, HttpResponseStatus status, HandlerInfo handlerInfo) {
    }
}
