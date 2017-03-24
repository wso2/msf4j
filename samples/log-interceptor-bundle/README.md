# Log Interceptor Bundle Sample

This sample demonstrates how to create an Interceptor as an OSGi bundle.

In this sample, we have exposed an OSGi service that implements the org.wso2.msf4j.Interceptor interface. This 
interceptor logs the headers of all HTTP requests that arrive to the hosted microservices.


## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample

Unzip the WSO2 MSF4J product and copy the bundle that was built in the previous step to the 
"[SERVER-HOME]/osgi/dropins" directory.

Then navigate to the bin directory and run the following command to start the WSO2 MSF4J server.
```
./carbon.sh
```
When the server is being started, LogInterceptor will be registered as an interceptor.


## How to test the sample

Install the [stockquote-msf4j-bundle](../stockquote/bundle) sample to the WSO2 MSF4J server as described in it's README file 
in order to test the interceptor.

Then run the following command to make a request to the installed microservice.
```
curl http://localhost:8080/stockquote/IBM
```

The HTTP headers of the request should be logged in the WSO2 MSF4J console.
