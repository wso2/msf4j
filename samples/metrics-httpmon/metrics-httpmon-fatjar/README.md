# Metrics and HTTP Monitoring Fat Jar Sample

This sample shows the use of Metrics Interceptor and HTTP Monitoring Interceptor with MicroservicesRunner. 
See also [Metrics and HTTP Monitoring MSF4J Bundle](../metrics-httpmon-bundle).

HTTP Monitoring events are published to WSO2 Data Analytics Server (DAS). Metrics can also be published to WSO2 DAS.

### Metrics Annotations

There are three metrics annotations: @Counted, @Metered and @Timed.

Use @Counted annotation when you need to count the method invocations. The @Metered annotation can measure the rate of events.
Use @Timed to keep a histogram of durations of each method invocation. 

The @Metered annotation also keeps a count. The @Timed annotation keeps the count and rate of events as well.

See [Metrics Annotations](../../../../../#metrics-annotations) for more details.

See the following example:

```java
@Path("/demo")
public class DemoService {

    private final Random random = new Random();

    private long total = 0L;

    @GET
    @Path("/rand/{bound}")
    @Metered
    public int getRandomInt(@PathParam("bound") int bound) {
        return random.nextInt(bound);
    }

    @GET
    @Path("/echo/{string}")
    @Timed
    public String echo(@PathParam("string") String string) {
        try {
            Thread.sleep(random.nextInt(5000));
        } catch (InterruptedException e) {
        }
        return string;
    }

    @GET
    @Path("/total/{number}")
    @Counted
    public long getTotal(@PathParam("number") int number) {
        return total = total + number;
    }

}
```

**How to add and initialize Metrics Interceptor**

The init() method accepts the MetricReporter types.

```java
new MicroservicesRunner()
    .addInterceptor(new MetricsInterceptor().init(MetricReporter.CONSOLE, MetricReporter.JMX, MetricReporter.DAS))
```

### HTTP Monitoring Annotation

Use the @HTTPMonitored annotation when you want to monitor each HTTP request. 

See [HTTP Monitoring Annotation](../../../../../#http-monitoring-annotation) for more details.

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
new MicroservicesRunner().addInterceptor(new HTTPMonitoringInterceptor().init())
```

## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample

You must first configure WSO2 Data Analytics Server (DAS) and run it in order to recieve the events published by this sample.
Please refer the [analytics documentation](../../../analytics) 
for more information on configuring WSO2 DAS.

For "Metrics Interceptor", there are several reporters supported.

**Configuring Reporters for Metrics Interceptor**

This sample uses Console, JMX and WSO2 DAS reporters. 
Configuration options can be provided as environment variables or system properties.

| Property                                  | Purpose      |
| ----------------------------------------- | ------------ |
| METRICS_REPORTING_CONSOLE_ENABLED         | This property sets the enabled status for the Metrics Console Reporter. |
| METRICS_REPORTING_CONSOLE_POLLINGPERIOD   | This is the period for polling metrics from the metric registry and  printing in the console. Polling Period is in seconds. Default is 60 | 
| METRICS_REPORTING_JMX_ENABLED             | This property sets the enabled status for the JMX Reporter | 
| METRICS_REPORTING_DAS_ENABLED             | This property sets the enabled status for the DAS Reporter | 
| METRICS_REPORTING_DAS_SOURCE              | The source for metrics. The hostname is used by default. | 
| METRICS_REPORTING_DAS_TYPE                | The DAS event protocol used for data publishing. Default value is "thrift" | 
| METRICS_REPORTING_DAS_RECEIVERURL         | The URL of the DAS receiver that receives the metrics published by MSF4J. Default value is "tcp://localhost:7611" | 
| METRICS_REPORTING_DAS_AUTHURL             | The authorization URL used to access DAS. | 
| METRICS_REPORTING_DAS_USERNAME            | The username used to access DAS. Default is "admin" | 
| METRICS_REPORTING_DAS_PASSWORD            | The password used to access DAS. Default is "admin" | 
| METRICS_REPORTING_DAS_DATAAGENTCONFIGPATH | The path to the data agent configuration file. | 
| METRICS_REPORTING_DAS_POLLINGPERIOD       | This is the period for polling metrics from the metric registry and sending events via the Data Publisher. Polling Period is in seconds. Default is 60 | 

For example:

```
# Console Reporter
export METRICS_REPORTING_CONSOLE_ENABLED=true
export METRICS_REPORTING_CONSOLE_POLLINGPERIOD=60

# JMX Reporter
export METRICS_REPORTING_JMX_ENABLED=true

# WSO2 DAS Reporter
export METRICS_REPORTING_DAS_ENABLED=true
# export METRICS_REPORTING_DAS_SOURCE
export METRICS_REPORTING_DAS_TYPE="thrift"
export METRICS_REPORTING_DAS_RECEIVERURL="tcp://localhost:7611"
# export METRICS_REPORTING_DAS_AUTHURL
export METRICS_REPORTING_DAS_USERNAME="admin"
export METRICS_REPORTING_DAS_PASSWORD="admin"
export METRICS_REPORTING_DAS_DATAAGENTCONFIGPATH="data-agent-conf.xml"
export METRICS_REPORTING_DAS_POLLINGPERIOD=60
```

All of the above configurations have default values except for `METRICS_REPORTING_DAS_DATAAGENTCONFIGPATH`.
You must give the path for a data agent configuration file, which is already provided in this sample as "data-agent-conf.xml"


**Configuring HTTP Monitoring Interceptor**

Following are the configuration options.

| Property                                | Purpose      |
| --------------------------------------- | ------------ |
| HTTP_MONITORING_DAS_TYPE                | The DAS event protocol used for data publishing. Default value is "thrift" | 
| HTTP_MONITORING_DAS_RECEIVERURL         | The URL of the DAS receiver that receives the metrics published by MSF4J. Default value is "tcp://localhost:7611" | 
| HTTP_MONITORING_DAS_AUTHURL             | The authorization URL used to access DAS. | 
| HTTP_MONITORING_DAS_USERNAME            | The username used to access DAS. Default is "admin" | 
| HTTP_MONITORING_DAS_PASSWORD            | The password used to access DAS. Default is "admin" | 
| HTTP_MONITORING_DAS_DATAAGENTCONFIGPATH | The path to the data agent configuration file. | 

Example:

```
export HTTP_MONITORING_DAS_TYPE="thrift"
export HTTP_MONITORING_DAS_RECEIVERURL="tcp://localhost:7611"
# export HTTP_MONITORING_DAS_AUTHURL
export HTTP_MONITORING_DAS_USERNAME="admin"
export HTTP_MONITORING_DAS_PASSWORD="admin"
export HTTP_MONITORING_DAS_DATAAGENTCONFIGPATH="data-agent-conf.xml"
```

Here, the `HTTP_MONITORING_DAS_DATAAGENTCONFIGPATH` is a required configuration as well, and the other configurations have default values.

## Running the sample

Note: Console and WSO2 DAS Reporters have a "Polling Period" in seconds. This is the period for 
polling metrics from the metric registry and reporting to Console and WSO2 DAS. The HTTP Monitoring Interceptor sends 
events for each request.


Use the following command to run the application,

```
java -DMETRICS_REPORTING_DAS_DATAAGENTCONFIGPATH="data-agent-conf.xml" -DHTTP_MONITORING_DAS_DATAAGENTCONFIGPATH="data-agent-conf.xml" -jar target/metrics-httpmon-fatjar*.jar
```

You can also run the following,

```
export METRICS_REPORTING_DAS_DATAAGENTCONFIGPATH="data-agent-conf.xml"
export HTTP_MONITORING_DAS_DATAAGENTCONFIGPATH="data-agent-conf.xml"
java -jar target/metrics-httpmon-fatjar*.jar
```


## How to test the sample



Use the following cURL commands.
```
curl -v http://localhost:8080/demo/rand/500

curl -v http://localhost:8080/demo/total/10

curl -v http://localhost:8080/demo/echo/test

curl -v http://localhost:8080/student/910760234V

curl -v --data "{'nic':'860766123V','firstName':'Jack','lastName':'Black','age':29}" -H "Content-Type: application/json" http://localhost:8080/student

curl -v http://localhost:8080/student/860766123V

curl -v http://localhost:8080/student

```

**Console Output**

After running the above cURL commands, you should see the metrics output to the console periodically.


**Analytics Dashboard in WSO2 Data Analytics Server**

The HTTP Monitoring events sent by this sample can be seen from the HTTP Monitoring Dashboard in WSO2 DAS.
You can access the dashboard from [http://localhost:9763/monitoring/](http://localhost:9763/monitoring/).
