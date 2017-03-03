# Request and response interceptors MSF4J deployable jar Sample

This sample demonstrates how to create deployable interceptors for hot deployment.

* See also; [MSF4J Interceptor Service - Fat Jar mode](../fatjar-interceptor-service)
* See also; [MSF4J Interceptor Service - OSGi mode](../osgi-interceptor-service)

```java
@Path("/interceptor-service")
public class InterceptorService implements Microservice {

    // resource methods are here..

}
```

You have to add full classpath of the microservice class in <microservice.resourceClasses> under properties in the pom
.xml of deployable jar as shown in the following code.

```xml
    <properties>
        <microservice.resourceClasses>org.wso2.msf4j.samples.deployablejarinterceptorservice.InterceptorService</microservice.resourceClasses>
    </properties>
```

## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample

First you need to install the MSF4J feature in to carbon kernel. To proceed with this navigate to [MSF4J Kernel pom](/resources)
and execute the command below:
```
mvn clean install
```
This will install the msf4j and other required features to the kernel and build up a product.
You can find the product in the resources/target directory

Go to the target/wso2msf4j-<version>/bin directory
Then run the following command to start the MSF4J server.
```
./carbon.sh
```

Finally copy the target/deployable-jar-interceptor-service-\<version>.jar to deployment/microservices directory of MSF4J server.
This will trigger the jar to be automatically deployed during the server runtime.

## How to test the sample

Use following cURL commands.
```
curl http://localhost:9090/interceptor-service/service-name
```