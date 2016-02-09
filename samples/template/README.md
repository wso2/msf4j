# Template Sample

This sample shows how to render a model from a template with WSO2 MSF4J.

First you have to add the following dependency to the pom.

```xml
     <dependency>
        <groupId>org.wso2.msf4j</groupId>
        <artifactId>msf4j-mustache-template</artifactId>
        <version>1.0.0</version>
     </dependency>
```

Following resource method renders the [resources/templates/hello.mustache] template

```java
    @GET
    @Path("/{name}")
    public Response helloMustache(@PathParam("name") String name) {
        Map map = Collections.singletonMap("name", name);
        String html = MustacheTemplateEngine.instance().render("hello.mustache", map);
        return Response.ok()
                .type(MediaType.TEXT_HTML)
                .entity(html)
                .build();
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

After the service is started navigate to [http://localhost:8080/MSF4J](http://localhost:8080/MSF4J) using the browser.
