#WSO2 Microservices Server

WSO2 Microservices Server is a lightweight high performance runtime for hosting microservices.

##Getting Started

It is really easy to setup a microservice using WSO2 Microservices Server. You simply need to write a usual JAX-RS 
service and deploy it using a single line of code. Check the following [Hello-Service](https://github.com/samiyuru/product-mss/tree/documentation01/samples/hello-service) sample.

####pom.xml
This pom file inherits from mss-service-parent/pom.xml. It provides a way of setting up things quickly with minimum 
amount of 
configuration. [More info](https://github.com/wso2/product-mss/tree/master/mss-service-parent#wso2-microservices-server
---parent-pom-for-microservices).

```
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    
    <parent>
        <artifactId>mss-service-parent</artifactId>
        <groupId>org.wso2.carbon.mss</groupId>
        <version>1.0.0-SNAPSHOT</version>
        <relativePath>../../mss-service-parent/pom.xml</relativePath>
    </parent>
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.wso2.carbon.mss.sample</groupId>
    <artifactId>Hello-Service</artifactId>

    <properties>
        <microservice.mainClass>org.wso2.carbon.mss.sample.Application</microservice.mainClass>
    </properties>

</project>
```

####HelloService.java
This is the hello service implementation that uses JAX-RS annotations.
```
@Path("/hello")
public class HelloService {

    @GET
    @Path("/{name}")
    public String hello(@PathParam("name") String name) {
        return "Hello " + name;
    }

}
```


####Application.java
This is the oneliner to deploy your service using WSO2 Microservices Server.
```
public class Application {
    public static void main(String[] args) {
        new MicroservicesRunner()
                .deploy(new HelloService())
                .start();
    }
}
```


###Build the Service
Run following Maven command. This will create the uber jar **Hello-Service-1.0.0-SNAPSHOT.jar** in **target** directory.
```
mvn package
```


###Run the Service
You just have to run the following command to get your service up and running.
```
java -jar target/Hello-Service-1.0.0-SNAPSHOT.jar
```


###Test the Service with curl
Run the following command or simply go to [http://localhost:8080/hello/Microservices]
(http://localhost:8080/hello/Microservices) 
from your browser.
```
curl http://localhost:8080/hello/Microservices
```






















