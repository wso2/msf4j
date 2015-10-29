# Analytics Related Files

This directory contains the files related to DAS


Configure DAS
------------------------------------------
Run "setup-das.sh" to script copy required files to DAS. 

Note: This will also copy the already built MSS HTTP Monitoring CAPP to DAS.


MSS HTTP Monitoring CAPP Source
------------------------------------------
1. Use ant to build the CAR file
```
ant
```
2. Upload the CAR file to DAS as a Carbon Application. CAR file is available at:
```
target/mss_http_monitoring_capp.car
```
