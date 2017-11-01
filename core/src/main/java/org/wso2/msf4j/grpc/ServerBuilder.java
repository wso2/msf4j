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
import io.grpc.MethodDescriptor;
import io.grpc.Server;
import io.grpc.ServerServiceDefinition;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ServerCalls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.grpc.exception.GrpcServerException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * gRPC service Implementation.
 */
public class ServerBuilder {
    private static final String FILE_DESCRIPTOR_METHOD = "getDescriptor";
    private static final String DEFAULT_INSTANCE_METHOD = "getDefaultInstance";
    public static final int DEFAULT_PORT = 8080;
    public static final String PROTO_SERVICE_METHOD = "getProtoService";
    private io.grpc.ServerBuilder serverBuilder = null;
    private final List<Object> serviceList = new ArrayList<Object>();;
    private Class protoType;
    private Server server = null;
    private static final Logger log = LoggerFactory.getLogger(ServerBuilder.class);

    public ServerBuilder(int port) {
        serverBuilder = io.grpc.ServerBuilder.forPort(port);
    }

    public ServerBuilder addService(Object... microservice) {
        Arrays.asList(microservice).forEach(service -> {
            try {
                this.serviceList.add(service);
                this.addProtoType(service);
            } catch (GrpcServerException e) {
                log.error("Error while registering migRPC proto service class is not set in microservice: " +
                        microservice.getClass().getName());
            }
        });
        return this;
    }

    private ServerBuilder addProtoType(Object service) throws GrpcServerException {
        try {
            Method method = service.getClass().getDeclaredMethod(PROTO_SERVICE_METHOD, null);
            method.setAccessible(true);
            this.protoType = (Class<?>) method.invoke(service);
            return this;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new GrpcServerException("Error while initializing Grpc service.", e);
        }
    }

    public ServerBuilder register() throws GrpcServerException {
        if (protoType == null) {
            throw new GrpcServerException("gRPC service proto file definition is undefined. You need to set proto " +
                    "class to setup gRPC service");
        }

        Descriptors.FileDescriptor fileDescriptor = null;
        try {
            Method paramMethod = protoType.getMethod(FILE_DESCRIPTOR_METHOD);
            fileDescriptor = (Descriptors.FileDescriptor) paramMethod.invoke(null);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new GrpcServerException("Error while retrieving gRPC file descriptor." + e);
        }
        if (fileDescriptor == null) {
            throw new GrpcServerException("Proto file descriptor cannot be null You need to set proto class to " +
                    "setup gRPC service");
        }

        for (Object serviceToInvoke : serviceList) {
            Descriptors.ServiceDescriptor serviceDescriptor = fileDescriptor.findServiceByName(serviceToInvoke.getClass
                    ().getSimpleName());
            if (serviceDescriptor == null) {
                log.error("Service descriptor not found for the service " + serviceToInvoke.getClass
                        ().getName());
                continue;
            }

            io.grpc.ServerServiceDefinition.Builder serviceDefBuilder = ServerServiceDefinition
                    .builder(serviceDescriptor.getFullName());

            Method[] declaredMethods = serviceToInvoke.getClass().getDeclaredMethods();
            for (Method exposedMethod : declaredMethods) {
                if (PROTO_SERVICE_METHOD.equals(exposedMethod.getName())) {
                    continue;
                }

                Class<?> [] paramTypes = exposedMethod.getParameterTypes();
                if (paramTypes.length > 1) {
                    throw new GrpcServerException("cannot handle multiple input parameters. parameter size: " +
                            paramTypes.length);
                }

                Descriptors.MethodDescriptor methodDescriptor = serviceDescriptor.findMethodByName(exposedMethod
                        .getName());
                if (methodDescriptor == null) {
                    log.error("Method descriptor not found for the method " + exposedMethod.getName());
                    continue;
                }

                final String methodName = serviceDescriptor.getFullName() + "/" + methodDescriptor.getName();

                MethodDescriptor.Marshaller reqMarshaller;
                MethodDescriptor.Marshaller resMarshaller;
                try {
                    Method paramMethod = Class.forName(methodDescriptor.getInputType().getFullName()).getMethod
                            (DEFAULT_INSTANCE_METHOD);
                    reqMarshaller = ProtoUtils.marshaller((com.google.protobuf.Message)
                            paramMethod.invoke(null));
                    Method returnMethod = Class.forName(methodDescriptor.getOutputType().getFullName()).getMethod
                            (DEFAULT_INSTANCE_METHOD);
                    resMarshaller = ProtoUtils.marshaller((com.google.protobuf.Message)
                            returnMethod.invoke(null));
                } catch (NoSuchMethodException | ClassNotFoundException |
                        IllegalAccessException | InvocationTargetException e) {
                    throw new GrpcServerException("Error while retrieving default instance of input and return " +
                            "message." + e);
                }

                if (methodDescriptor.toProto().hasServerStreaming()
                        || methodDescriptor.toProto().hasClientStreaming()) {
                    throw new UnsupportedOperationException("gRPC Streaming services are currently not supported");
                }

                MethodDescriptor grpcMethodDescriptor = MethodDescriptor.newBuilder().setType(MethodDescriptor
                        .MethodType.UNARY).setFullMethodName(methodName)
                        .setRequestMarshaller(reqMarshaller)
                        .setResponseMarshaller(resMarshaller)
                        .setSchemaDescriptor(methodDescriptor).build();

                ServerCalls.UnaryMethod<Object, Object> methodInvokation = new UnaryMethodInvoker(serviceToInvoke,
                        exposedMethod, methodDescriptor);
                serviceDefBuilder.addMethod(grpcMethodDescriptor, ServerCalls.asyncUnaryCall(methodInvokation));
            }
            serverBuilder.addService(serviceDefBuilder.build());
        }
        server = serverBuilder.build();
        return this;
    }


    public void start() throws GrpcServerException {
        if (server != null) {
            try {
                server.start();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    log.info("Shutting down gRPC server since JVM is shutting down");
                    ServerBuilder.this.stop();
                }));
            } catch (IOException e) {
                throw new GrpcServerException("Error while starting gRPC server", e);
            }
        } else {
            throw new GrpcServerException("No gRPC service is registered to start. You need to register the service");
        }
    }

    /**
     * Shutdown grpc server
     */
    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

}
