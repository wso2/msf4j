package org.wso2.carbon.mss.example2;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * StockQuote microservice
 */
@Path("/SimpleStockQuote")
public class StockQuoteService {

    private static final Logger log = LoggerFactory.getLogger(StockQuoteService.class);

    // http://localhost:7778/StockQuote/get/IBM

    private Map<String, Double> stockQuotes = new HashMap<>();

    public StockQuoteService() {
        stockQuotes.put("IBM", 77.45);
        stockQuotes.put("GOOG", 200.65);
        stockQuotes.put("AMZN", 145.88);
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

    @Override
    public String toString() {
        return "StockQuoteService2{}";
    }
}
