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

package org.wso2.msf4j.example;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.interceptor.RequestInterceptor;

/**
 * Spring based MSF4J Interceptor class which print HTTP headers from incoming messages.
 */
@Component
public class LogHeadersInterceptor implements RequestInterceptor {

    private final Log log = LogFactory.getLog(LogHeadersInterceptor.class);

    @Override
    public boolean interceptRequest(Request request, Response response) throws Exception {
        request.getHeaders().getAll()
                .forEach(header -> log.info("Header - " + header.getName() + " : " + header.getValue()));
        return true;
    }
}
