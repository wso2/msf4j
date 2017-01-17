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
package org.wso2.msf4j.interceptor;

import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.exception.InterceptorException;
import org.wso2.msf4j.interceptor.annotation.RequestInterceptor;
import org.wso2.msf4j.interceptor.annotation.ResponseInterceptor;
import org.wso2.msf4j.internal.MicroservicesRegistryImpl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class for executing interceptors.
 */
public class InterceptorExecutor {

    private InterceptorExecutor() {
    }

    /**
     * Execute global request interceptors.
     *
     * @param microServicesRegistry current micro-service registry of {@link MicroservicesRegistryImpl}
     * @param request               {@link Request}
     * @param response              {@link Response}
     * @return is request interceptors successful
     * @throws InterceptorException {@link InterceptorException} on interception exception
     */
    public static boolean executeGlobalRequestInterceptors(MicroservicesRegistryImpl microServicesRegistry,
                                                           Request request, Response response)
            throws InterceptorException {
        List<MSF4JRequestInterceptor> globalRequestInterceptorList =
                microServicesRegistry.getGlobalRequestInterceptorList();
        return executeRequestInterceptors(request, response, globalRequestInterceptorList);
    }

    /**
     * Execute request interceptors annotated in class.
     *
     * @param microServicesRegistry current micro-service registry of {@link MicroservicesRegistryImpl}
     * @param request               {@link Request}
     * @param response              {@link Response}
     * @param resourceClass         method declaring class
     * @return is request interceptors successful
     * @throws InterceptorException {@link InterceptorException} on interception exception
     */
    public static boolean executeClassLevelRequestInterceptors(MicroservicesRegistryImpl microServicesRegistry,
                                                               Request request, Response response,
                                                               Class<?> resourceClass) throws InterceptorException {
        Map<Class<? extends MSF4JRequestInterceptor>, MSF4JRequestInterceptor> requestInterceptorMap =
                microServicesRegistry.getRequestInterceptorMap();
        List<Class<? extends MSF4JRequestInterceptor>> classRequestInterceptorClasses =
                resourceClass.isAnnotationPresent(RequestInterceptor.class)
                        ? Arrays.asList(resourceClass.getAnnotation(RequestInterceptor.class).value())
                        : new ArrayList<>();
        return executeRequestInterceptors(request, response, classRequestInterceptorClasses.stream()
                .filter(requestInterceptorMap::containsKey)
                .map(requestInterceptorMap::get)
                .collect(Collectors.toList()));
    }

    /**
     * Execute request interceptors annotated in method
     *
     * @param microServicesRegistry current micro-service registry of {@link MicroservicesRegistryImpl}
     * @param request               {@link Request}
     * @param response              {@link Response}
     * @param method                method to be executed
     * @return is request interceptors successful
     * @throws InterceptorException {@link InterceptorException} on interception exception
     */
    public static boolean executeMethodLevelRequestInterceptors(MicroservicesRegistryImpl microServicesRegistry,
                                                                Request request, Response response, Method method)
            throws InterceptorException {
        Map<Class<? extends MSF4JRequestInterceptor>, MSF4JRequestInterceptor> requestInterceptorMap =
                microServicesRegistry.getRequestInterceptorMap();
        List<Class<? extends MSF4JRequestInterceptor>> methodRequestInterceptorClasses =
                method.isAnnotationPresent(RequestInterceptor.class)
                        ? Arrays.asList(method.getAnnotation(RequestInterceptor.class).value())
                        : new ArrayList<>();
        return executeRequestInterceptors(request, response, methodRequestInterceptorClasses.stream()
                .filter(requestInterceptorMap::containsKey)
                .map(requestInterceptorMap::get)
                .collect(Collectors.toList()));
    }

    /**
     * Execute global response interceptors.
     *
     * @param microServicesRegistry current micro-service registry of {@link MicroservicesRegistryImpl}
     * @param request               {@link Request}
     * @param response              {@link Response}
     * @return is request interceptors successful
     * @throws InterceptorException {@link InterceptorException} on interception exception
     */
    public static boolean executeGlobalResponseInterceptors(MicroservicesRegistryImpl microServicesRegistry,
                                                            Request request, Response response)
            throws InterceptorException {
        List<MSF4JResponseInterceptor> globalResponseInterceptorList =
                microServicesRegistry.getGlobalResponseInterceptorList();
        return executeResponseInterceptors(request, response, globalResponseInterceptorList);
    }

