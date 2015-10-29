# Analytics Related Files

This directory contains the files related to DAS

Configure Datasource
------------------------------------------
1. Copy `httpmon-datasources.xml` to $DAS_HOME/repository/conf/datasources/


Create MySQL Database for HTTP Monitoring
------------------------------------------
1. Create database
```
create database mss_httpmon;
```
2. Use `http-mon-mysql.sql` to create tables


Deploy MSS HTTP Monitoring CAPP
------------------------------------------
1. Use ant to build the CAR file
```
ant
```
2. Upload the CAR file to DAS as a Carbon Application. CAR file is available at:
```
target/mss_http_monitoring_capp.car
```
