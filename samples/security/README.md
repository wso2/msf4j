# Security Sample

This sample shows how to secure microservices deployed on WSO2 MSS.

We show how to implement and register a SecurityInterceptor with MicroservicesRunner. There are 2 sample
Interceptors are available.

1. LoggingInterceptor - Demonstrate how to log HTTP headers from incoming request.

2. UsernamePasswordSecurityInterceptor - Demonstrate very simple HTTP BasicAuth authentication interceptor.


How to build the sample
------------------------------------------
From this directory, run

```
mvn clean install
```

How to build the sample
------------------------------------------
Use following command to run the application
```
java -jar target/security-*.jar
```

How to tests the sample
------------------------------------------

Use following cURL commands.  
```
curl --user john:john http://localhost:8080/hello/john -v

curl --user john:wrong http://localhost:8080/hello/john -v

```
