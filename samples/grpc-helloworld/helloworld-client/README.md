# Hello gRPC Client Sample

gRPC is a framework which enable client application to directly call methods on a server application on a different 
machine as if it was a local object. gRPC is based around the idea of defining a service, specifying the methods that
 can be called remotely with their parameters and return types.

This sample shows how to create service client using server definition file ([.proto file](../helloworld-service/src/main/proto/helloworld.proto)).

## Writing the pom.xml 

Your POM can inherit from [msf4j-service](../../../poms/msf4j-service). 
See details [here](../../../poms/msf4j-service).

## Writing service client

We need to follow the basic steps to generate service stubs using proto file.

1. First we need to copy proto file to src/main/proto/ directory of the project
2. Run mvn build command and generate service stubs.

```
mvn package
```

3. Using the service stubs, write the client code to invoke service methods


## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample

From the target directory, run
```
java -jar helloworld-client-*.jar
```
Note that you need to start the [helloworld-service](../helloworld-service) before you run this sample.

You should get a successful response if everything worked fine.

