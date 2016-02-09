# WSO2 MSF4J - Parent POM for Microservices

This parent POM file makes life easy for developers who write microservices using WSO2 MSF4J. The following example
from the [stockquote-fatjar](../samples/stockquote/fatjar) example demonstrates how to quickly write a POM for your 
microservice using the MSF4J Service Parent POM. Please see [archtypes](../../archetypes) for easily creating an 
MSF4J project using archtypes.

```xml
<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/POM/4.0.0"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.wso2.msf4j</groupId>
        <artifactId>msf4j-service</artifactId>
        <version>1.0.0</version>
        <relativePath>../../msf4j-service/pom.xml</relativePath>
    </parent>

    <groupId>org.wso2.msf4j.example</groupId>
    <artifactId>stockquote-fatjar</artifactId>
    <packaging>jar</packaging>

    <name>StockQuote Service</name>

    <properties>
        <microservice.mainClass>org.wso2.msf4j.example.StockQuoteService</microservice.mainClass>
    </properties>
</project>
```

The microservice.mainClass Maven property should be used to define your main class. i.e. the class that includes
the main method.



