##Echo message performance test

In this test, a message of a certain size (1kB) is sent to the microservice and get it back again form the service as 
the response.

###How to Run

Start the service, Then run the following command

```
./run-test.sh "http://localhost:8080/EchoService/echo"
```

Be patient... This will take some time.

Results will be printed in the console and will be saved in "throughput-results.txt".