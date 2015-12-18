# Hello Service Sample

This is the WSO2 Microservices Server Hello World sample

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
java -jar helloworld-*.jar
```

How to test the sample
------------------------------------------
We will use the cURL command line tool for testing. You could use any appropriate HTTP or REST client too.

```
curl -v http://localhost:8080/hello/wso2
```

You should get a response similar to the following:

```
* Adding handle: conn: 0x7fc9d3803a00
* Adding handle: send: 0
* Adding handle: recv: 0
* Curl_addHandleToPipeline: length: 1
* - Conn 0 (0x7fc9d3803a00) send_pipe: 1, recv_pipe: 0
* About to connect() to localhost port 8080 (#0)
*   Trying ::1...
* Connected to localhost (::1) port 8080 (#0)
> GET /hello/wso2 HTTP/1.1
> User-Agent: curl/7.30.0
> Host: localhost:8080
> Accept: */*
>
< HTTP/1.1 200 OK
< Content-Type: */*
< Content-Length: 10
< Connection: keep-alive
<
* Connection #0 to host localhost left intact
Hello wso2
```
