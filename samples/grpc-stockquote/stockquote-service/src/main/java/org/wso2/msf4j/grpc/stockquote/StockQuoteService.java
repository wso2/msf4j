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

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.HashMap;
import java.util.Map;

/**
 * StockQuote sample. This service will be available at.
 * http://localhost:8080/stockquote
 */
@Path("/stockquote")
public class StockQuoteService {

    // Map that stores stocks (symbol -> stock).
    private Map<String, Stock> stockQuotes = new HashMap<>();

    /**
     * Add initial stocks IBM, GOOG, AMZN.
     */
    public StockQuoteService() {
        stockQuotes.put("IBM", new Stock("IBM", "International Business Machines", 149.62, 150.78, 149.18));
        stockQuotes.put("GOOG", new Stock("GOOG", "Alphabet Inc.", 652.30, 657.81, 643.15));
        stockQuotes.put("AMZN", new Stock("AMZN", "Amazon.com", 548.90, 553.20, 543.10));
    }

    /**
     * Retrieve a stock for a given symbol.
     * http://localhost:8080/stockquote/IBM
     *
     * @param symbol Stock symbol will be taken from the path parameter.
     * @return Response
     */
    @GET
    @Path("/{symbol}")
    @Produces({"application/json", "text/xml"})
    public Stock getQuote(@PathParam("symbol") String symbol) {
        System.out.println("Getting stock details for symbol: " + symbol);
        Stock stock = stockQuotes.get(symbol);
        return stock;
    }


    /**
     * Add a new stock.
     * curl -v -X POST -H "Content-Type:application/json" \
     * -d '{"symbol":"BAR","name": "Bar Inc.", \
     * "last":149.62,"low":150.78,"high":149.18,
     * "createdByHost":"10.100.1.192"}' \
     * http://localhost:8080/stockquote
     *
     * @param stock Stock object will be created from the request Json body.
     */
    @POST
    @Consumes("application/json")
    public void addStock(Stock stock) {
        String symbol = stock.getSymbol();
        if (stockQuotes.containsKey(symbol)) {
            System.out.println("Symbol " + symbol + " already exists");
            return;
        }
        System.out.println("Symbol " + symbol + " is added successfully");
        stockQuotes.put(symbol, stock);
    }

    private Class getProtoService() {
        return StockQuoteServiceProto.class;
    }
}
