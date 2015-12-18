# SimpleStockQuote MSS Bundle Sample

This sample demonstrates how to create a microservice as an OSGi bundle.
See also.. [mss lite](../stockquote-mss-lite), [mss deployable jar](../stockquote-mss-deployable-jar)

In this sample we have exposed the StockQuoteService as an OSGi service that implements 
org.wso2.carbon.mss.Microservice interface as shown in the following code.

```java
@Component(
        name = "org.wso2.carbon.mss.stockquote.StockQuoteService",
        service = Microservice.class,
        immediate = true
)
@Path("/stockquote")
public class StockQuoteService implements Microservice {

    @Activate
    protected void activate(BundleContext bundleContext){
        // Nothing to do
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext){
        // Nothing to do
    }
    
    // resource methods are here..
}
```
Note the empty @Activate and @Deactivate methods that are required to properly generate the bundle headers that are 
required by the framework.


## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample

### Using OSGi Console

Unzip wso2 MSS product and navigate to the bin directory. Then run the following command to start the MSS server.
```
./carbon.sh
```

Install the target/stockquote-mss-bundle-1.0.0-SNAPSHOT.jar as an OSGi bundle to WSO2 MSS product using it's 
OSGi console with the following command.

```
install file://<path to target directory>/stockquote-mss-bundle-1.0.0-SNAPSHOT.jar
```

When the installation of the bundle is successful, use the bundle ID of the installed bundle to start  
it. Use the following command in the OSGi console for that.

```
start <bundle ID>
```

When the bundle is started, the microservice that is exposed as an OSGi service will be picked by the runtime and 
will be exposed as a REST service.

### Using dropins directory
Unzip wso2 MSS product and copy target/stockquote-mss-bundle-1.0.0-SNAPSHOT.jar bundle to the 
"[SERVER-HOME]/osgi/dropins" directory.

Then navigate to the bin directory and run the following command to start WSO2 MSS server.
```
./carbon.sh
```
When the server is being started, the bundle in the dropins directory will be automatically 
loaded and it's microservices that are there as OSGi services will be exposed as REST services.


## How to test the sample

Use following cURL commands.
```
curl http://localhost:8080/stockquote/IBM
```