/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.msf4j.example;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Contact;
import io.swagger.annotations.Info;
import io.swagger.annotations.License;
import io.swagger.annotations.SwaggerDefinition;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.example.exception.DuplicateSymbolException;
import org.wso2.msf4j.example.exception.SymbolNotFoundException;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * StockQuote sample. This service will be available at.
 * http://localhost:8080/stockquote
 */
@Api(value = "stockquote")
@SwaggerDefinition(
        info = @Info(
                title = "Stockquote Swagger Definition", version = "1.0",
                description = "Stock quote service",
                license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0"),
                contact = @Contact(
                        name = "Afkham Azeez",
                        email = "azeez@wso2.com",
                        url = "http://wso2.com"
                ))
)
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
    @ApiOperation(
            value = "Return stock quote corresponding to the symbol",
            notes = "Returns HTTP 404 if the symbol is not found")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Valid stock item found"),
            @ApiResponse(code = 404, message = "Stock item not found")})
    public Response getQuote(@ApiParam(value = "Symbol", required = true)
                             @PathParam("symbol") String symbol) throws SymbolNotFoundException {
        System.out.println("Getting symbol using PathParam...");
        Stock stock = stockQuotes.get(symbol);
        if (stock == null) {
            throw new SymbolNotFoundException("Symbol " + symbol + " not found");
        }
        return Response.status(Response.Status.OK).entity(stock).build();
    }

    /**
     * Retrieve metainformation about the entity implied by the request.
     * curl -i -X HEAD http://localhost:8080/stockquote/IBM
     *
     * @return Response
     */
    @HEAD
    @Path("/{symbol}")
    @Produces({"application/json", "text/xml"})
    @ApiOperation(
            value = "Returns headers of corresponding GET request ",
            notes = "Returns metainformation contained in the HTTP header identical to the corresponding GET Request")
    public Response getMetaInformationForQuote(@ApiParam(value = "Symbol", required = true)
                                               @PathParam("symbol") String symbol) throws SymbolNotFoundException {
        Stock stock = stockQuotes.get(symbol);
        if (stock == null) {
            throw new SymbolNotFoundException();
        }
        return Response.status(Response.Status.OK).build();
    }

    /**
     * Retrieve a stock for a given symbol using a cookie.
     * This method demonstrates the CookieParam JAXRS annotation in action.
     *
     * curl -v --header "Cookie: symbol=IBM" http://localhost:8080/stockquote
     *
     * @param symbol Stock symbol will be taken from the symbol cookie.
     * @return Response
     */
    @GET
    @Produces({"application/json", "text/xml"})
    @ApiOperation(
            value = "Return stock quote corresponding to the symbol",
            notes = "Returns HTTP 404 if the symbol is not found")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Valid stock item found"),
            @ApiResponse(code = 404, message = "Stock item not found")})
    public Response getQuoteUsingCookieParam(@ApiParam(value = "Symbol", required = true)
                                             @CookieParam("symbol") String symbol) throws SymbolNotFoundException {
        System.out.println("Getting symbol using CookieParam...");
        Stock stock = stockQuotes.get(symbol);
        if (stock == null) {
            throw new SymbolNotFoundException("Symbol " + symbol + " not found");
        }
        return Response.status(Response.Status.OK).entity(stock).build();
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
    @ApiOperation(
            value = "Add a stock item",
            notes = "Add a valid stock item")
    public void addStock(@ApiParam(value = "Stock object", required = true) Stock stock)
            throws DuplicateSymbolException {
        String symbol = stock.getSymbol();
        if (stockQuotes.containsKey(symbol)) {
            throw new DuplicateSymbolException("Symbol " + symbol + " already exists");
        }
        stockQuotes.put(symbol, stock);
    }

    /**
     * Retrieve all stocks.
     * http://localhost:8080/stockquote/all
     *
     * @return All stocks will be sent to the client as Json/xml
     * according to the Accept header of the request.
     */
    @GET
    @Path("/all")
    @Produces({"application/json", "text/xml"})
    @ApiOperation(
            value = "Get all stocks",
            notes = "Returns all stock items",
            response = Stocks.class,
            responseContainer = "List")
    public Stocks getAllStocks(@Context Request request) {
        request.getHeaders().getAll().forEach(entry -> System.out.println(entry.getName() + "=" + entry.getValue()));
        return new Stocks(stockQuotes.values());
    }

    /**
     * Retrieve information on what methods are allowed on the Request-URI.
     * curl -i -X OPTIONS http://localhost:8080/stockquote/all
     *
     * @return Response
     */
    @OPTIONS
    @Path("/all")
    @ApiOperation(
            value = "Get supported reuest methods",
            notes = "Return a response with headers that show the supported HTTP Requests on the Request-URI")
    public Response getCommunicationInformationForRequestURI(){
        return Response.status(Response.Status.OK).header("Access-Control-Allow-Methods","GET,OPTIONS").build();
    }
}
