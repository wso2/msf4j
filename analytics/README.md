# WSO2 Microservices Server - Analytics with WSO2 Data Analytics Server (DAS)

This directory contains the files related to publishing metrics to WSO2 Data Analytics Server (DAS)

Install MySQL RDBMS
------------------------------------------
Download and install MySQL. This will be used as the data store for DAS as well as the analytics dashboards.

Download WSO2 DAS
------------------------------------------
[Download](http://wso2.com/products/data-analytics-server/) WSO2 DAS and unpack it to some directory.
This will be the DAS Home directory.

Configure DAS
------------------------------------------
Run "setup-das.sh" to script copy required files to DAS as well as setup all the schemas in MySQL. Note that the
DAS Home directory in the above step has to be provided as an input to that script. You will also need to
provide your MySQL username & password to setup the database schemas.

The setup script will also copy the already built MSS HTTP Monitoring Carbon App (CAPP) to DAS.

Run a sample that publishes data to DAS
------------------------------------------
Run the [metrics sample](https://github.com/wso2/product-mss/tree/master/samples/metrics)
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
