# Metrics Sample

This sample shows the use of Metrics Interceptor. The metrics needs to be configured using a YAML file.

The default "metrics.yml" is included in final the JAR. In this sample, there is a custom metrics.yml file added to configure
the Metrics Reporters.

Metrics can also be published to WSO2 Data Analytics Server (DAS). 

See the [HTTP Monitoring sample](../http-monitoring) for an example of publishing Metrics events to WSO2 DAS.

### Metrics Annotations

There are three metrics annotations: @Counted, @Metered and @Timed.

Use @Counted annotation when you need to count the method invocations. The @Metered annotation can measure the rate of events.
Use @Timed to keep a histogram of durations of each method invocation. 

The @Metered annotation also keeps a count. The @Timed annotation keeps the count and rate of events as well.

See [Metrics Annotations](../../../../#metrics-annotations) for more details.

See the following example:

```java
@Path("/demo")
public class DemoService {

    private final Random random = new Random();

    private final LongAdder longAdder = new LongAdder();

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
    @Path("/count/{number}")
    @Counted(monotonic = true)
    public long getCount(@PathParam("number") long number) {
        longAdder.add(number);
        return longAdder.longValue();
    }

}
```

**How to add and initialize Metrics Interceptor**

The init() method accepts the MetricReporter types.

```java
new MicroservicesRunner().addInterceptor(new MetricsInterceptor())
```

## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample

Run the JAR file in target directory.

```
java -jar target/metrics*.jar
OR
java -Dmetrics.config=<Metrics YAML configuration file path> -jar target/metrics*.jar
```

## How to test the sample

You can use following cURL commands.
```
curl -v http://localhost:8080/demo/rand/500

curl -v http://localhost:8080/demo/count/10

curl -v http://localhost:8080/demo/echo/test
```

**Metrics Reporters**

See the [metrics.yaml](src/main/resources/metrics.yaml) configuration.

The JMX, Console, CSV and SLF4J reporters are configured in this sample.
