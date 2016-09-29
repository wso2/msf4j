# Automated deployment of the Petstore sample

## Pre-requisites

 * Docker 
 * Docker compose

### Docker installation for linux
```
wget -qO- https://get.docker.com/ | sh

```

### Docker installation for Mac

https://docs.docker.com/docker-for-mac/

### Docker installation for Windows

https://docs.docker.com/docker-for-windows/

### Docker Compose Installation

https://docs.docker.com/compose/install/


##Run automated deployment 

Run the following commands.

```
./run.sh
```
#### Access Petstore Admin UI

Point your browser to http://localhost:32080

#### Access Petstore Store UI
Point your browser to http://localhost:32081

```
./clean.sh
```

Clean.sh will clean all k8s resources




