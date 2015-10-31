# Metrics Interceptor Sample

This sample demonstrate how to use the Metrics Interceptor with MicroservicesRunner.

How to build the sample
------------------------------------------
From this directory, run

```
mvn clean install
```

How to run the sample
------------------------------------------

Use following command to run the application
```
java -jar target/metrics-1.0.0.jar
```
Configuring Reporters
------------------------------------------
This sample uses console & JMX reporters. Configuration options can be provided as environment variables.

For example:

```
export METRICS_REPORTING_CONSOLE_POLLINGPERIOD=5
```

How to test the sample
------------------------------------------

Use following cURL commands.
```
curl -v http://localhost:8080/test/rand/500

curl -v http://localhost:8080/test/total/10

curl -v http://localhost:8080/test/echo/test

curl -v http://localhost:8080/student/910760234V

curl -v --data "{'nic':'860766123V','firstName':'Jack','lastName':'Black','age':29}" -H "Content-Type: application/json" http://localhost:8080/student

curl -v http://localhost:8080/student/860766123V

curl -v http://localhost:8080/student

```

Console Output
------------------------------------------
After running the above cURL commands, you should see metrics output to the console.

```
TODO IsuruP
```

Analytics Dashboard in WSO2 Data Analytics Server
------------------------------------------

Running this sample will also publish data to WSO2 DAS. In order to configure WSO2 DAS for Microservices analytics,
please see the [analytics documentation](https://github.com/wso2/product-mss/tree/master/analytics)


