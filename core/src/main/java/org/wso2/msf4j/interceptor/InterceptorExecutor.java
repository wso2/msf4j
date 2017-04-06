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
import org.wso2.msf4j.internal.MicroservicesRegistryImpl;
import org.wso2.msf4j.util.ReflectionUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
     */
    public static boolean executeGlobalRequestInterceptors(MicroservicesRegistryImpl microServicesRegistry,
                                                           Request request, Response response) {
        List<RequestInterceptor> globalRequestInterceptorList = microServicesRegistry.getGlobalRequestInterceptorList();
        return executeGlobalRequestInterceptors(request, response, globalRequestInterceptorList);
    }

    /**
     * Execute request interceptors annotated in class.
     *
     * @param request       {@link Request}
     * @param response      {@link Response}
     * @param resourceClass method declaring class
     * @return is request interceptors successful
     * @throws InterceptorException {@link InterceptorException} on interception exception
     */
    public static boolean executeClassLevelRequestInterceptors(Request request, Response response,
                                                               Class<?> resourceClass) throws InterceptorException {
        Collection<Class<? extends RequestInterceptor>> classRequestInterceptorClasses =
                resourceClass.isAnnotationPresent(org.wso2.msf4j.interceptor.annotation.RequestInterceptor.class)
                        ? Arrays.asList(resourceClass
                        .getAnnotation(org.wso2.msf4j.interceptor.annotation.RequestInterceptor.class).value())
                        : new ArrayList<>();
        return executeNonGlobalRequestInterceptors(request, response, classRequestInterceptorClasses);
    }

    /**
     * Execute request interceptors annotated in method
     *
     * @param request  {@link Request}
     * @param response {@link Response}
     * @param method   method to be executed
     * @return is request interceptors successful
     * @throws InterceptorException {@link InterceptorException} on interception exception
     */
    public static boolean executeMethodLevelRequestInterceptors(Request request, Response response, Method method)
            throws InterceptorException {
        Collection<Class<? extends RequestInterceptor>> methodRequestInterceptorClasses =
                method.isAnnotationPresent(org.wso2.msf4j.interceptor.annotation.RequestInterceptor.class)
                        ? Arrays.asList(method
                        .getAnnotation(org.wso2.msf4j.interceptor.annotation.RequestInterceptor.class).value())
                        : new ArrayList<>();
        return executeNonGlobalRequestInterceptors(request, response, methodRequestInterceptorClasses);
    }

    /**
     * Execute global response interceptors.
     *
     * @param microServicesRegistry current micro-service registry of {@link MicroservicesRegistryImpl}
     * @param request               {@link Request}
     * @param response              {@link Response}
     * @return is request interceptors successful
     */
    public static boolean executeGlobalResponseInterceptors(MicroservicesRegistryImpl microServicesRegistry,
                                                            Request request, Response response) {
        List<ResponseInterceptor> globalResponseInterceptorList =
                microServicesRegistry.getGlobalResponseInterceptorList();
        return executeGlobalResponseInterceptors(request, response, globalResponseInterceptorList);
    }

    /**
     * Execute response interceptors annotated in class.
     *
     * @param request       {@link Request}
     * @param response      {@link Response}
     * @param resourceClass method declaring class
     * @return is request interceptors successful
     * @throws InterceptorException {@link InterceptorException} on interception exception
     */
    public static boolean executeClassLevelResponseInterceptors(Request request, Response response,
                                                                Class<?> resourceClass) throws InterceptorException {
        List<Class<? extends ResponseInterceptor>> classResponseInterceptorClasses =
                resourceClass.isAnnotationPresent(org.wso2.msf4j.interceptor.annotation.ResponseInterceptor.class)
                        ? Arrays.asList(resourceClass
                        .getAnnotation(org.wso2.msf4j.interceptor.annotation.ResponseInterceptor.class).value())
                        : new ArrayList<>();
        return executeNonGlobalResponseInterceptors(request, response, classResponseInterceptorClasses);
    }

    /**
     * Execute response interceptors annotated in class for a list of classes.
     *
     * @param request  {@link Request}
     * @param response {@link Response}
     * @param classes  list of method declaring class
     * @return is request interceptors successful
     * @throws InterceptorException {@link InterceptorException} on interception exception
     */
    public static boolean executeClassResponseInterceptorsForClasses(Request request, Response response,
                                                                     List<Class<?>> classes)
            throws InterceptorException {
        if (classes == null) {
            return true;
        }
        for (Class<?> aClass : classes) {
            if (!(InterceptorExecutor.executeClassLevelResponseInterceptors(request, response, aClass))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Execute response interceptors annotated in method
     *
     * @param request  {@link Request}
     * @param response {@link Response}
     * @param method   method to be executed
     * @return is request interceptors successful
     * @throws InterceptorException {@link InterceptorException} on interception exception
     */
    public static boolean executeMethodLevelResponseInterceptors(Request request, Response response, Method method)
            throws InterceptorException {
        List<Class<? extends ResponseInterceptor>> methodResponseInterceptorClasses =
                method.isAnnotationPresent(org.wso2.msf4j.interceptor.annotation.ResponseInterceptor.class)
                        ? Arrays.asList(method
                        .getAnnotation(org.wso2.msf4j.interceptor.annotation.ResponseInterceptor.class).value())
                        : new ArrayList<>();
        return executeNonGlobalResponseInterceptors(request, response, methodResponseInterceptorClasses);
    }

    /**
     * Execute response interceptors annotated in method for a list of methods.
     *
     * @param request  {@link Request}
     * @param response {@link Response}
     * @param methods  list of methods to be executed
     * @return is request interceptors successful
     * @throws InterceptorException {@link InterceptorException} on interception exception
     */
    public static boolean executeMethodResponseInterceptorsForMethods(Request request, Response response,
                                                                      List<Method> methods)
            throws InterceptorException {
        if (methods == null) {
            return true;
        }
        for (Method resourceMethod : methods) {
            if (!(InterceptorExecutor.executeMethodLevelResponseInterceptors(request, response, resourceMethod))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Execute request interceptors.
     *
     * @param request             {@link Request}
     * @param response            {@link Response}
     * @param requestInterceptors request interceptor instances
     * @return is interception successful
     */
    private static boolean executeGlobalRequestInterceptors(Request request, Response response,
                                                            Collection<RequestInterceptor> requestInterceptors) {
        if (requestInterceptors == null) {
            return true;
        }
        for (RequestInterceptor interceptor : requestInterceptors) {
            if (!executeRequestInterceptor(interceptor, request, response)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Execute response interceptors.
     *
     * @param request              {@link Request}
     * @param response             {@link Response}
     * @param responseInterceptors response interceptor instances
     * @return is interception successful
     */
    private static boolean executeGlobalResponseInterceptors(Request request, Response response,
                                                             Collection<ResponseInterceptor> responseInterceptors) {
        if (responseInterceptors == null) {
            return true;
        }
        for (ResponseInterceptor interceptor : responseInterceptors) {
            if (!executeResponseInterceptor(interceptor, request, response)) {
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
     * @param classes  request interceptor classes
     * @return is interception successful
     * @throws InterceptorException {@link InterceptorException} on interception exception
     */
    private static boolean executeNonGlobalRequestInterceptors(
            Request request, Response response, Collection<Class<? extends RequestInterceptor>> classes)
            throws InterceptorException {

        Class<?>[] parameterTypes = new Class[]{};
        Object[] arguments = new Object[]{};

        if (classes == null) {
            return true;
        }

        for (Class<? extends RequestInterceptor> requestInterceptorClass : classes) {
            RequestInterceptor interceptor;
            try {
                interceptor = requestInterceptorClass.cast(ReflectionUtils
                        .createInstanceFromClass(requestInterceptorClass, parameterTypes, arguments));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                    InstantiationException e) {
                throw new InterceptorException("Error occurred when creating an instance type of the interceptor class "
                        + requestInterceptorClass, e);
            }

            if (!executeRequestInterceptor(interceptor, request, response)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Execute response interceptors.
     *
     * @param request  {@link Request}
     * @param response {@link Response}
     * @param classes  response interceptor classes
     * @return is interception successful
     * @throws InterceptorException {@link InterceptorException} on interception exception
     */
    private static boolean executeNonGlobalResponseInterceptors(
            Request request, Response response, Collection<Class<? extends ResponseInterceptor>> classes)
            throws InterceptorException {

        Class<?>[] parameterTypes = new Class[]{};
        Object[] arguments = new Object[]{};

        if (classes == null) {
            return true;
        }

        for (Class<? extends ResponseInterceptor> responseInterceptorClass : classes) {
            ResponseInterceptor interceptor;
            try {
                interceptor = responseInterceptorClass.cast(ReflectionUtils
                        .createInstanceFromClass(responseInterceptorClass, parameterTypes, arguments));
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException |
                    InstantiationException e) {
                throw new InterceptorException("Error occurred when creating an instance type of the interceptor class "
                        + responseInterceptorClass, e);
            }

            if (!executeResponseInterceptor(interceptor, request, response)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Execute a single request interceptor.
     *
     * @param interceptor request interceptor
     * @param request     request instance
     * @param response    response instance
     * @return interceptor result
     */
    private static boolean executeRequestInterceptor(RequestInterceptor interceptor, Request request,
                                                     Response response) {
        try {
            return interceptor.interceptRequest(request, response);
        } catch (Exception e) {
            return interceptor.onRequestInterceptionError(request, response, e);
        }
    }

    /**
     * Execute a single response interceptor.
     *
     * @param interceptor response interceptor
     * @param request     request instance
     * @param response    response instance
     * @return interceptor result
     */
    private static boolean executeResponseInterceptor(ResponseInterceptor interceptor, Request request,
                                                      Response response) {
        try {
            return interceptor.interceptResponse(request, response);
        } catch (Exception e) {
            return interceptor.onResponseInterceptionError(request, response, e);
        }
    }
}
