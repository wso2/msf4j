# Lifecycle Sample

This sample shows how to use service lifecycle methods on WSO2 MSF4J.

You can notice following 2 lifecycle methods in Helloworld service class.

```java
    @PostConstruct
    public void init() {
        LOG.info("Helloworld :: calling PostConstruct method");
    }

    @PreDestroy
    public void close() {
        LOG.info("Helloworld :: calling PreDestroy method");
    }

```

## How to build the sample

From this directory, run

```
mvn clean package
```

## How to run the sample

Use following command to run the application
```
java -jar target/lifecycle-*.jar
```

## How to tests the sample


During the service startup you should be able to see the following output.
```
2015-11-12 17:31:28 INFO  Helloworld:40 - Helloworld :: calling PostConstruct method


```

Shutdown the server using CTRL+C, now you should be able to see the following output.

```
2015-11-12 17:31:38 INFO  Helloworld:45 - Helloworld :: calling PreDestroy method

```
