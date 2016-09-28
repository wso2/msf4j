# Automated deployment of the Petstore sample

## Pre-requisites

 * Kubernetes cluster with SKY DNS support
 * Can use one of following links to setup kubernetes cluster
  - https://github.com/imesh/kubernetes-vagrant-setup
  - https://github.com/pires/kubernetes-vagrant-coreos-cluster

##Run automated deployment 

Copy WSO2 Data Analytics Server pack (zip) into packs folder

Run the following commands.

```
./petstore.sh
```

Deploy k8s resources for petstore sample.

#### List all k8s nodes
```
$ kubectl get nodes
NAME           LABELS                                             STATUS    AGE
172.17.8.102   disktype=ssd,kubernetes.io/hostname=172.17.8.102   Ready     6d
172.17.8.103   kubernetes.io/hostname=172.17.8.103                Ready     6d
```
#### List all k8s pods
```
$ kubectl get pods
NAME                   READY     STATUS    RESTARTS   AGE
admin-fe-povyi         1/1       Running   0          39s
fileserver-mrxhj       1/1       Running   0          39s
pet-jdd2u              1/1       Running   0          40s
redis-0kfqa            1/1       Running   0          1m
redis-lgem8            1/1       Running   0          2m
redis-master-sstyy     1/1       Running   0          39s
redis-sentinel-kvv69   1/1       Running   0          1m
redis-sentinel-m7t08   1/1       Running   0          1m
redis-sentinel-ur0za   1/1       Running   0          39s
redis-y5o9d            1/1       Running   0          2m
security-rslsf         1/1       Running   0          39s
store-fe-8xyzh         1/1       Running   0          39s
txn-5csjp              1/1       Running   0          39s
```
#### List all k8s services
```
$ kubectl get svc
NAME              CLUSTER_IP       EXTERNAL_IP   PORT(S)     SELECTOR              AGE
admin-fe          10.100.2.62      nodes         80/TCP      name=admin-fe         1m
fileserver        10.100.173.68    nodes         80/TCP      name=fileserver       1m
identity-server   10.100.123.101   <none>        9443/TCP    <none>                3m
kubernetes        10.100.0.1       <none>        443/TCP     <none>                5d
pet               10.100.59.232    nodes         80/TCP      name=pet              1m
redis-master      10.100.70.115    <none>        6379/TCP    name=redis-master     1m
redis-sentinel    10.100.90.239    nodes         26379/TCP   redis-sentinel=true   3m
security          10.100.151.122   nodes         80/TCP      name=security         1m
store-fe          10.100.227.224   nodes         80/TCP      name=store-fe         1m
txn               10.100.222.102   nodes         80/TCP      name=txn              1m
```
#### Access Petstore Admin UI
```
$ kubectl describe svc/admin-fe
Name:			admin-fe
Namespace:		default
Labels:			name=admin-fe
Selector:		name=admin-fe
Type:			NodePort
IP:			10.100.2.62
Port:			<unnamed>	80/TCP
NodePort:		<unnamed>	30313/TCP
Endpoints:		10.244.98.7:80
Session Affinity:	ClientIP
No events.
```
Point your browser to http://172.17.8.102:30313 # get relevant port from above `kubectl describe svc/admin-fe` command

#### Access Petstore Store UI
```
$ kubectl describe svc/store-fe
Name:			store-fe
Namespace:		default
Labels:			name=store-fe
Selector:		name=store-fe
Type:			NodePort
IP:			10.100.227.224
Port:			<unnamed>	80/TCP
NodePort:		<unnamed>	31691/TCP
Endpoints:		10.244.98.9:80
Session Affinity:	ClientIP
No events.
```
Point your browser to http://172.17.8.102:31691 # get relevant port from above `kubectl describe svc/store-fe` command

```
./clean.sh
```

Clean.sh will clean all k8s resources



## Troubleshooting

#### I'm getting errors while waiting for Kubernetes master to become ready on a MacOS host!

You probably have a pre-existing Kubernetes config file on your system at `~/.kube/config`. Delete or move that file and try again.

#### When I run "kubectl get pods" I can see many pods not become to running state or they have lot of restarts.

This may happend first time when you run the system due to slow internet. Because it took lot of time to download k8s packs. You may resolve this by running following commands
```
./clean.sh
./petstore.sh
```



