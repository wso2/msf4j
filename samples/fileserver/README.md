# File handling


## Serve Files with WSO2 MSF4J

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

## Chunked HTTP Request Handling


With WSO2 MSF4J, you can handle chunked requests in two ways.

### 1. Handle requests using HttpStreamHandler

First way is to implement org.wso2.msf4j.HttpStreamHandler as shown in the below example to handle chunked http 
requests in a zero copy manner.

```java
    @POST
    @Path("/stream")
    @Consumes("text/plain")
    public void stream(@Context HttpStreamer httpStreamer) {
        final StringBuffer sb = new StringBuffer();
        httpStreamer.callback(new HttpStreamHandler() {
            @Override
            public void chunk(ByteBuf request, HttpResponder responder) {
                sb.append(request.toString(Charsets.UTF_8));
            }

            @Override
            public void finished(ByteBuf request, HttpResponder responder) {
                sb.append(request.toString(Charsets.UTF_8));
                responder.sendString(HttpResponseStatus.OK, sb.toString());
            }

            @Override
            public void error(Throwable cause) {
                sb.delete(0, sb.length());
            }
        });
    }
```

In the above example when the request chunks arrive, chunk() method is called. When the last chunk is arrived the 
finished() method is called. error() method will be called if an error occurs while processing the request.


### 2. Handle requests by aggregating chunks 
Second way of handling chunked requests is to implement a normal resource method to handle the request ignoring 
whether the requests is chunked as shown in the below example. In this case MSF4J internally 
aggregates all the chunks of the request and presents it as a full http request to the resource method.

```java
    @POST
    @Path("/aggregate")
    @Consumes("text/plain")
    public String aggregate(String content) {
        return content;
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
java -jar target/fileserver-*.jar
```
Note: /var/www/html/upload directory should be available with write permissions


## How to test the sample

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

