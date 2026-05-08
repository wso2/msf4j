/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Retrieves stockquotes
 */
public class StockQuoteDatabase {

    private static final Logger log = LoggerFactory.getLogger(StockQuoteDatabase.class);
    private Random random = new Random();

    // Map that stores stocks (symbol -> stock).
    private Map<String, Stock> stockQuotes = new HashMap<>();

    private Map<String, Stock> stockQuotesCache = new HashMap<>();

    /**
     * Add initial stocks IBM, GOOG, AMZN.
     */
    public StockQuoteDatabase() {
        stockQuotes.put("IBM", new Stock("IBM", "International Business Machines", 149.62, 150.78, 149.18));
        stockQuotes.put("GOOG", new Stock("GOOG", "Alphabet Inc.", 652.30, 657.81, 643.15));
        stockQuotes.put("AMZN", new Stock("AMZN", "Amazon.com", 548.90, 553.20, 543.10));
    }

    public Stock getStock(String symbol) {
        // To simulate failures, timeout & circuit breaker behaviour, this method will randomly fail

        int rand = random.nextInt(3);
        if (rand == 0) { // Simulate a timeout
            log.info("Failed. Timeout!");
            delay(50);
        } else if (rand == 1) {  // Simulate a failure
            log.info("Failed. Exception!");
            throw new RuntimeException("Failed. Exception!");
        }
        log.info("No failure");
        Stock stock = stockQuotes.get(symbol);
        stockQuotesCache.put(symbol, stock);
        return stock;
    }

    private void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

    public Stock getCachedStock(String symbol) {
        return stockQuotesCache.get(symbol);
    }
}
