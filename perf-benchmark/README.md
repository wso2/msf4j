##Echo message performance test

In this test an echo service was written in each microservices framework. Then, requests with 1kB payload were sent to each service in varying concurrency levels. Then, the average throughput was measured for each concurrency level for each microservices framework.

###How to Run

Start the service, Then run the following command

```
./run-test.sh "http://localhost:8080/EchoService/echo"
```

Be patient... This will take some time.

Results will be printed in the console and will be saved in "throughput-results.txt".
