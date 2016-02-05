# WSO2 MSF4J - Analytics with WSO2 Data Analytics Server (DAS)

This directory contains the files related to publishing metrics to WSO2 Data Analytics Server (DAS)

Download WSO2 DAS
------------------------------------------
[Download](http://wso2.com/products/data-analytics-server/) WSO2 DAS and unpack it to some directory.
This will be the DAS_HOME directory.

Configure DAS
------------------------------------------
Run "das-setup/setup.sh" to setup DAS. Note that the DAS Home directory in the above step has to 
be provided as an input to that script.

The setup script will also copy the already built MSF4J HTTP Monitoring Carbon App (CAPP) to DAS.

Start DAS
------------------------------------------

From DAS_HOME, run, bin/wso2server.sh to start DAS and make sure that it starts properly.

Run a sample that publishes data to DAS
------------------------------------------
Run the [Metrics and HTTP Monitoring Sample](../samples/metrics-httpmon/metrics-httpmon-fatjar)
included in the distribution. This sample will publish data to DAS.

Accessing the dashboard
------------------------------------------

Go to [http://127.0.0.1:9763/monitoring/](http://127.0.0.1:9763/monitoring/). If everything works fine, you should
see the metrics & information related to your microservices on this dashboard. Please allow a few minutes for the
dashboard to be updated because the dashboard update batch task runs every few minutes.


For Advanced Users
------------------------------------------
If you are an advanced WSO2 DAS user, you can go to the DAS Management Console at
[https://localhost:9443/](https://localhost:9443/) and login with username/password admin/admin.
Once you login, you can view and manually execute the *http_event_script*  in the console to immediately see
the results in the dashboard.

The sources for MSF4J HTTP Monitoring Carbon Application (CAPP) can be found inside "msf4j_http_monitoring_capp_source" 
directory. This CAPP is already built and copied to WSO2 DAS when you Configure DAS as mentioned above.
