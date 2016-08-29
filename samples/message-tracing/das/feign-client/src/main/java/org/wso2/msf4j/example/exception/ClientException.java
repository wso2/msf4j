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

package org.wso2.msf4j.example.exception;

import feign.Response;

public class ClientException extends Exception {
    private final int status;
    private final String reason;
    private final String methodKey;
    private final Response response;

    public ClientException(String methodKey, Response response) {
        this.status = response.status();
        this.reason = response.reason();
        this.methodKey = methodKey;
        this.response = response;
    }

    /**
     * Gets the HTTP status code of the failure, such as 404.
     */
    public int getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return String.format("[reason] %s, [method-key] %s, [status] %s, [headers] %s, [body] %s",
                reason, methodKey, status, response.headers(), response.toString());
    }

    @Override
    public String toString() {
        return String.format("[reason] %s, [method-key] %s, [status] %s, [headers] %s, [body] %s",
                reason, methodKey, status, response.headers(), response.toString());
    }
}
