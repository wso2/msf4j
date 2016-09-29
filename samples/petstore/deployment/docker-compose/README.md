# Automated deployment of the Petstore sample

## Pre-requisites

 * Docker 
 * Docker compose

#### Docker installation for linux
```
wget -qO- https://get.docker.com/ | sh

```

#### Docker installation for Mac

https://docs.docker.com/docker-for-mac/

#### Docker installation for Windows

https://docs.docker.com/docker-for-windows/

#### Docker Compose Installation

https://docs.docker.com/compose/install/

##Run automated deployment 

Run the following commands.

```
./run.sh
```
#### Access Petstore Admin UI

http://localhost:32080

#### Access Petstore Store UI
http://localhost:32081

#### Access Analytics UI
http://localhost:39763/monitoring/

#### Clean up
```
./clean.sh
```
Clean.sh will clean all docker compose resources

## How to run in Docker Swarm Cluster

### Setup Docker Swarm Cluster in Amazon AWS

https://beta.docker.com/docs/aws/

### Deploy on Swarm

Change docker-compose image names according to your docker private registry or public registry.

eg. If you have a docker public registry account (say account name is "lakwarus"), you can change images as following

```
docker.wso2.com/petstore-security	-> lakwarus/petstore-security
docker.wso2.com/petstore-transaction	-> lakwarus/petstore-transaction
docker.wso2.com/petstore-pet		-> lakwarus/petstore-pet
docker.wso2.com/petstore-fileserver	-> lakwarus/petstore-fileserver
docker.wso2.com/petstore-admin-fe	-> lakwarus/petstore-admin-fe
docker.wso2.com/petstore-store-fe	-> lakwarus/petstore-store-fe
docker.wso2.com/petstore-das		-> lakwarus/petstore-das
```
```
docker-compose build
```
This will build all docker images

```
docker-compose push
```

This will push newly built images to relevant docker registry

```
docker-compose bundle
```
This will create dockercompose.dab json file

Copy dockercompose.dab file to docker swarm manager node and run following

```
docker deploy dockercompose
```
This will deploy all docker services on swarm cluster

```
docker service update --publish-add 32080:80 dockercompose_admin-fe
docker service update --publish-add 32081:80 dockercompose_store-fe
docker service update --publish-add 39763:9763 dockercompose_das
```

Point your browser to AWS ELB domain with relevent ports to access deployed petstore in swarm cluster




