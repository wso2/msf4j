package org.wso2.carbon.mss.example;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.mss.AbstractHttpHandler;
import org.wso2.carbon.mss.MicroservicesRunner;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
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
public class StockQuoteService extends AbstractHttpHandler {

    private static final Logger log = LoggerFactory.getLogger(StockQuoteService.class);

    // http://localhost:8080/StockQuote/get/IBM

    private Map<String, Double> stockQuotes = new HashMap<String, Double>();

    public StockQuoteService() {
        stockQuotes.put("IBM", 77.45);
        stockQuotes.put("GOOG", 200.65);
        stockQuotes.put("AMZN", 145.88);
    }

    public static void main(String[] args) {
        new MicroservicesRunner().deploy(new StockQuoteService()).start();
    }

    @GET
    @Path("get/{symbol}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response getQuote(@PathParam("symbol") String symbol) {
        Double price = stockQuotes.get(symbol);
        if (price != null) {
            JsonObject response = new JsonObject();
            response.addProperty("symbol", symbol);
            response.addProperty("price", price);
            try {
                response.addProperty("ip", InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                log.error("Could not get IP address of local host", e);
            }
            return Response.status(Response.Status.OK).entity(response).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
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
