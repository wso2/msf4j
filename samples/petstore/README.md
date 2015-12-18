# Pet Store Sample

This sample modeled after the popular JavaEE Pet Store sample, show cases how to use WSO2 Microservices Server as
well as other technologies such as Vagrant & Kubernetes to build a Microservices Architecture solution.

The sample contains 3 Microservices Server samples;

1. Pet - This microservice is used to represent the pet resource
2. Transaction -  This microservice is used to represent the transaction resource which is used for handling transactions
in the pet store
3. Security -  This microservice contains an embedded LDAP server & handles authentication as well as issuing JWT tokens
4. Frontend admin - a PHP front end for administering the pet store
5. Fronend user - a PHP store front end for the pet store
6. File server - a service which handles file storing & retrieving
7. Redis - the database used by the pet store

# Deploying the Pet Store Microservice Solution
Go to the deployment directory & follow the instructions in that directory to deploy the complete pet store. This
deployment is dependent on Vagrant, Docker & Kubernetes (k8s).