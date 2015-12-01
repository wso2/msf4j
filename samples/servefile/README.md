# Serve Files with WSO2 MSS

You can serve files from the resource methods by returning a java.io.File or 
by returning a javax.ws.rs.core.Response object with a java.io.File entity.
See the following sample.

```java
    @GET
    @Path("/{fileName}")
    public Response getFile(@PathParam("fileName") String fileName) {
        File file = Paths.get(MOUNT_PATH, fileName).toFile();
        if (file.exists()) {
            return Response.ok(file).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
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
Note: /var/www/html/upload directory should be available with write permissions


How to test the sample
------------------------------------------

Run the following curl command to upload file
```
curl -v -X POST --data-binary @/testPng.png http://localhost:8080/filename.png
```
Here /testPng.png will be uploaded with the name filename.png

---

Run the following curl command to receive file
```
curl -v -X GET http://localhost:8080/filename.png
```

