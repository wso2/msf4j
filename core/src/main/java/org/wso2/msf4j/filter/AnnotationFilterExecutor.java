/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.msf4j.filter;

import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.filter.annotation.FilterRequest;
import org.wso2.msf4j.filter.annotation.FilterRequests;
import org.wso2.msf4j.filter.annotation.FilterResponse;
import org.wso2.msf4j.filter.annotation.FilterResponses;
import org.wso2.msf4j.internal.MicroservicesRegistryImpl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Class for executing filtering annotations.
 */
public class AnnotationFilterExecutor {

    /**
     * Process request filtering annotations.
     *
     * @param microServicesRegistry Current micro-services registry
     * @param method                Method
     * @param request               msf4j context
     */
    public boolean processRequestFilterAnnotation(MicroservicesRegistryImpl microServicesRegistry, Method method,
                                                  Request request) {
        Class<?> methodDeclaringClass = method.getDeclaringClass();
        boolean isMethodAnnotationPresent = method.isAnnotationPresent(FilterRequest.class) ||
                method.isAnnotationPresent(FilterRequests.class);
        boolean isClassAnnotationPresent = methodDeclaringClass.isAnnotationPresent(FilterRequest.class) ||
                methodDeclaringClass.isAnnotationPresent(FilterRequests.class);

        List<FilterRequest> filterRequests = new ArrayList<>(); // Annotations applied for resource / sub-resource
        if (isMethodAnnotationPresent) {
            filterRequests.addAll(Arrays.asList(method.getAnnotationsByType(FilterRequest.class)));
        }
        if (isClassAnnotationPresent) {
            filterRequests.addAll(Arrays.asList(methodDeclaringClass.getAnnotationsByType(FilterRequest.class)));
        }

        // MSF4J request instances
        Map<Class<?>, MSF4JRequestFilter> requestFilterMap = microServicesRegistry.getMsf4JRequestFilterListMap();

        // Global filters
        List<Class<?>> globalFilterClassList = microServicesRegistry.getGlobalRequestFilterClassList();

        // Prioritise

        // Code to fix build failure
        return requestFilterMap.isEmpty() || globalFilterClassList.isEmpty() || filterRequests.isEmpty();
    }

    /**
     * Process response filtering annotations.
     *
     * @param microServicesRegistry Current micro-services registry
     * @param method                Method
     * @param request               msf4j context
     */
    public boolean processResponseFilterAnnotation(MicroservicesRegistryImpl microServicesRegistry, Method method,
                                                   Request request, Response response) {
        Class<?> methodDeclaringClass = method.getDeclaringClass();
        boolean isMethodAnnotationPresent = method.isAnnotationPresent(FilterResponse.class) ||
                method.isAnnotationPresent(FilterResponses.class);
        boolean isClassAnnotationPresent = methodDeclaringClass.isAnnotationPresent(FilterResponse.class) ||
                methodDeclaringClass.isAnnotationPresent(FilterResponses.class);

        List<FilterResponse> filterResponses = new ArrayList<>(); // Annotations applied for resource / sub-resource
        if (isMethodAnnotationPresent) {
            filterResponses.addAll(Arrays.asList(method.getAnnotationsByType(FilterResponse.class)));
        }
        if (isClassAnnotationPresent) {
            filterResponses.addAll(Arrays.asList(methodDeclaringClass.getAnnotationsByType(FilterResponse.class)));
        }

        // MSF4J request instances
        Map<Class<?>, MSF4JResponseFilter> responseFilterMap = microServicesRegistry.getMsf4JResponseFilterListMap();

        // Global filters
        List<Class<?>> globalFilterClassList = microServicesRegistry.getGlobalResponseFilterClassList();

        // Prioritise

        // Code to fix build failure
        return responseFilterMap.isEmpty() || globalFilterClassList.isEmpty() || filterResponses.isEmpty();
    }
}
