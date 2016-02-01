# Metrics and HTTP Monitoring MSF4J Deployable Jar Sample

This sample shows how to develop and deploy a microservice as a msf4j deployable jar.

Please see [Metrics and HTTP Monitoring Executable Jar Sample](../metrics-httpmon-msf4j-lite) for configuring WSO2 Data Analytics Server (DAS) and using annotations.

See also [Metrics and HTTP Monitoring MSF4J Bundle Sample](../metrics-httpmon-msf4j-bundle).


## How to build the sample



From this directory, run

```
mvn clean install
```

## How to run the sample



Unzip WSO2 MSF4J product and navigate to the bin directory. Then run the following command to start the MSF4J server.
```
./carbon.sh -DMETRICS_ENABLED=true -DHTTP_MONITORING_ENABLED=true
```

Note that we have passed two parameters to enable Metrics Service and HTTP Monitoring Data Publisher.

The copy the target/metrics-httpmon-msf4j-deployable-jar-1.0.0-SNAPSHOT.jar to deployment/microservices directory of WSO2 MSF4J.
Then the jar will be automatically deployed to the server runtime.


## How to test the sample



Use following cURL commands.
```
curl http://localhost:8080/demo-jar/rand/500

curl http://localhost:8080/demo-jar/total/10

curl http://localhost:8080/demo-jar/echo/test

```
