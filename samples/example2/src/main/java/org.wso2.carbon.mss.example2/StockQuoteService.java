package org.wso2.carbon.mss.example2;

import com.google.gson.JsonObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.wso2.carbon.mss.AbstractHttpHandler;
import org.wso2.carbon.mss.HttpResponder;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

@Path("/SimpleStockQuote")
public class StockQuoteService extends AbstractHttpHandler {

    // http://localhost:7778/StockQuote/get/IBM

    private Map<String, Double> stockQuotes = new HashMap<String, Double>();

    public StockQuoteService() {
        stockQuotes.put("IBM", 77.45);
        stockQuotes.put("GOOG", 200.65);
        stockQuotes.put("AMZN", 145.88);
    }

    @GET
    @Path("getQuote")
    @Consumes("application/json")
    @Produces("application/json")
    public void getQuote(HttpRequest request, HttpResponder responder, @QueryParam("symbol") String symbol) {
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

    @Override
    public String toString() {
        return "StockQuoteService2{}";
    }
}