/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.mss;


import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains information about service method details.
 */
public class ServiceMethodInfo {

    private final String handlerName;
    private final Method method;

    private Map<String, Object> attributes = new ConcurrentHashMap<>();

    public ServiceMethodInfo(String handlerName, Method method) {
        this.handlerName = handlerName;
        this.method = method;
    }

    public String getHandlerName() {
        return handlerName;
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
        return attributes.get(name);
    }

    /**
     * Stores an attribute in this request.
     *
     * @param name a {@link String} specifying the name of the attribute
     * @param obj  the {@link Object} to be stored
     */
    public void setAttribute(String name, Object obj) {
        attributes.put(name, obj);
    }
}
