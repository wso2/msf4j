# Security-hook Sample

This sample demonstrate how to implement and register HandlerHook with MicroservicesRunner. There are 2 sample
HandlerHook are available.

1. LoggingHeadersHook - Demonstrate how to log HTTP headers from incoming request.

2. UsernamePasswordSecurityHook - Demonstrate very simple HTTP BasicAuth authentication hook.


How to run the sample  
------------------------------------------
1. Use maven to build the sample 
```
mvn clean package 
```
2. Use following command to run the application 
```
java -jar target/security-hook-1.0.0-SNAPSHOT.jar
```
How to test the sample  
------------------------------------------

Use following cURL commands.  
```
curl --user john:john http://<IP>:8080/hello/john -v

curl --user john:wrong http://<IP>:8080/hello/john -v

```
