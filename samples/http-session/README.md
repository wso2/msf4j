# Session-aware Service Sample

This sample demonstrates how to use HTTP sessions in your microservice.

## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample

From the target directory, run

```
java -jar session-*.jar
```

## How to test the sample

Point your Web browser to [http://localhost:8080](http://localhost:8080) & keep refreshing the page.

You will see the count being printed on the browser window, and everytime you refresh, the count will be incremented 
and once the count reaches 100, it will be reset because the session is invalidated.

You can also use cURL or any other REST client to invoke your service.

```
curl -v http://localhost:8080
```

You should get a response similar to the following:

```
> 
< HTTP/1.1 200 OK
< Connection: keep-alive
< Set-Cookie: JSESSIONID=3508015E4EF0ECA8C4B761FCC4BC1718
< Content-Length: 1
< Content-Type: */*
< 
* Connection #0 to host localhost left intact
1
```

Next we will send the JSESSIONID cookie with the curl requests so that the same session can be referred to on the service
end.

```
curl --header "Cookie:  JSESSIONID=3508015E4EF0ECA8C4B761FCC4BC1718" http://localhost:8080
```

Keep repeating the above command and you should see that the counter being incremented upto 100, and then being reset.
Once the counter gets reset, you will need to retrieve the new JSESSIONID and repeat the requests.