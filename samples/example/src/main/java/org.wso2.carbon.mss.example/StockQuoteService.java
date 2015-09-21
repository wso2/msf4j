package org.wso2.carbon.mss.example;

import co.cask.http.AbstractHttpHandler;
import co.cask.http.BodyConsumer;
import co.cask.http.HttpResponder;
import com.google.gson.JsonObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import javax.ws.rs.*;
import java.util.HashMap;
import java.util.Map;

@Path("/StockQuote")
public class StockQuoteService extends AbstractHttpHandler {

    // http://localhost:7778/StockQuote/get/IBM

    private Map<String, Double> stockQuotes = new HashMap<String, Double>();

    public StockQuoteService() {
        stockQuotes.put("IBM", 77.45);
        stockQuotes.put("GOOG", 200.65);
        stockQuotes.put("AMZN", 145.88);
    }

    @GET
    @Path("get/{symbol}")
    @Consumes("application/json")
    @Produces("application/json")
    public void getQuote(HttpRequest request, HttpResponder responder, final @PathParam("symbol") String symbol) {
//    public void getQuote(HttpRequest request, HttpResponder responder, final @PathParam("symbol") String symbol) {
//    public BodyConsumer getQuote(HttpRequest request, HttpResponder responder, final @PathParam("symbol") String symbol) {
        StockQuoteService.this.getQuote(responder, symbol);

        /*return new BodyConsumer() {
            @Override
            public void chunk(ChannelBuffer request, HttpResponder responder) {
                // write the incoming data to a file
                System.out.println("--- chunk");
            }
            @Override
            public void finished(HttpResponder responder) {
               StockQuoteService.this.getQuote(responder, symbol);
             }
            @Override
            public void handleError(Throwable cause) {
                // if there were any error during this process, this will be called.
                // do clean-up here.
                System.out.println("--- handle error");
            }
        };*/
    }

    private void getQuote(HttpResponder responder, String symbol) {
        /*System.out.println("-----------------------------------");
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            System.out.println(ste);
        }
        System.out.println("-----------------------------------");*/
        Double price = stockQuotes.get(symbol);
        if (price != null) {
            JsonObject response = new JsonObject();
            response.addProperty("symbol", symbol);
            response.addProperty("price", price);
            responder.sendJson(HttpResponseStatus.OK, response);
        } else {
            responder.sendStatus(HttpResponseStatus.NOT_FOUND);
        }
    }

    @Path("/v1/ping")
    @GET
    public void testGet(HttpRequest request, HttpResponder responder) {
        responder.sendString(HttpResponseStatus.OK, "OK");
    }

    // The HTTP endpoint v1/apps/deploy will be handled by the deploy method given below
    @Path("deploy")
    @POST
    public void deploy(HttpRequest request, HttpResponder responder) {
        // ..
        // Deploy application and send status
        // ..

        responder.sendStatus(HttpResponseStatus.OK);
    }

    // The HTTP endpoint v1/apps/deploy will be handled by the deploy method given below
    @Path("deploy")
    @GET
    public void deploy2(HttpRequest request, HttpResponder responder) {
        // ..
        // Deploy application and send status
        // ..

        responder.sendStatus(HttpResponseStatus.OK);
    }

    // The HTTP endpoint v1/apps/{id}/status will be handled by the status method given below
    @Path("{id}/status/{x}/{y}")
    @GET
    public void status(HttpRequest request, HttpResponder responder, @PathParam("id") String id,
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
        responder.sendJson(HttpResponseStatus.OK, status);
    }


    // The HTTP endpoint v1/apps/{id}/status will be handled by the status method given below
    @Path("/status2")
    @GET
    public void status2(HttpRequest request, HttpResponder responder, @QueryParam("z") String z) {
        // The id that is passed in HTTP request will be mapped to a String via the PathParam annotation
        // ..
        // Retrieve status the application
        // ..
        JsonObject status = new JsonObject();
        status.addProperty("status", "RUNNING");
        status.addProperty("z", z);
        responder.sendJson(HttpResponseStatus.OK, status);
    }


    @Override
    public String toString() {
        return "StockQuoteService{}";
    }
}