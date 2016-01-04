# Metrics and HTTP Monitoring MSS Deployable Jar Sample

This sample shows how to develop and deploy a microservice as a mss deployable jar.

Please see [Metrics and HTTP Monitoring Executable Jar Sample](../metrics-httpmon-mss-lite) for configuring WSO2 Data Analytics Server (DAS) and using annotations.

See also [Metrics and HTTP Monitoring MSS Bundle Sample](../metrics-httpmon-mss-bundle).


## How to build the sample
------------------------------------------

From this directory, run

```
mvn clean install
```

## How to run the sample
------------------------------------------

Unzip WSO2 MSS product and navigate to the bin directory. Then run the following command to start the MSS server.
```
./carbon.sh -DMETRICS_ENABLED=true -DHTTP_MONITORING_ENABLED=true
```

Note that we have passed two parameters to enable Metrics Service and HTTP Monitoring Data Publisher.

The copy the target/metrics-httpmon-mss-deployable-jar-1.0.0-SNAPSHOT.jar to deployment/microservices directory of WSO2 MSS.
Then the jar will be automatically deployed to the server runtime.


## How to test the sample
------------------------------------------

Use following cURL commands.
```
curl http://localhost:8080/demo-jar/rand/500

curl http://localhost:8080/demo-jar/total/10

curl http://localhost:8080/demo-jar/echo/test

```
