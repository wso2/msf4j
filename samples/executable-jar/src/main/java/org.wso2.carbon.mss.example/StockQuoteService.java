package org.wso2.carbon.mss.example;

import com.google.gson.JsonObject;
import org.wso2.carbon.mss.MicroservicesRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

/**
 * StockQuote sample
 */
@Path("/StockQuote")
public class StockQuoteService {

    // http://localhost:8080/StockQuote/get/IBM

    private Map<String, Stock> stockQuotes = new HashMap<>();

    public StockQuoteService() {
        stockQuotes.put("IBM", new Stock("IBM", "International Business Machines", 149.62, 150.78, 149.18));
        stockQuotes.put("GOOG", new Stock("GOOG", "Alphabet Inc.", 652.30, 657.81, 643.15));
        stockQuotes.put("AMZN", new Stock("AMZN", "Amazon.com", 548.90, 553.20, 543.10));
    }

    public static void main(String[] args) {
        new MicroservicesRunner().deploy(new StockQuoteService()).start();
    }

    @GET
    @Path("/{symbol}")
    @Produces({"application/json", "text/xml"})
    public Response getQuote(@PathParam("symbol") String symbol) {
        Stock stock = stockQuotes.get(symbol);
        return (stock == null) ?
                Response.status(Response.Status.NOT_FOUND).build() :
                Response.status(Response.Status.OK).entity(stock).build();
    }

    /*
    curl -v -X POST -H "Content-Type:application/json"
    -d '{"symbol":"BAR","name": "Bar Inc.","last":149.62,"low":150.78,"high":149.18,"createdByHost":"10.100.1.192"}'
    http://localhost:8080/StockQuote/
     */
    @POST
    @Consumes("application/json")
    public void addStock(Stock stock) {
        stockQuotes.put(stock.getSymbol(), stock);
    }

    @GET
    @Path("/all")
    @Produces("application/json")
    public List<Stock> getAllStocks() {
        List<Stock> stocks = new ArrayList<>();
        stocks.addAll(stockQuotes.values());
        return stocks;
    }

    @GET
    @Consumes("application/json")
    @Produces("application/json")
    public Response getQuote() {
        return getQuote("IBM");
    }

    @Path("/v1/ping")
    @GET
    public String testGet() {
        return "OK";
    }

    // The HTTP endpoint v1/apps/deploy will be handled by the deploy method given below
    @Path("deploy")
    @POST
    public Response deploy() {
        // ..
        // Deploy application and send status
        // ..
        return Response.status(Response.Status.OK).build();
    }

    // The HTTP endpoint v1/apps/deploy will be handled by the deploy method given below
    @Path("deploy")
    @GET
    public Response deploy2() {
        // ..
        // Deploy application and send status
        // ..

        return Response.status(Response.Status.OK).build();
    }

    // The HTTP endpoint v1/apps/{id}/status will be handled by the status method given below
    @Path("{id}/status/{x}/{y}")
    @GET
    public String status(@PathParam("id") String id,
                         @PathParam("x") int x, @PathParam("y") long y) {
        // The id that is passed in HTTP request will be mapped to a String via the PathParam annotation
        // ..
        // Retrieve status the application
        // ..
        JsonObject status = new JsonObject();
        status.addProperty("status", "RUNNING");
        status.addProperty("id", id);
        status.addProperty("x", x);
        status.addProperty("y", y);
        return status.toString();
    }

    // The HTTP endpoint v1/apps/{id}/status will be handled by the status method given below
    @Path("/status2")
    @GET
    public String status2(@QueryParam("z") String z) {
        // The id that is passed in HTTP request will be mapped to a String via the PathParam annotation
        // ..
        // Retrieve status the application
        // ..
        JsonObject status = new JsonObject();
        status.addProperty("status", "RUNNING");
        status.addProperty("z", z);
        return status.toString();
    }

    @Override
    public String toString() {
        return "StockQuoteService{}";
    }
}
