# Hello Service Sample

This is the MSF4J Hello World sample that reponds to you saying hello.

## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample

From the target directory, run
```
java -jar helloworld-*.jar
```

## How to test the sample

We will use the cURL command line tool for testing. You can use your preferred HTTP or REST client too.

```
curl http://localhost:8080/hello/wso2
```

You should get a response similar to the following:

```
Hello wso2
```
