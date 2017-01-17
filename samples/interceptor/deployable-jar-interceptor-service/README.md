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

First you need to install the MSF4J feature in to carbon kernel. To proceed with this task you need to
checkout the carbon kernel source code and install the below mentioned features (in distribution pom.xml). Please note that the
corresponding version of the feature may change along with the MSF4J version.

* \<groupId>org.wso2.carbon.metrics\</groupId>\<artifactId>org.wso2.carbon.metrics.core.feature\</artifactId>
* \<groupId>org.wso2.carbon.metrics\</groupId>\<artifactId>org.wso2.carbon.metrics.jdbc.core.feature\</artifactId>
* \<groupId>org.wso2.carbon.metrics\</groupId>\<artifactId>org.wso2.carbon.metrics.das.core.feature\</artifactId>
* \<groupId>org.wso2.carbon.messaging\</groupId>\<artifactId>org.wso2.carbon.messaging.feature\</artifactId>
* \<groupId>org.wso2.carbon.jndi\</groupId>\<artifactId>org.wso2.carbon.jndi.feature\</artifactId>
* \<groupId>org.wso2.carbon.datasources\</groupId>\<artifactId>org.wso2.carbon.datasource.core.feature\</artifactId>
* \<groupId>org.wso2.carbon.transport\</groupId>\<artifactId>org.wso2.carbon.transport.http.netty.feature\</artifactId>
* \<groupId>org.wso2.msf4j\</groupId>\<artifactId>org.wso2.msf4j.feature\</artifactId>

One the features are added execute the following command to build the carbon kernel.
```
mvn clean install
```
This will install the msf4j and other required features to the kernel and build up a product.
You can find the product in the resources/target directory

Extract \<CARBON_KERNEL SOURCE CODE HOME>/distribution/target/wso2carbon-kernel-\<version>.zip to a location of your choice.

Run the following command to start the carbon kernel with MSF4J feature from /bin directory of the extracted zip.
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