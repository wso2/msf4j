# Helloworld JPA

This sample demonstrates how to use JPA API and Hibernate with WSO2 MSF4J framework. You can use in-memoery H2 database or MySQL database server with this sample. 

![Image of Sample] (https://raw.githubusercontent.com/sagara-gunathunga/msf4j-intro-webinar-samples/master/etc/jpa.png)

# How to build and run using Maven 

1. Run following command 

```shell
mvn clean package 
```
 
2. Run the application using following java commend 

```shell
java -jar target/jpa.jar
```

# How to test this sample 
Use following cURL commands

Add a sample user 
```shell
curl -v -X POST http://localhost:8080/users/fname/jhon/lname/snow 
```

Get detailss of all the users 

```shell
curl -v http://localhost:8080/users 
```

Get detailss of first user 

```shell
curl -v http://localhost:8080/users/1 
```












