# Spring Helloworld Sample

This is the MSF4J Hello World sample based on Spring that reponds to you saying hello.

Following example illustrates how to use Spring annotations together with MSF4J annotations to build a RESTful service. 

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Component
@Path("/greeting")
public class Hello {

    @Autowired
    private HelloService helloService;

    @GET
    public String message() {
        return helloService.hello(" World");
    }
}

``` 

Here an instance of HelloService class will be injected to the Hello service class by Spring framework,  to enable 
Spring to discover  HelloService bean it is possible to use @Component annotation within HelloService class as shown 
below.  

```java
@Component
public class HelloService {

   public String hello(String name) throws InvalidNameException {
       if (isNumericValue(name)) {
           throw new InvalidNameException(name + " is an invalid name");
       }
       return "Hello " + name;
   }

 }
 
``` 

In addition to above service classes a special class called MSF4JSpringApplication is used to run Spring based MSF4J 
services.  When specific Spring Configuration is not present, MSF4J use Spring’s auto-scan feature to discover service 
beans, any class  with @Path and Spring’s @Component  annotations will be deploy as services. 

```java
import org.wso2.msf4j.spring.MSF4JSpringApplication;

public class Application {

    public static void main(String[] args) {
        MSF4JSpringApplication.run(Application.class, args);
    }
}
```

### Deploying interceptors using Spring.  

It is possible to configure and deploy MSF4J Interceptors through Spring. Adding @Component  annotation to any 
implementation class of MSF4J Interceptor enables Spring to auto-discover and deploy above class as 
an MSF4J Interceptor.  

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Component
public class LogHeadersInterceptor implements Interceptor {

    @Override
    public boolean preCall(HttpRequest httpRequest, HttpResponder httpResponder, 
                           ServiceMethodInfo serviceMethodInfo) {
        // preCall implementation code goes here 
    }

    @Override
    public void postCall(HttpRequest httpRequest, HttpResponseStatus httpResponseStatus,
                         ServiceMethodInfo serviceMethodInfo) {
        // postCall implementation code goes here 
    }
}
```

### Configuring ExceptionMappers using Spring. 

Adding  @Component  annotations into ExceptionMapper classes enable Spring framework to discover and register 
ExceptionMappers with MSF4J.

```java
@Component
public class EntityNotFoundMapper implements ExceptionMapper<EntityNotFoundException> {
    @Override
    public Response toResponse(EntityNotFoundException ex) {
        return Response.status(404).
                entity(ex.getMessage() + " [from EntityNotFoundMapper]").
                type("text/plain").
                build();
    }
}
```

### Configuring MSF4J through Spring
It’s possible to provide alternative or additional Spring configuration by annotating configuration classes 
using @Configuration annotation. For more details refer following examples. 


### 1. Changing HTTP port 

```java
@Configuration
public class TransportConfiguration {

    @Bean
    public HTTPTransportConfig http(){
         return new HTTPTransportConfig(6060);
     }

}
```
In above example it overrides default HTTPTransportConfig bean with user configured  HTTPTransportConfig bean mentioned in TransportConfiguration class so that service will run on HTTP port 9090 instead of default 8080 port. Here adding @Configuration annotation into above class enables Spring to identify underlying class as a Spring configuration.  Also note that, here default HTTPTransportConfig bean name is “http”, it’s required to use same bean name as default HTTPTransportConfig bean name in order to override default HTTP configurations.    

Alternatively it’s possible to use one of the following approach to change the HTTP port through configuration. 

1. Provide port number as a command line argument
    * Example:   --http.port=9090

2. Provide port number as a system variable 
    *  Example:  -Dhttp.port=9090

3. Provide port number through application.properties file 
    *  Example: create a application.properties in the classpath and  include http.port=9090 property. 
 
Following are the list of configuration options supported by HTTPS transport.  
 
Option | Description 
--- | --- |
enabled | Enable or disable the transport 
port  | HTTP port 
host | Host to be bind with HTTP transport 



### 2. Configuring HTTPS transport  

By default HTTPSTransportConfig is disabled, it is possible to configure HTTPSTransportConfig bean as below 
by providing required key store details. 

```java
@Configuration
public class TransportConfiguration {

    @Bean
    public HTTPSTransportConfig https() {
        return new HTTPSTransportConfig().port(7070).keyStore("wso2carbon")
                .keyStorePass("wso2carbon").certPass("wso2carbon").enabled();

    }
}
```

Following are the list of configuration options supported by HTTPS transport.  

Option | Description 
--- | --- |
enabled | Enable or disable the transport 
port  | HTTP port 
host | Host to be bind with HTTP transport 
keyStoreFile | Key store file to be used with HTTPS transport 
keyStorePass | Key store password 
certPass | Certificate password 


# How to build and run the sample

## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample

From the target directory, run
```
java -jar spring-helloworld-*.jar
```

## How to test the sample

We will use the cURL command line tool for testing. You can use your preferred HTTP or REST client too.

```
curl http://localhost:9090/hello/wso2
```

You should get a response similar to the following:

```
Hello wso2
```
