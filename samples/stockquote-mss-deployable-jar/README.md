# SimpleStockQuote Deployable jar Sample

This sample shows how to develop and deploy a microservice as a mss deployable jar.
See also.. [mss lite](../stockquote-mss-lite), [mss bundle](../stockquote-mss-bundle)


## How to build the sample
------------------------------------------

From this directory, run

```
mvn clean install
```

## How to run the sample
------------------------------------------

Unzip wso2 MSS product and navigate to the bin directory. Then run the following command to start the MSS server.
```
./wso2server.sh
```

The copy the target/stockquote-mss-deployable-jar-1.0.0-SNAPSHOT.jar to deployment/microservices directory of WSO2 MSS.
Then the jar will be automatically deployed to the server runtime.


## How to test the sample
------------------------------------------

Use following cURL commands.
```
curl http://localhost:8080/stockquote/IBM
```
