# Automated deployment of the Petstore sample

Run automated deployment 
```
./run.sh 
```
It will download and configure CoreOS vagrant boxes and setup Kubernetes 1.0.6 in a 3 CoreOS nodes. Complied product-mss, build all docker files, deploy k8s replication controllers, services ..etc.

```
./clean.sh
```

Clean.sh will clean all k8s resources

```
./petstore.sh
```

Deploy k8s resources for petstore sample.

```
./stop.sh
```
 Clean all k8s resources and stop all CoreOS nodes

