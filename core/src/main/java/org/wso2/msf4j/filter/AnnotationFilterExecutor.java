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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;

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
    public void processRequestFilterAnnotation(MicroservicesRegistryImpl microServicesRegistry, Method method,
                                               Request request) throws IOException {
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
        Set<Class<?>> globalFilterClassSet = microServicesRegistry.getGlobalRequestFilterClassSet();
        // Prioritise
        List<Class<?>> classList = Stream
                .concat(filterRequests.stream().map(FilterRequest::value), globalFilterClassSet.stream())
                .collect(Collectors.toList());
        // For requests priorities are ascending
        TreeMap<Integer, List<Class<?>>> priorityClassMap = getPriorityClassMap(classList, false);

        // For requests priorities are in ascending order
        for (Map.Entry<Integer, List<Class<?>>> entry : priorityClassMap.entrySet()) {
            for (Class<?> clazz : entry.getValue()) {
                requestFilterMap.get(clazz).filter(request);
            }
        }
    }

    /**
     * Process response filtering annotations.
     *
     * @param microServicesRegistry Current micro-services registry
     * @param method                Method
     * @param request               msf4j context
     */
    public void processResponseFilterAnnotation(MicroservicesRegistryImpl microServicesRegistry, Method method,
                                                Request request, Response response) throws IOException {
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

        // MSF4J response instances
        Map<Class<?>, MSF4JResponseFilter> responseFilterMap = microServicesRegistry.getMsf4JResponseFilterListMap();
        // Global filters
        Set<Class<?>> globalFilterClassSet = microServicesRegistry.getGlobalResponseFilterClassSet();
        // Prioritise
        List<Class<?>> classList = Stream
                .concat(filterResponses.stream().map(FilterResponse::value), globalFilterClassSet.stream())
                .collect(Collectors.toList());
        // For response priorities are descending
        TreeMap<Integer, List<Class<?>>> priorityClassMap = getPriorityClassMap(classList, true);

        // For requests priorities are in ascending order
        for (Map.Entry<Integer, List<Class<?>>> entry : priorityClassMap.entrySet()) {
            for (Class<?> clazz : entry.getValue()) {
                responseFilterMap.get(clazz).filter(request, response);
            }
        }
    }

    /**
     * Get the priority class map for filter classes.
     *
     * @return priority class map
     */
    private TreeMap<Integer, List<Class<?>>> getPriorityClassMap(List<Class<?>> classList, boolean isReverseOrder) {

        TreeMap<Integer, List<Class<?>>> priorityClassMap = isReverseOrder
                ? new TreeMap<>(Collections.reverseOrder())
                : new TreeMap<>();

        for (Class<?> clazz : classList) {
            Integer priority = clazz.isAnnotationPresent(Priority.class)
                    ? clazz.getAnnotation(Priority.class).value()
                    : Priorities.USER;
            if (priorityClassMap.containsKey(priority)) {
                priorityClassMap.get(priority).add(clazz);
            } else {
                priorityClassMap.put(priority, new ArrayList<>());
                priorityClassMap.get(priority).add(clazz);
            }
        }
        return priorityClassMap;
    }
}
