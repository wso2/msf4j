# JWT token Sample

This sample shows how to verify signed JWT tokens and use claims in microservices deployed in WSO2 MSF4J.

## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample

Use following command to run the application

```
java -jar jwt-sample/target/jwt-sample-1.0.0-SNAPSHOT.jar
```

## Setting up the Authorization Server with web app.

Here we use WSO2 Identity server as the authorization server.
*This sample is compatible only with the Identity Server versions from 5.2.0*

1) Download and unzip the latest version of the WSO2 Identity Server from http://wso2.com/products/identity-server.

2) Add JWTAccessTokenBuilder/target/JWTAccessTokenBuilder-1.0.0-SNAPSHOT.jar into \<IS_HOME\>/repository/components/lib folder.

3) Add the following property in <IS_HOME>/repository/conf/identity/identity.xml under oauth tag:
"\<IdentityOAuthTokenGenerator\>com.wso2.jwt.token.builder.JWTAccessTokenBuilder\</IdentityOAuthTokenGenerator\>"

4) Using the sso-agent-sample/target/travelocity.com.war, configure the single sign-on web app as indicated in this
document:
[https://docs.wso2.com/display/IS520/Configuring+Single+Sign-On]
(https://docs.wso2.com/display/IS520/Configuring+Single+Sign-On)

5) Configure SAML2 Bearer assertion profile for OAuth 2 with the travelocity web app following the instruction in
this document:
 [https://docs.wso2.com/display/IS520/SAML2+Bearer+Assertion+Profile+for+OAuth+2.0+with+WSO2+Travelocity]
 (https://docs.wso2.com/display/IS520/SAML2+Bearer+Assertion+Profile+for+OAuth+2.0+with+WSO2+Travelocity)

6) Configure OAuth2-OpenID connect for single sign-on following the instructions in this document for the web app:
[https://docs.wso2.com/display/IS520/Configuring+OAuth2-OpenID+Connect+Single-Sign-On]
(https://docs.wso2.com/display/IS520/Configuring+OAuth2-OpenID+Connect+Single-Sign-On)

## How to test the sample

1) Open the travelocity web app from http://localhost:8080/travelocity.com

2) Login using your credentials with SAML SSO.

3) Click on Request OAuth2 Access Token and send to micro service.

You will see the following response in the page.

```
Your OAuth2 Access Token details
Token Type: Bearer
Expiry In: 3600

Micro service details
Micro service URL: http://localhost:8081/hello/
Response from micro service: Hello there admin@carbon.super
```
