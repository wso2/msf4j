# WSO2 Microservices Framework for Java (MSF4J) Samples

This directory contain a comprehensive suite of samples demonstrating the capabilities of WSO2 MSF4J.

# Requirements
* JDK 1.8.*
* Maven 3.0.x

#Samples 
In increasing order of complexity;

* [Hello World](helloworld) - Running a basic Microservice
* [Spring Hello World](spring-helloworld) - Running a basic Microservice using the Spring framework  
* [Session-aware Service](http-session) - Shows how to use HTTP sessions in services 
* [StockQuote (Fat jar)](stockquote/fatjar) - Developing a Microservice as a single jar which packs all the dependencies
* [StockQuote (OSGi bundle)](stockquote/bundle) - Creating a Microservice as an OSGi Bundle
* [Lifecycle](lifecycle) - Using Service Lifecycle Methods
* [Metrics](metrics) - Using Metrics Interceptor
* [HTTP Monitoring](http-monitoring) - Using HTTP Monitoring Interceptor
* [Regex PathParam](regex-pathparam) - How to use regex with PathParam annotation
* [File Server](fileserver) - Handling Files including streaming input & output
* [FormParam](formparam) - Shows usage of the FormParam annotation
* [Circuit Breaker](circuitbreaker) - Using the circuit breaker pattern
* [Interceptor (Fat jar)](interceptor/fatjar-interceptor-service) - Creating an Interceptor as a single jar which packs 
all the dependencies
* [Interceptor (Deployable jar)](interceptor/deployable-jar-interceptor-service) - Creating an Interceptor as a 
deployable jar
* [Interceptor (OSGi bundle)](interceptor/osgi-interceptor-service) - Creating an Interceptor as an OSGi Bundle
* [Hello World JPA](jpa) - Simple JPA Hibernate based sample
* [BasicAuth Security](basicauth-security) - Securing Microservice using BasicAuth
* [OAuth2 Security](oauth2-security) - Securing Microservice using OAuth2
* [Template](template) - Rendering a Model from a Template
* [WSO2 DAS Tracing](message-tracing/das) - Using WSO2 DAS for tracing messages
* [Zipkin Tracing](message-tracing/zipkin) - Using Zipkin for tracing messages
* [Pet-store](petstore) - Running as an MSA Application
* [WebSocket Chat App](websocket/chatApp) - How to use MSF4J WebSocket capabilities to create a simple chat app

