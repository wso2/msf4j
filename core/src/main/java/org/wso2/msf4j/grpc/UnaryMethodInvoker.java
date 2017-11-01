/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.msf4j.grpc;

import com.google.protobuf.Descriptors;
import io.grpc.stub.ServerCalls.UnaryMethod;
import io.grpc.stub.StreamObserver;
import org.wso2.msf4j.grpc.exception.GrpcServerException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This is Unary Method Implementation for gRPC Service Call.
 */
public class UnaryMethodInvoker implements UnaryMethod<Object, Object> {
    public static final String FIELD_SUFFIX = "_";
    private final Object serviceToInvoke;
    private final Method method;
    private final Class paramType;
    private final Class returnType;
    private final Descriptors.MethodDescriptor methodDescriptor;

    public UnaryMethodInvoker(Object serviceToInvoke, Method method, Descriptors.MethodDescriptor methodDescriptor)
            throws GrpcServerException {
        this.serviceToInvoke = serviceToInvoke;
        this.method = method;
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length > 1) {
            throw new GrpcServerException("service should have single input parameter, but service has " + paramTypes
                    .length);
        }
        this.paramType = paramTypes[0];
        this.returnType = method.getReturnType();
        this.methodDescriptor = methodDescriptor;
    }


    @Override
    public void invoke(Object request, StreamObserver<Object> responseObserver) {
        try {
            Object paramObject;
            List<Descriptors.FieldDescriptor> fieldDescriptors = methodDescriptor.getInputType().getFields();

            if (paramType.isAnnotationPresent(XmlRootElement.class)) {
                paramObject = AccessController.doPrivileged(new InputRequestElementMapper(request, fieldDescriptors));

            } else {
                if (fieldDescriptors.size() > 1) {
                    throw new RuntimeException("Service method " + method.getName() + " not handle multiple input " +
                            "parameters. ");
                }
                paramObject = AccessController.doPrivileged(new InputRequestFieldMapper(request, fieldDescriptors));

            }

            Object returnObj = method.invoke(serviceToInvoke, paramObject);
            Method returnMethod = Class.forName(methodDescriptor.getOutputType().getFullName()).getMethod
                    ("newBuilder");
            Object builderObject = returnMethod.invoke(null);
            if (returnType.isAnnotationPresent(XmlRootElement.class)) {
                AccessController.doPrivileged(new OutputResponseElementMapper(returnObj, builderObject));

            } else {
                AccessController.doPrivileged(new OutputResponseFieldMapper(returnObj, builderObject));

            }
            Method buildMethod = builderObject.getClass().getMethod("build");
            responseObserver.onNext(buildMethod.invoke(builderObject));
        } catch (PrivilegedActionException | IllegalAccessException | InvocationTargetException | NoSuchMethodException
                | ClassNotFoundException ex) {
            responseObserver.onError(ex);
        } finally {
            responseObserver.onCompleted();
        }
    }

    private static String capitalize(String name) {
        if (name != null && name.length() != 0) {
            char[] chars = name.toCharArray();
            chars[0] = Character.toUpperCase(chars[0]);
            return new String(chars);
        } else {
            return name;
        }
    }

    private class InputRequestElementMapper implements PrivilegedExceptionAction<Object> {

        final Object request;
        final List<Descriptors.FieldDescriptor> fieldDescriptors;

        InputRequestElementMapper(Object request, List<Descriptors.FieldDescriptor> fieldDescriptors) {
            this.request = request;
            this.fieldDescriptors = fieldDescriptors;
        }

        @Override
        public Object run() throws Exception {
            try {
                Object paramObject = paramType.newInstance();
                for (Descriptors.FieldDescriptor fieldDescriptor : fieldDescriptors) {
                    String fieldName = fieldDescriptor.getName();
                    if (fieldName != null) {
                        Field field = request.getClass().getDeclaredField(fieldName + FIELD_SUFFIX);
                        field.setAccessible(true);
                        Object value = field.get(request);
                        field.setAccessible(false);
                        Field matchedField = paramType.getDeclaredField(fieldName);
                        matchedField.setAccessible(true);
                        matchedField.set(paramObject, value);
                        matchedField.setAccessible(false);
                    }
                }
                return paramObject;
            } catch (NoSuchFieldException | InstantiationException | IllegalAccessException e) {
                throw new GrpcServerException("Error while mapping request inputs to service method " +
                        "params", e);
            }
        }
    }

    private static class InputRequestFieldMapper implements PrivilegedExceptionAction<Object> {

        final Object request;
        final List<Descriptors.FieldDescriptor> fieldDescriptors;

        InputRequestFieldMapper(Object request, List<Descriptors.FieldDescriptor> fieldDescriptors) {
            this.request = request;
            this.fieldDescriptors = fieldDescriptors;
        }

        @Override
        public Object run() throws Exception {
            try {
                String fieldName = fieldDescriptors.get(0).getName();
                Field field = request.getClass().getDeclaredField(fieldName + FIELD_SUFFIX);
                field.setAccessible(true);
                Object paramObject =  field.get(request);
                field.setAccessible(false);
                return paramObject;
            } catch (NoSuchFieldException | IllegalAccessException  e) {
                throw new GrpcServerException("Error while mapping request inputs to service method " +
                        "params", e);
            }
        }
    }

    private class OutputResponseElementMapper implements PrivilegedExceptionAction<Object> {
        final Object returnObject;
        final Object builderObject;

        OutputResponseElementMapper(Object returnObject, Object builderObject) {
            this.returnObject = returnObject;
            this.builderObject = builderObject;
        }

        @Override
        public Object run() throws Exception {
            try {
                Field[] responseFields = returnType.getDeclaredFields();
                for (Field field : responseFields) {
                    Method setterMethod = builderObject.getClass().getMethod("set" + capitalize(field
                            .getName()), field.getType());
                    field.setAccessible(true);
                    setterMethod.invoke(builderObject, field.get(returnObject));
                    field.setAccessible(false);
                }
                return builderObject;
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new GrpcServerException("Error while mapping response types to service response" , e);
            }

        }
    }

    private static class OutputResponseFieldMapper implements PrivilegedExceptionAction<Object> {
        final Object returnObject;
        final Object builderObject;

        OutputResponseFieldMapper(Object returnObject, Object builderObject) {
            this.returnObject = returnObject;
            this.builderObject = builderObject;
        }

        @Override
        public Object run() throws Exception {
            try {
                Field[] returnFields = builderObject.getClass().getDeclaredFields();
                for (Field field : returnFields) {
                    String fieldName = field.getName();
                    if (fieldName != null && fieldName.endsWith("_")) {
                        String paramName = fieldName.substring(0, fieldName.lastIndexOf('_'));
                        field.setAccessible(true);
                        Method setterMethod = builderObject.getClass().getMethod("set" + capitalize
                                (paramName), field.get(builderObject).getClass());
                        setterMethod.invoke(builderObject, returnObject);
                    }
                }
                return builderObject;
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new GrpcServerException("Error while mapping response types to service response" , e);
            }
        }
    }
}
