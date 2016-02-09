Following curl command can be used to consume this service.

```
curl -v -X POST -H "Transfer-Encoding: chunked" -H "Content-Type: text/plain" -d @1kb_rand_data.txt http://localhost:8080/EchoService/echo
```