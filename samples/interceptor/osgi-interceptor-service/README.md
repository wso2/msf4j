# Request and response interceptors MSF4J OSGi Bundle Sample

This sample demonstrates how to create MSF4J interceptors as an OSGi bundle.

* See also; [MSF4J Interceptor Service - Fat Jar mode](../fatjar-interceptor-service)
* See also; [MSF4J Interceptor Service - Deployable Jar mode](../deployable-jar-interceptor-service)

In this sample we have exposed the InterceptorService as an OSGi service that implements 
org.wso2.msf4j.Microservice interface as shown in the following code.

```java
@Component(
        name = "InterceptorService",
        service = Microservice.class,
        immediate = true
)
@Path("/interceptor-service")
public class InterceptorService implements Microservice {
    

    private static final Logger log = LoggerFactory.getLogger(InterceptorService.class);
    

    /**
     * Method for getting the micro-service name.
     *
     * @return name of the micro-service.
     */
    @GET
    @Path("/service-name")
    @RequestInterceptor(HTTPRequestLogger.class)
    @ResponseInterceptor(HTTPResponseLogger.class)
    public String getServiceName() {
        log.info("HTTP Method Execution - getServiceName()");
        return "WSO2 Service";
    }
}
```

Further more we have to define the request and response interceptor order. In order to proceed with include the 
following dependency in the pom. This dependency will contain the interceptors to be used.

```xml
        <dependency>
            <groupId>org.wso2.msf4j.samples</groupId>
            <artifactId>interceptor-common</artifactId>
            <version>${project.version}</version>
        </dependency>
```

###Defining the order of request interceptors

Please do refer to the [SampleRequestInterceptorConfig](./src/main/java/org/wso2/msf4j/samples/osgiinterceptorservice/config/SampleRequestInterceptorConfig.java)
class.

In here you inherit from the [OSGiRequestInterceptorConfig](../../../core/src/main/java/org/wso2/msf4j/interceptor/OSGiRequestInterceptorConfig.java)
class. All you need to do is to add a list of non-global request interceptors (optional) and global request interceptors
(option) using "addRequestInterceptors" and "addGlobalRequestInterceptors" respectively. The order in which you define
 the global interceptors are defined is the order in which they are executed.
 
Please do make sure to add the `@component` annotation as stated below.

```java
@Component(
        name = "SampleRequestInterceptorConfig",
        service = OSGiRequestInterceptorConfig.class,
        immediate = true
)
public class SampleRequestInterceptorConfig extends OSGiRequestInterceptorConfig {
    @Override
    public void createRequestInterceptors() {
        addRequestInterceptors(new HTTPRequestLogger());
    }
}
```
 
###Defining the order of response interceptors

Please do refer to the [SampleResponseInterceptorConfig](./src/main/java/org/wso2/msf4j/samples/osgiinterceptorservice/config/SampleResponseInterceptorConfig.java)
class.

In here you inherit from the [OSGiResponseInterceptorConfig](../../../core/src/main/java/org/wso2/msf4j/interceptor/OSGiResponseInterceptorConfig.java)
class. All you need to do is to add a list of non-global response interceptors (optional) and global response interceptors
(option) using "addResponseInterceptors" and "addGlobalResponseInterceptors" respectively. The order in which you define
 the global interceptors are defined is the order in which they are executed.
 
 Please do make sure to add the `@component` annotation as stated below.
 
 ```java
@Component(
        name = "SampleResponseInterceptorConfig",
        service = OSGiResponseInterceptorConfig.class,
        immediate = true
)
public class SampleResponseInterceptorConfig extends OSGiResponseInterceptorConfig {

    @Override
    public void createResponseInterceptors() {
        addResponseInterceptors(new HTTPResponseLogger());
    }
}
 ```
 
## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample

### <a name="osgiconsole"></a>Using OSGi Console

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

Install the target/osgi-interceptor-service-\<version>.jar and ../../interceptor-common/target/
interceptor-common-\<version>.jar as an OSGi bundle to 
carbon kernel using it's OSGi console with the following command.

```
install file://<path to target directory>/osgi-interceptor-service-\<version>.jar
install file://<path to target directory>/interceptor-common-\<version>.jar
```

When the installation of the bundle is successful, use the bundle ID of the installed bundle to start  
it. Use the following command in the OSGi console for that.

```
start <bundle ID>
```

When the bundle is started, the microservice that is exposed as an OSGi service will be picked by the runtime and 
will be exposed as a REST service.

### Using dropins directory
Install MSF4J feature to the carbon kernel as described in section [Using OSGi Console](#osgiconsole)

Copy the target/osgi-interceptor-service-\<version>.jar and ../../interceptor-common/target/
interceptor-common-\<version>.jar to [SERVER-HOME]/osgi/dropins" directory.

Then navigate to the bin directory and run the following command to start WSO2 carbon kernel.
```
./carbon.sh
```
When the server is being started, the bundle in the dropins directory will be automatically 
loaded and it's microservices that are there as OSGi services will be exposed as REST services.


## How to test the sample

Use following cURL commands.
```
curl http://localhost:9090/interceptor-service/service-name
```