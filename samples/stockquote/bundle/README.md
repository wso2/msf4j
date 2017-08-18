# SimpleStockQuote MSF4J OSGi Bundle Sample

This sample demonstrates how to create a microservice as an OSGi bundle.
See also; [msf4j fatjar](../fatjar)

In this sample we have exposed the StockQuoteService as an OSGi service that implements 
org.wso2.msf4j.Microservice interface as shown in the following code.

```java
@Component(
        name = "org.wso2.msf4j.stockquote.StockQuoteService",
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

Copy and extract the wso2-msf4j-*.zip file in tests/test-distribution to some location.
Go to the wso2-msf4j-*/wso2/default/bin directory
Then run the following command to start the MSF4J server.
```
./carbon.sh
```

Install the target/stockquote-bundle-*.jar as an OSGi bundle to WSO2 MSF4J product using it's 
OSGi console with the following command.

```
install file://<path to target directory>/stockquote-bundle-2.0.0.jar
```

When the installation of the bundle is successful, use the bundle ID of the installed bundle to start  
it. Use the following command in the OSGi console for that.

```
start <bundle ID>
```

When the bundle is started, the microservice that is exposed as an OSGi service will be picked by the runtime and 
will be exposed as a REST service.

### Using dropins directory
Unzip wso2 MSF4J product and copy target/stockquote-bundle-*.jar bundle to the 
"[SERVER-HOME]/lib" directory.

Then navigate to the bin directory and run the following command to start WSO2 MSF4J server.
```
./carbon.sh
```
When the server is being started, the bundle in the dropins directory will be automatically 
loaded and it's microservices that are there as OSGi services will be exposed as REST services.


## How to test the sample

Use following cURL commands.
```
curl http://localhost:9090/stockquote/IBM
```