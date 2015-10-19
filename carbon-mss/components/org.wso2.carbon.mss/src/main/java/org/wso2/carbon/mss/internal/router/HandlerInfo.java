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

package org.wso2.carbon.mss.internal.router;

import java.lang.reflect.Method;

/**
 * Contains information about {@link org.wso2.carbon.mss.HttpHandler} method.
 */
public class HandlerInfo {
    private final String handlerName;
    private final String methodName;

    private final Method method;

    public HandlerInfo(String handlerName, String methodName, Method method) {
        this.handlerName = handlerName;
        this.methodName = methodName;
        this.method = method;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public String getMethodName() {
        return methodName;
    }

    public Method getMethod() {
        return method;
    }
}
