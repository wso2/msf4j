package org.wso2.carbon.mss.stockquote;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.mss.Microservice;

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
import javax.ws.rs.core.Response;

/**
 * StockQuote microservice.
 */
@Component(
        name = "org.wso2.carbon.mss.stockquote.StockQuoteService",
        service = Microservice.class,
        immediate = true
)
@Path("/stockquote")
public class StockQuoteService implements Microservice {

    private static final Logger log = LoggerFactory.getLogger(StockQuoteService.class);
    private Map<String, Stock> stockQuotes = new HashMap<>();

    /**
     * Add initial stocks IBM, GOOG, AMZN.
     */
    public StockQuoteService() {
        stockQuotes.put("IBM", new Stock("IBM", "International Business Machines", 149.62, 150.78, 149.18));
        stockQuotes.put("GOOG", new Stock("GOOG", "Alphabet Inc.", 652.30, 657.81, 643.15));
        stockQuotes.put("AMZN", new Stock("AMZN", "Amazon.com", 548.90, 553.20, 543.10));
    }

    @Activate
    protected void activate(BundleContext bundleContext){
        // Nothing to do
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext){
        // Nothing to do
    }


    /**
     * Retrieve a stock for a given symbol.
     * http://localhost:8080/stockquote/IBM
     *
     * @param symbol Stock symbol will be taken from the path parameter.
     * @return
     */
    @GET
    @Path("/{symbol}")
    @Produces({"application/json", "text/xml"})
    public Response getQuote(@PathParam("symbol") String symbol) {
        Stock stock = stockQuotes.get(symbol);
        return (stock == null) ?
                Response.status(Response.Status.NOT_FOUND).build() :
                Response.status(Response.Status.OK).entity(stock).build();
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
        stockQuotes.put(stock.getSymbol(), stock);
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
    public List<Stock> getAllStocks() {
        List<Stock> stocks = new ArrayList<>();
        stocks.addAll(stockQuotes.values());
        return stocks;
    }

    @Override
    public String toString() {
        return "StockQuoteService-OSGi-bundle";
    }
}
