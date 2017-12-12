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
package org.wso2.msf4j.grpc.stockquote;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;

/**
 * StockQoute Client Sample.
 */
public class StockQuoteClient {
    private final ManagedChannel channel;
    private final StockQuoteServiceGrpc.StockQuoteServiceBlockingStub blockingStub;

    // Call StockQuote Service to add and query stocks.
    public static void main(String[] args) throws Exception {
        StockQuoteClient client = new StockQuoteClient("localhost", 8080);
        try {
            // Add new Stock to the Stock Qoute Service.
            StockQuote stockQoute = StockQuote.newBuilder().setName("WSO2 Lanka (Pvt) Ltd").setSymbol("WSO2").setHigh
                    (654).setLow(10).setLast(200).build();
            client.blockingStub.addStock(stockQoute);
            // Read WSO2 Stock from Stock Qoute Service.
            String user = "WSO2";
            StockQuote response = client.getQuote(user);
            System.out.println("Stock Name:" + response.getName() + " | Symbol:" + response.getSymbol() + " | Max " +
                    "Price" + response.getHigh());

        } finally {
            client.shutdown();
        }
    }

    // Construct Client Server Connection.
    public StockQuoteClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build());
    }

    // Creating Blocking Stub using the existing channel.
    private StockQuoteClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = StockQuoteServiceGrpc.newBlockingStub(channel);
    }

    // Shutdown connection
    private void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    // Get Quote details from Stock Qoute service
    private StockQuote getQuote(String name) {
        StockRequest request = StockRequest.newBuilder().setName(name).build();
        StockQuote response = null;
        try {
            response = blockingStub.getQuote(request);
        } catch (StatusRuntimeException e) {
            e.printStackTrace();
        }
        return response;
    }


}
