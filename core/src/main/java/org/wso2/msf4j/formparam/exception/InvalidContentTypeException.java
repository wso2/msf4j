package org.wso2.msf4j.formparam.exception;
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

/**
 * Thrown to indicate that the request is not a multipart request.
 */
public class InvalidContentTypeException extends FormUploadException {

    /**
     * The exceptions UID, for serializing an instance.
     */
    private static final long serialVersionUID = -9073026332015646668L;

    /**
     * Constructs an <code>InvalidContentTypeException</code> with
     * the specified detail message.
     *
     * @param message The detail message.
     */
    public InvalidContentTypeException(String message) {
        super(message);
    }

    /**
     * Constructs an <code>InvalidContentTypeException</code> with
     * the specified detail message and cause.
     *
     * @param msg   The detail message.
     * @param cause the original cause
     */
    public InvalidContentTypeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
