# Hello gRPC Service Sample

gRPC is a framework which enable client application to directly call methods on a server application on a different 
machine as if it was a local object. gRPC is based around the idea of defining a service, specifying the methods that
 can be called remotely with their parameters and return types.

This sample shows how to create micro service which can expose as gRPC service and how to create mapping proto file to 
the microservice. So when creating gRPC service and proto file, we need to make sure same service name and method 
name used in both places.  

## Writing the pom.xml 

Your POM can inherit from [msf4j-service](../../../poms/msf4j-service). 
See details [here](../../../poms/msf4j-service).

## Writing service implementation

Writing gRPC service is similar to the normal microservice. Following are the additional things we need to consider, 

* According gRPC framework, service method can have one input parameter. If we need to pass multiple values, we can 
define a new message type with all the parameters we need to pass. Similarly, when we create microservice we need to
define one request type.
* Addition to the method implementation, we need to add method to return ServiceProto.class like below
 ```java
     private Class getProtoService() {
         return HelloServiceProto.class;
     }
 ```

## Writing proto file

Writing proto file needs to be accordance with the specification in [grpc.io](https://grpc.io/docs/tutorials/basic/java.html). Follwing are the additional things we need to consider,

* Service Name should be the same as Service Class name.
* Method Names should be the same as Service method name defined in the class.
* Field Name in request and return types should be the same as field names defined in the class.

proto file need to copy to the src/main/proto directory.

## Writing Service Application
Set isGrpcService flag to true when creating MicroserviceRunner as below,
````java
        new MicroservicesRunner(9090, true)
                .deploy(new HelloService())
                .start();
````

## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample

From the target directory, run
```
java -jar helloworld-service-*.jar
```

## How to test the sample

We need to create client using the .proto file defined earlier and call the service using service stub. See how to 
create java client [here](../helloworld-client/README.md)  


You should get a successful response if everything worked fine.