    /**
     * Execute response interceptors annotated in class.
     *
     * @param microServicesRegistry current micro-service registry of {@link MicroservicesRegistryImpl}
     * @param request               {@link Request}
     * @param response              {@link Response}
     * @param resourceClass         method declaring class
     * @return is request interceptors successful
     * @throws InterceptorException {@link InterceptorException} on interception exception
     */
    public static boolean executeClassLevelResponseInterceptors(MicroservicesRegistryImpl microServicesRegistry,
                                                                Request request, Response response,
                                                                Class<?> resourceClass) throws InterceptorException {
        Map<Class<? extends MSF4JResponseInterceptor>, MSF4JResponseInterceptor> responseInterceptorMap =
                microServicesRegistry.getResponseInterceptorMap();
        List<Class<? extends MSF4JResponseInterceptor>> classResponseInterceptorClasses =
                resourceClass.isAnnotationPresent(ResponseInterceptor.class)
                        ? Arrays.asList(resourceClass.getAnnotation(ResponseInterceptor.class).value())
                        : new ArrayList<>();
        return executeResponseInterceptors(request, response, classResponseInterceptorClasses.stream()
                .filter(responseInterceptorMap::containsKey)
                .map(responseInterceptorMap::get)
                .collect(Collectors.toList()));
    }

    /**
     * Execute response interceptors annotated in class for a list of classes.
     *
     * @param microServicesRegistry current micro-service registry of {@link MicroservicesRegistryImpl}
     * @param request               {@link Request}
     * @param response              {@link Response}
     * @param classes               list of method declaring class
     * @return is request interceptors successful
     * @throws InterceptorException {@link InterceptorException} on interception exception
     */
    public static boolean executeClassResponseInterceptorsForClasses(MicroservicesRegistryImpl microServicesRegistry,
                                                                     Request request, Response response,
                                                                     List<Class<?>> classes)
            throws InterceptorException {
        for (Class<?> aClass : classes) {
            if (!(InterceptorExecutor.executeClassLevelResponseInterceptors(microServicesRegistry, request, response,
                    aClass))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Execute response interceptors annotated in method
     *
     * @param microServicesRegistry current micro-service registry of {@link MicroservicesRegistryImpl}
     * @param request               {@link Request}
     * @param response              {@link Response}
     * @param method                method to be executed
     * @return is request interceptors successful
     * @throws InterceptorException {@link InterceptorException} on interception exception
     */
    public static boolean executeMethodLevelResponseInterceptors(MicroservicesRegistryImpl microServicesRegistry,
                                                                 Request request, Response response, Method method)
            throws InterceptorException {
        Map<Class<? extends MSF4JResponseInterceptor>, MSF4JResponseInterceptor> responseInterceptorMap =
                microServicesRegistry.getResponseInterceptorMap();
        List<Class<? extends MSF4JResponseInterceptor>> methodResponseInterceptorClasses =
                method.isAnnotationPresent(ResponseInterceptor.class)
                        ? Arrays.asList(method.getAnnotation(ResponseInterceptor.class).value())
                        : new ArrayList<>();
        return executeResponseInterceptors(request, response, methodResponseInterceptorClasses.stream()
                .filter(responseInterceptorMap::containsKey)
                .map(responseInterceptorMap::get)
                .collect(Collectors.toList()));
    }

    /**
     * Execute response interceptors annotated in method for a list of methods.
     *
     * @param microServicesRegistry current micro-service registry of {@link MicroservicesRegistryImpl}
     * @param request               {@link Request}
     * @param response              {@link Response}
     * @param methods               list of methods to be executed
     * @return is request interceptors successful
     * @throws InterceptorException {@link InterceptorException} on interception exception
     */
    public static boolean executeMethodResponseInterceptorsForMethods(MicroservicesRegistryImpl microServicesRegistry,
                                                                      Request request, Response response,
                                                                      List<Method> methods)
            throws InterceptorException {
        for (Method resourceMethod : methods) {
            if (!(InterceptorExecutor.executeMethodLevelResponseInterceptors(microServicesRegistry, request,
                    response, resourceMethod))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Execute request interceptors.
     *
     * @param request  {@link Request}
     * @param response {@link Response}
     * @return is interception successful
     * @throws InterceptorException {@link InterceptorException} on interception exception
     */
    private static boolean executeRequestInterceptors(Request request, Response response,
                                                      Collection<MSF4JRequestInterceptor> requestInterceptors)
            throws InterceptorException {
        for (MSF4JRequestInterceptor interceptor : requestInterceptors) {
            try {
                if (!interceptor.interceptRequest(request, response)) {
                    return false;
                }
            } catch (Exception e) {
                if (!interceptor.onRequestInterceptionError(request, response, e)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Execute response interceptors.
     *
     * @param request  {@link Request}
     * @param response {@link Response}
     * @return is interception successful
     * @throws InterceptorException {@link InterceptorException} on interception exception
     */
    private static boolean executeResponseInterceptors(Request request, Response response,
                                                       Collection<MSF4JResponseInterceptor> responseInterceptors)
            throws InterceptorException {
        for (MSF4JResponseInterceptor interceptor : responseInterceptors) {
            try {
                if (!interceptor.interceptResponse(request, response)) {
                    return false;
                }
            } catch (Exception e) {
                if (!interceptor.onResponseInterceptionError(request, response, e)) {
                    return false;
                }
            }
        }
        return true;
    }
}
