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


import java.lang.reflect.Method;

/**
 * Contains information about service method details.
 * @deprecated
 */
public class ServiceMethodInfo {

    private final String methodName;
    private final Method method;
    private final Request request;

    public ServiceMethodInfo(String methodName, Method method, Request request) {
        this.methodName = methodName;
        this.method = method;
        this.request = request;
    }

    public String getMethodName() {
        return methodName;
    }

    public Method getMethod() {
        return method;
    }

    /**
     * Returns the value of the named attribute as an Object, or null if no attribute of the given name exists.
     *
     * @param name a {@link String} specifying the name of the attribute
     * @return an {@link Object} containing the value of the attribute, or null if the attribute does not exist
     */
    public Object getAttribute(String name) {
        return request.getProperty(name);
    }

    /**
     * Stores an attribute in this request.
     *
     * @param name a {@link String} specifying the name of the attribute
     * @param obj  the {@link Object} to be stored
     */
    public void setAttribute(String name, Object obj) {
        request.setProperty(name, obj);
    }
}
