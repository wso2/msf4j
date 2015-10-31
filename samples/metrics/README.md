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
