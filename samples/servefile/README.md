# Serve Files with WSO2 MSS

You can serve files from the resource methods by returning a java.io.File or 
by returning a javax.ws.rs.core.Response object with a java.io.File entity.
See the following sample.

```java
    @GET
    public Response serveFile() {
        if (file != null) {
            return Response.ok(file).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
```

How to build the sample
------------------------------------------
From this directory, run

```
mvn clean package
```

How to run the sample
------------------------------------------
Use following command to run the application
```
java -jar target/fileserver-*.jar
```

How to tests the sample
------------------------------------------

Run the following curl command
```
curl -v -X GET http://localhost:8080/file
```

