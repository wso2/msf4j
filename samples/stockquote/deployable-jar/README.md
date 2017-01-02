# SimpleStockQuote Deployable jar Sample

This sample demonstrates how to create a deployable microservice for hot deployment.

In this sample we have exposed the StockQuoteService as an deployable microservice that implements
org.wso2.msf4j.Microservice interface as shown in the following code.
 See also [msf4j bundle](../bundle), [msf4j fatjar](../fatjar)

```java
@Path("/stockquote")
public class StockQuoteService implements Microservice {

    // resource methods are here..

}
```

You have to add full classpath of the microservice class in <microservice.resourceClasses> under properties in the pom
.xml of deployable jar as shown in the following code.

```xml
    <properties>
        <microservice.resourceClasses>org.wso2.msf4j.stockquote.example.StockQuoteService</microservice.resourceClasses>
    </properties>
```

## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample

First you need to install the MSF4J feature and Deployment feature in to carbon kernel. To do go to the resource
directory at [msf4j bundle](../bundle) and execute the following command

```
mvn clean install
```

This will install the msf4j and other required features to the kernel and build up a product.
You can find the product in the resources/target directory

Go to the target/wso2msf4j-<-version->/bin directory
Then run the following command to start the MSF4J server.

```
./carbon.sh
```

The copy the target/stockquote-deployable-jar-<-version->.jar to deployment/microservices directory of MSF4J server.
Then the jar will be automatically deployed to the server runtime.

## How to test the sample

Use following cURL commands.
```
curl http://localhost:9090/stockquote/IBM
```