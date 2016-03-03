# BasicAuth Security Sample

This shows how to implement and register a SecurityInterceptor with MicroservicesRunner.

## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample


Use following command to run the application

```
java -jar target/basicauth-security-*.jar
```

## How to tests the sample

Use following cURL commands.

```
curl --user sam:sam http://localhost:8080/hello/sam -v

curl --user sam:wrong http://localhost:8080/hello/sam -v

```
