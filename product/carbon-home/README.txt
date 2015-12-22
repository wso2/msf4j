WSO2 Microservices Server ${mss.version}
-----------------------------

Welcome to the WSO2 Microservices Server ${mss.version} release

WSO2 Microservices Server is a lightweight high performance runtime for hosting microservices.
WSO2 MSS is one of the highest performing lightweight microservices frameworks.

Key Features
----------------------------
1. Lightweight and fast runtime
    - 6MB pack size
    - Starts within 400ms
    - Based on the new WSO2 Carbon 5.0 kernel
    - ~25MB memory consumption for the WSO2 MSS framework
2. Simple development, deployment, and monitoring
    - WSO2 Developer Studio-based tooling for generating microservices projects starting from a Swagger API definition
    - Built-in metrics and analytics APIs via WSO2 Data Analytics Server
    - Templating support for dynamic web page generation
    - Tracing of requests using a unique message ID
3. High scalability and reliability
    - Transport based on Netty 4.0
    - JWT-based security
    - Custom interceptors
    - Streaming input and streaming output support
    - Comprehensive samples demonstrating how to develop microservices applications

Installation & Running
----------------------
1. Extract the downloaded zip file.
2. Run the carbon.sh or carbon.bat file in the bin directory.
3. You can enable/disable OSGi Console by un-commenting the osgi.console property in
   CARBON_HOME/conf/osgi/launch.properties file.
4. You can enable OSGi debug logs by un-commenting the osgi.debug property in
   CARBON_HOME/conf/osgi/launch.properties file.

Hardware Requirements
-------------------
1. Minimum memory - 512MB
2. Processor      - Pentium 800MHz or equivalent at minimum

Software Requirements
-------------------
1. Java SE Development Kit - 1.8

All known issues have been recorded at https://wso2.org/jira/browse/WMS

Carbon Binary Distribution Directory Structure
--------------------------------------------

     CARBON_HOME
        |-- bin <directory>
        |   |-- bootstrap <directory>
        |-- osgi <directory>
        |-- conf <directory>
        |-- deployment <directory>
        |-- logs <directory>
        |-- tmp <directory>
        |-- LICENSE.txt <file>
        |-- README.txt <file>
        |-- INSTALL.txt <file>
        |-- release-notes.html <file>

    - bin
      Contains various scripts .sh & .bat scripts.
        - bootstrap
            Contains the basic set of libraries required to bootstrap Carbon.
    - osgi
      Contains all OSGi related libraries and configurations.

    - conf
      Contains server configuration files. Ex: carbon.yml

    - deployment/microservices
	  All deployment artifacts should go into this directory.

    - logs
      Contains all log files created during execution.

    - tmp
      Used for storing temporary files, and is pointed to by the
      java.io.tmpdir System property.

    - LICENSE.txt
      Apache License 2.0 under which WSO2 Microservices Server is distributed.

    - README.txt
      This document.

    - INSTALL.txt
      This document contains information on installing WSO2 Carbon.

    - release-notes.html
      Release information for WSO2 Microservices Server ${mss.version}.

Support
-------

WSO2 Inc. offers a variety of development and production support
programs, ranging from Web-based support up through normal business
hours, to premium 24x7 phone support.

For additional support information please refer to http://wso2.com/support/

For more information on WSO2 Microservices Server,
visit WSO2 Microservices Server Home Page (http://wso2.com/products/microservices-server)


---------------------------------------------------------------------------
(c) Copyright 2015 WSO2 Inc.
