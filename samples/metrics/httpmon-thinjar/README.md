# Metrics and HTTP Monitoring MSF4J Thin Jar Sample

This sample shows how to develop and deploy a microservice as an MSF4J thin jar.

Please see [Metrics and HTTP Monitoring Fat Jar Sample](../httpmon-fatjar) 
for configuring WSO2 Data Analytics Server (DAS) and using annotations.

See also [Metrics and HTTP Monitoring MSF4J Bundle Sample](../httpmon-bundle).


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

The copy the target/metrics-httpmon-thinjar-1.0.0.jar to deployment/microservices directory of WSO2 MSF4J.
Then the jar will be automatically deployed to the server runtime.


## How to test the sample



Use following cURL commands.
```
curl http://localhost:8080/demo-jar/rand/500

curl http://localhost:8080/demo-jar/total/10

curl http://localhost:8080/demo-jar/echo/test

```
