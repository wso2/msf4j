#HTTP Streaming (chunking) Sample

This sample demonstrates streaming or HTTP chunked requests in WSO2 MSS.

How to build the sample
------------------------------------------
From this directory, run

```
mvn clean install
```

How to run the sample
------------------------------------------
From the target directory, run

```
java -jar request-streamer-1.0.0.jar
```

Some Fundamentals
------------------------------------------

With WSO2 Microservices server, you can handle chunked requests in two ways.

#Handle requests using HttpStreamHandler

First way is to implement org.wso2.carbon.mss.HttpStreamHandler as shown in the below example to handle chunked http 
requests in a zero copy manner.

```
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
In the above example the when request chunks arrive, chunk() method is called. When the last chunk is arrived the 
finished() method is called. error() method will be called if an error occurs while processing the request.


###Handle requests by aggregating chunks 
Second way of handling chunked requests is to implement a normal resource method to handle the request ignoring the 
whether the requests is chunked as shown in the below example. In this case WSO2 Microservices Server internally 
aggregates all the chunks of the request and presents it as a full http request to the resource method.

```
    @POST
    @Path("/aggregate")
    @Consumes("text/plain")
    public String aggregate(String content) {
        return content;
    }
```