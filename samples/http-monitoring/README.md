# HTTP Monitoring Sample

This sample shows the use of HTTP Monitoring Interceptor.

HTTP Monitoring events are published to WSO2 Data Analytics Server (DAS).

The HTTP Monitoring Interceptor is configured using a YAML file. The default "http-monitoring.yaml" is included in final the JAR.

The HTTP Monitoring is disabled by default. In this sample, there is a custom http-monitoring.yaml file is added to enable the 
Data Publisher to publish HTTP events to WSO2 DAS.

This samples also uses Metrics Interceptor and the Metrics is configured to publish events to WSO2 DAS. There is a metrics.yaml 
file added to configure the Metrics DAS Reporter.

See also the [Metrics sample](../metrics).

### HTTP Monitoring Annotation

Use the @HTTPMonitored annotation when you want to monitor each HTTP request. 

See [HTTP Monitoring Annotation](../../../../#http-monitoring-annotation) for more details.

See the following example to use the annotation at the Class level.

```java
@Path("/demo")
@HTTPMonitored
public class DemoService {

}
```

See the following example to use the annotation at the Method level.


```java
    @GET
    @Path("/{nic}")
    @Produces("application/json")
    @HTTPMonitored
    public Student getStudent(@PathParam("nic") String nic) {
        return students.get(nic);
    }
```

**How to add and initialize HTTP Monitoring Interceptor**

```java
new MicroservicesRunner().addInterceptor(new HTTPMonitoringInterceptor())
```

## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample

You must first configure WSO2 Data Analytics Server (DAS) and run it in order to receive the events published by this sample.
Please refer the [analytics documentation](../../analytics)
for more information on configuring WSO2 DAS.

## Running the sample

Note: The Metrics DAS Reporters have a "Polling Period" in seconds. This is the period for polling metrics from the metric registry 
and reporting to WSO2 DAS. The HTTP Monitoring Interceptor sends events for each request.

Run the JAR file in target directory.

```
java -jar target/http-monitoring*.jar
OR
java -Dmsf4j.conf=<MSF4J Deployment YAML configuration path> -jar target/http-monitoring*.jar
```

## How to test the sample

Use the following cURL commands.
```
curl -v http://localhost:8080/student/910760234V

curl -v --data "{'nic':'860766123V','firstName':'Jack','lastName':'Black','age':29}" -H "Content-Type: application/json" http://localhost:8080/student

curl -v http://localhost:8080/student/860766123V

curl -v http://localhost:8080/student

```

**Analytics Dashboard in WSO2 Data Analytics Server**

The HTTP Monitoring events sent by this sample can be seen from the HTTP Monitoring Dashboard in WSO2 DAS.
You can access the dashboard from [http://localhost:9763/monitoring/](http://localhost:9763/monitoring/).
