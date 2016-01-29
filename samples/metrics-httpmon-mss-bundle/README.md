# Metrics and HTTP Monitoring MSF4J Bundle Sample

This sample demonstrates how to create a microservice as an OSGi bundle.

Please see [Metrics and HTTP Monitoring Executable Jar Sample](../metrics-httpmon-msf4j-lite) for configuring WSO2 Data Analytics Server (DAS) and using annotations.

See also [Metrics and HTTP Monitoring MSF4J Deployable Jar](../metrics-httpmon-msf4j-deployable-jar).

In this sample we have exposed the DemoService as an OSGi service.

See following example:

```java
@Path("/demo")
@HTTPMonitoring
@Component(
        name = "org.wso2.msf4j.example.service.DemoService",
        service = Microservice.class,
        immediate = true
)
public class DemoService implements Microservice {

}
```

## How to build the sample



From this directory, run

```
mvn clean install
```

## How to run the sample



### Using OSGi Console

Unzip WSO2 MSF4J product and navigate to the bin directory. Then run the following command to start the MSF4J server.
```
./carbon.sh -DMETRICS_ENABLED=true -DHTTP_MONITORING_ENABLED=true
```

Note that we have passed two parameters to enable Metrics Service and HTTP Monitoring Data Publisher.

Install the target/metrics-httpmon-msf4j-bundle-1.0.0-SNAPSHOT.jar as an OSGi bundle to WSO2 MSF4J product using it's 
OSGi console with the following command.

```
install file://<path to target directory>/metrics-httpmon-msf4j-bundle-1.0.0-SNAPSHOT.jar
```

When the installation of the bundle is successful, use the bundle ID of the installed bundle to start  
it. Use the following command in the OSGi console for that.

```
start <bundle ID>
```

When the bundle is started, the microservice that is exposed as an OSGi service will be picked by the runtime and 
will be exposed as a REST service.

### Using dropins directory
Unzip WSO2 MSF4J product and copy target/metrics-httpmon-msf4j-bundle-1.0.0-SNAPSHOT.jar bundle to the 
"[SERVER-HOME]/osgi/dropins" directory.

Then navigate to the bin directory and run the following command to start WSO2 MSF4J server.
```
./carbon.sh -DMETRICS_ENABLED=true -DHTTP_MONITORING_ENABLED=true
```
When the server is being started, the bundle in the dropins directory will be automatically 
loaded and it's microservices that are there as OSGi services will be exposed as REST services.


## How to test the sample

Use following cURL commands.
```
curl http://localhost:8080/demo-bundle/rand/500

curl http://localhost:8080/demo-bundle/total/10

curl http://localhost:8080/demo-bundle/echo/test

```