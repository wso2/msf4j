Following curl command can be used to consume this service after this is deployed in wildfly server.

```
curl -v -X POST -H "Transfer-Encoding: chunked" -H "Content-Type: text/plain" -d @1kb_rand_data.txt http://localhost:8080/wildfly-echo-message/app/EchoService/echo
```