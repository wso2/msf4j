# Sample for Metrics and HTTP Monitoring Interceptors

This sample demonstrate how to use the Metrics Interceptor and HTTP Monitoring Interceptor with MicroservicesRunner.

How to build the sample
------------------------------------------
From this directory, run

```
mvn clean install
```

How to run the sample
------------------------------------------
You must configure WSO2 Data Analytics Server (DAS) first and run it to recieve the events published by this sample.
Please refer the [analytics documentation](https://github.com/wso2/product-mss/tree/master/analytics) 
for more information on configuring WSO2 DAS.

For "Metrics Interceptor", there are several reporters supported.

**Configuring Reporters for Metrics Interceptor**

This sample uses Console, JMX and WSO2 DAS reporters. 
Configuration options can be provided as environment variables or system properties.

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

```
export HTTP_MONITORING_DAS_TYPE="thrift"
export HTTP_MONITORING_DAS_RECEIVERURL="tcp://localhost:7611"
# export HTTP_MONITORING_DAS_AUTHURL
export HTTP_MONITORING_DAS_USERNAME="admin"
export HTTP_MONITORING_DAS_PASSWORD="admin"
export HTTP_MONITORING_DAS_DATAAGENTCONFIGPATH="data-agent-conf.xml"
```

Here also the `HTTP_MONITORING_DAS_DATAAGENTCONFIGPATH` is a required configuration and others have default values.

**Running the sample**

Note: Console and WSO2 DAS Reporters have a "Polling Period" in seconds. This is the period for 
polling metrics from the metric registry and reporting to Console and WSO2 DAS. The HTTP Monitoring Interceptor sends 
events for each request.


Use following command to run the application

```
java -DMETRICS_REPORTING_DAS_DATAAGENTCONFIGPATH="data-agent-conf.xml" -DHTTP_MONITORING_DAS_DATAAGENTCONFIGPATH="data-agent-conf.xml" -jar target/metrics-1.0.0.jar
```

You can also run as follows.

```
export METRICS_REPORTING_DAS_DATAAGENTCONFIGPATH="data-agent-conf.xml"
export HTTP_MONITORING_DAS_DATAAGENTCONFIGPATH="data-agent-conf.xml"
java -jar target/metrics-1.0.0.jar
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
After running the above cURL commands, you should see metrics output to the console periodically.


Analytics Dashboard in WSO2 Data Analytics Server
------------------------------------------

The HTTP Monitoring events sent by this sample can be seen from the HTTP Monitoring Dashboard in WSO2 DAS.
You can access the dashboard from [http://localhost:9763/monitoring/](http://localhost:9763/monitoring/)
