# How to build and run the sample

## About this sample

This sample demonstrates how to configure MSF4J transport related properties using application.yml file.
The service will be run on port 10000 instead of default 8080 port or the 6060 which is hard coded. This is because
we have set http].port property in the application.yml file.

```java
@Configuration
public class TransportConfiguration {

    @Bean
    public HTTPTransportConfig http() {
        return new HTTPTransportConfig(6060);
    }

}

```

## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample

From the target directory, run
```
java -jar target/spring-profile*.jar
```

## How to test the sample

We will use the cURL command line tool for testing. You can use your preferred HTTP or REST client too.

```
curl -v http://localhost:10000/hello/wso2
```

You should get a response similar to the following:

```
Hello wso2
```

## How to use Spring profiles

```
dev profile - java -Dspring.profiles.active=dev -jar target/spring-profile*.jar

prod profile - java -Dspring.profiles.active=prod -jar target/spring-profile*.jar
```
Based on the profile that set, you will see the port the application start get change.