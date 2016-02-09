# SimpleStockQuote Fat jar Sample

A fat jar is a jar file which includes all the dependencies in one fat (uber) jar. This mode of creating a fat jar
and running it in a Java process is also referred to as server-less execution.

This sample shows the use of @Produces and @Consumes annotations for bean conversions. In addition to that this 
demonstrates how to develop a microservice with WSO2 MSF4J, create a fat jar & run it.

See also; [msf4j bundle](../bundle)

## Writing the pom.xml 

Your POM can inherit from [msf4j-service](../../../poms/msf4j-service). 
See details [here](../../../poms/msf4j-service).

### How @Produces work

When you need to return a bean from a resource method you can specify @Produces annotation with mime types you need to 
support. In this case, at runtime WSO2 MSF4J framework matches the request's Accept header with the @Produce annotation 
and dispatches the correct resource method. Then, when the response is sent, the returned bean will be automatically 
serialized to the matched mime type. See the following example.

```java
    @GET
    @Path("/{symbol}")
    @Produces({"application/json", "text/xml"})
    public Response getQuote(@PathParam("symbol") String symbol) {
        Stock stock = stockQuotes.get(symbol);
        return (stock == null) ?
                Response.status(Response.Status.NOT_FOUND).build() :
                Response.status(Response.Status.OK).entity(stock).build();
    }
```

In the above example, if the requests Accept header is application/json, then the Stock bean object will be serialized 
as JSON and will be sent as the response. In the same way if the requests Accept header is text/xml, then the bean 
will be serialized as XML and will be sent as the respond.


### How @Consumes work

If a request body is JSON or XML, then you can receive a bean for it as shown in the following example.

```java
    @POST
    @Consumes("application/json")
    public void addStock(Stock stock) {
        stockQuotes.put(stock.getSymbol(), stock);
    }
```

In the above example, since @Consumes is set to application/json, if a JSON object that matches to the type of stock 
parameter is arrived in the request body, a Stock object will be automatically populated from it and passed to the 
resource method.


## How to build the sample


From this directory, run

```
mvn clean install
```

## How to run the sample


Use following command to run the application
```
java -jar target/stockquote-fatjar-*.jar
```

## How to test the sample


Use following cURL commands.
```
curl http://localhost:8080/stockquote/IBM
```

You should get a successful response if everything worked fine.

## Invoking the service via HTTPS
Use following command to run the application
```
java -jar -Dtransports.netty.conf=netty-transports.yml target/stockquote-fatjar-*.jar
```

The above command will expose the service via HTTP on port 8080 & HTTPS on 8443. Invoke the service as follows:

```
curl --insecure https://localhost:8443/stockquote/IBM
```

You should get a successful response if everything worked fine.
