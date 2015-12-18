# Log Interceptor Bundle Sample

This sample demonstrates how to create an Interceptor as an OSGi bundle.

In this sample we have exposed an OSGi service that implements org.wso2.carbon.mss.Interceptor interface. This 
interceptor logs the headers of all http requests that arrives to the hosted microservices.


## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample

Unzip wso2 MSS product and copy the bundle that was built in previous step to the 
"[SERVER-HOME]/osgi/dropins" directory.

Then navigate to the bin directory and run the following command to start WSO2 MSS server.
```
./carbon.sh
```
When the server is being started, LogInterceptor will be registered as an interceptor.


## How to test the sample

Install the [stockquote-mss-bundle](../stockquote-mss-bundle) sample to WSO2 MSS server as described in it's readme 
in order to test the interceptor.

Then run the following command to make a request to the installed microservice.
```
curl http://localhost:8080/stockquote/IBM
```

The HTTP headers of the request should be logged in WSO2 MSS console.
