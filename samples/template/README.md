# Template Sample

This sample shows how to render a model from a template with WSO2 MSF4J.

First you have to add the following dependency to the pom.

```xml
     <dependency>
        <groupId>org.wso2.msf4j</groupId>
        <artifactId>msf4j-mustache-template</artifactId>
        <version>1.0.0-SNAPSHOT</version>
     </dependency>
```

Following resource method renders the resources/templates/hello.mustache template

```java
    @GET
    @Path("/mustache/{name}")
    public String helloMustache(@PathParam("name") String name) {
        Map map = new HashMap<>();
        map.put("name", name);
        return MustacheTemplateEngine.instance().render("hello.mustache", map);
    }
```

## How to build the sample

From this directory, run the following maven command

```
mvn clean package
```

## How to run the sample

Use following command to run the application

```
java -jar target/template-*.jar
```

## How to tests the sample

After the service is started you can use the following curl command to test the service.

```
curl -X GET http://localhost:8080/template/mustache/world
```
