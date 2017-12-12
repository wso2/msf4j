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

import org.wso2.msf4j.grpc.exception.GrpcServerException;

/**
 * gRPC service builder initializes the gRPC server. deploys the microservices and starts the relevant transports.
 */
public interface ServerBuilder {

    /**
     * initialized Server Builder instance which will be used for deploying gRPC services.
     * Allows specifying ports on which the microservices in this Builder are deployed.
     *
     * @param port The port on which the microservices are exposed
     */
    public void init(int port);

    /**
     * Deploy a microservice.
     *
     * @param microservice The microservice which is to be deployed
     * @return this Server Builder object
     */
    public ServerBuilder addService(Object... microservice);

    /**
     * Shutdown grpc server.
     */
    public void stop();

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    public void blockUntilShutdown() throws InterruptedException;

    /**
     * Register all microservices in Server.
     * @return  this Server Builder object
     * @throws GrpcServerException exception when there is an error in registering microservices.
     */
    public ServerBuilder register() throws GrpcServerException;

    /**
     * Start this gRPC server. This will startup all the gRPC services.
     * @throws GrpcServerException exception when there is an error in starting the server.
     */
    public void start() throws GrpcServerException;
}
