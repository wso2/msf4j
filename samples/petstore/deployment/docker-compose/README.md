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


```
./clean.sh
```

Clean.sh will clean all docker compose resources




