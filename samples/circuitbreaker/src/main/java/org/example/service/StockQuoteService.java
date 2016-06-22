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

package org.example.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * This is the Microservice resource class.
 * See <a href="https://github.com/wso2/msf4j#getting-started">https://github.com/wso2/msf4j#getting-started</a>
 * for the usage of annotations.
 *
 */

@Path("/stockquote")
public class StockQuoteService {

    private StockQuoteDatabase db = new StockQuoteDatabase();

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
    public Response getQuote(@PathParam("symbol") String symbol) {
        Stock stock = new CircuitBreakerCommand(db, symbol).execute();
        return (stock == null) ?
                Response.status(Response.Status.NOT_FOUND).build() :
                Response.status(Response.Status.OK).entity(stock).build();
    }
}
