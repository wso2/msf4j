# Security-interceptor Sample

This sample demonstrate how to implement and register Interceptor with MicroservicesRunner. There are 2 sample
Interceptors are available.

1. LoggingInterceptor - Demonstrate how to log HTTP headers from incoming request.

2. UsernamePasswordSecurityInterceptor - Demonstrate very simple HTTP BasicAuth authentication interceptor.


How to run the sample  
------------------------------------------
1. Use maven to build the sample 
```
mvn clean package 
```
2. Use following command to run the application 
```
java -jar target/security-interceptor-1.0.0-SNAPSHOT.jar
```
How to test the sample  
------------------------------------------

Use following cURL commands.  
```
curl --user john:john http://localhost:8080/hello/john -v

curl --user john:wrong http://localhost:8080/hello/john -v

```
