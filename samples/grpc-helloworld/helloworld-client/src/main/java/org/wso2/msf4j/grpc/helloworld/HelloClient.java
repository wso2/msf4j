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
package org.wso2.msf4j.grpc.helloworld;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;

/**
 * HelloWorld Client Sample.
 */
public class HelloClient {
    private final ManagedChannel channel;
    private final HelloServiceGrpc.HelloServiceBlockingStub blockingStub;

    // Call Greet Method in HelloWorld Service.
    public static void main(String[] args) throws Exception {
        HelloClient client = new HelloClient("localhost", 9090);
        try {
            // Default user
            String user = "WSO2";
            // Use the arg as the name to greet if provided
            if (args.length > 0) {
                user = args[0];
            }
            client.greet(user);
        } finally {
            client.shutdown();
        }
    }

    // Construct Client Server Connection.
    private HelloClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build());
    }

    // Creating Blocking Stub using the existing channel.
    private HelloClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = HelloServiceGrpc.newBlockingStub(channel);
    }

    private void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    // Call Greet Method in Hello server.
    private void greet(String name) {
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloResponse response;
        try {
            response = blockingStub.hello(request);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("Greeting: " + response.getMessage());
    }
}
