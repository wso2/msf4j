# How to Build the Docker Image

1. Copy target/petstore-pet-1.0.0.jar to the docker/packages directory
2. Download jdk-8u60-linux-x64.gz to  the docker/packages directory
3. From this directory, run:

```
docker build -t wso2mss/petstore-pet .
```

4. From the ssh directory, run
```
docker build -t wso2mss/ssh .
```