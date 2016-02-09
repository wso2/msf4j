##Echo message performance test

In this performance test, a sample echo service that can accept a payload and respond the same payload back to the 
client was created with each subjected microservices framework. See [echo-samples](echo-samples) directory for the 
sample services that were created for each framework.

## Throughput test

To measure the throughput, each of the above mentioned sample services were started and 1KB of payload was sent to 
each service repeatedly in varying concurrency levels using apache bench tool. After that the average throughput for
each concurrency level is calculated and plotted for each framework.

![Throughput](graphs/tps.png)

All services were run out of the box without any tuning  separately on a 8 core 64GB server.

### Performing the throughput test

For each service in [echo-samples](echo-samples) directory,
* Build service
* Start the service and get the service url (Refer the readme of each service)
* Perform the test using the automated [run-test.sh](run-test.sh) script

```
./run-test.sh <service-url>
```
This script will perform the loads and provide you the average throughput of the given service.


## Memory Test

To measure the memory usage, above mentioned 1KB echo test was performed for each sample service. Then for each 
concurrency level the heap usage was measured and an average value for each concurrency level was calculated. These
average heape usage values were plotted for each framework.

![Memory](graphs/memory.png)

### Performing the memory test

For each service in [echo-samples](echo-samples) directory,
* Build service
* Start the service with GC logging enabled with time stamp (-Xloggc:gc-log-file.log -verbose:gc -XX:+PrintGCDateStamps)
* Perform the test using the automated [run-test.sh](run-test.sh) script as mentioned earlier
* Get time range of each concurrency level from the output of the run-test.sh
* Analyse the GC log for each concurrency level by matching the time range and calculate the average heap usage for each concurrency level
