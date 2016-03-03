# OAuth2 Security Sample

This sample shows how to secure microservices deployed in WSO2 MSF4J using OAuth2.

## Setting up the Authorization Server.

Here we use WSO2 Identity server as the authorization server.

1) Download and unzip the latest version of the WSO2 Identity Server from http://wso2.com/products/identity-server.

2) Copy resources/introspect.war to wso2is-5.1.0/repository/deployment/server/webapps directory.

3) Create a Service Provider by following the instructions in the this document
 [https://docs.wso2.com/display/IS510/Configuring+a+Service+Provider]
 (https://docs.wso2.com/display/IS510/Configuring+a+Service+Provider)

4) Then under the "configure inbound authentication" section, create an OAuth2 application which represents your
client application. Instructions are available in the above documentation link. 

For "Callback Url", provide "https://localhost:9443/oauth2/token"

Once the OAuth2 application is created,
you will get a pair of keys called OAuth Client Key and OAuth Client Secret.

5) Execute following command from the installation directory to start the server.  e.g. /home/user/wso2is-5.1.0/

 ```
 sh bin/wso2server.sh
 ```

6) Execute the following command using OAuth Client Key and OAuth Client Secret.
This command should return with a JSON response which contains the access token.

 ```
 curl -v -k -X POST --basic -u <OAuth Client Key>:<OAuth Client Secret> -H "Content-Type: application/x-www-form-urlencoded;charset=UTF-8" -d "grant_type=client_credentials" https://localhost:9443/oauth2/token
 ```

 ```
 {"access_token":"ded91567bbc7573d5c47e77e700f62ac","token_type":"Bearer","expires_in":3600}
 ```

Note the access_token value.

## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample

When you start the MSF4J server, you need to pass a system property with the endpoint of the authorization server.

e.g. AUTH_SERVER_URL=http://localhost:9763/introspect

If the system property is not specified, then a default value of "http://localhost:9763/introspect" will be set in this
 sample

Use following command to run the application

```
java -DAUTH_SERVER_URL=http://localhost:9763/introspect -jar target/security-*.jar
```

## How to tests the sample

Use following cURL commands.

```
curl -v -H "Authorization: Bearer <access_token>" http://localhost:8080/hello/john

> GET /hello/john HTTP/1.1
> Host: localhost:8080
> User-Agent: curl/7.43.0
> Accept: */*
> Authorization: Bearer <access_token>
>
< HTTP/1.1 200 OK
< Content-Type: */*
< Content-Length: 13
< Connection: keep-alive

Hello john

```
