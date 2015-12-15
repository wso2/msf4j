# Automated deployment of the Petstore sample

## Pre-requisites

 * **[Vagrant](https://www.vagrantup.com)**
 * a supported Vagrant hypervisor
 	* **[Virtualbox](https://www.virtualbox.org)** (the default)
 	
### MacOS X

On **MacOS X** (and assuming you have [homebrew](http://brew.sh) already installed) run

```
brew update
brew install wget
```

### Linux OS - Running Kubernetes locally via Docker

Instead of using Vagrant, you can use Docker to set up Kubernetes locally in your machine with Linux OS. 
Navigate to the **deployment** directory inside **petstore** directory in the sample and do the following modifications in the relevant script files. This would enable you to run the Petstore sample with MacOS X as well as with Linux.

####**run.sh**

Replace 
```
cp -f $HOME/bootstrap.sh $VAGRANT_HOME/docker/  
cd $VAGRANT_HOME
NODE_MEM=2048 NODE_CPUS=2 NODES=2 USE_KUBE_UI=true vagrant up
source ~/.bash_profile
```
with 
```
 if [[ "$OSTYPE" == "darwin"* ]]; then
	cp -f $HOME/bootstrap.sh.tmp $VAGRANT_HOME/docker/bootstratp.sh  
	cd $VAGRANT_HOME
	NODE_MEM=2048 NODE_CPUS=2 NODES=2 USE_KUBE_UI=true vagrant up
	source ~/.bash_profile

elif  [[ "$OSTYPE" == "linux-gnu" ]]; then
	echo "--------------------------------------------------------------"
	echo "Setting up Kubernetes locally via Docker"
	echo "--------------------------------------------------------------"
	
	#run etcd
	docker run --net=host -d gcr.io/google_containers/etcd:2.0.12 /usr/local/bin/etcd --addr=127.0.0.1:4001 --bind-addr=0.0.0.0:4001 --data-dir=/var/etcd/data
	
	#run master
	docker run \
    --volume=/:/rootfs:ro \
    --volume=/sys:/sys:ro \
    --volume=/dev:/dev \
    --volume=/var/lib/docker/:/var/lib/docker:ro \
    --volume=/var/lib/kubelet/:/var/lib/kubelet:rw \
    --volume=/var/run:/var/run:rw \
    --net=host \
    --pid=host \
    --privileged=true \
    -d \
    gcr.io/google_containers/hyperkube:v1.1.1 \
    /hyperkube kubelet --containerized --hostname-override="127.0.0.1" --address="0.0.0.0" --api-servers=http://localhost:8080 --config=/etc/kubernetes/manifests-multi --cluster_dns=10.0.0.10  --cluster_domain=cluster.local

	#run service proxy
	docker run -d --net=host --privileged gcr.io/google_containers/hyperkube:v1.1.1 /hyperkube proxy --master=http://127.0.0.1:8080 --v=2

	echo "--------------------------------------------------------------"
	echo "Kubernetes was set up"
	echo "--------------------------------------------------------------"
	
	sed -i -e "s@HOME=.*@HOME=$VAGRANT_HOME/docker@g" ./bootstrap.sh	
	chmod 755 ./bootstrap.sh
	./bootstrap.sh
fi	

```

####**petstore.sh**

Replace 
```
echo "--------------------------------------------------------------"
echo "Deploying FileServer"
echo "--------------------------------------------------------------"
cd $HOME
kubectl label nodes 172.17.8.102 disktype=ssd
cd $FILESERVER_HOME/container/kubernetes/
kubectl create -f .
```
with 
```
echo "--------------------------------------------------------------"
echo "Deploying FileServer"
echo "--------------------------------------------------------------"
cd $HOME
kubectl label nodes 172.17.8.102 disktype=ssd
cd $FILESERVER_HOME/container/kubernetes/
if [[ "$OSTYPE" == "darwin"* ]]; then
	kubectl create -f .
elif [[ "$OSTYPE" == "linux-gnu" ]]; then		
	sed -i -e "s@path: \/home\/core\/fileserver@path: \/home\/$USER\/fileserver@g" fileserver-rc.yaml
	sed -i -e "s@nodeSelector:@ @g" fileserver-rc.yaml
	sed -i -e "s@disktype: ssd@ @g" fileserver-rc.yaml				
	kubectl create -f .
fi
```

And replace
```
echo "--------------------------------------------------------------"
echo "Creating Kube-System Namespace, Kube-DNS, Kube-UI"
echo "--------------------------------------------------------------"
kubectl create -f $VAGRANT_HOME/plugins/namespace/kube-system.json
kubectl create -f $VAGRANT_HOME/plugins/dns/dns-service.yaml
kubectl create -f $VAGRANT_HOME/plugins/dns/dns-controller.yaml
kubectl create -f $VAGRANT_HOME/plugins/kube-ui/kube-ui-controller.yaml
kubectl create -f $VAGRANT_HOME/plugins/kube-ui/kube-ui-service.yaml
sleep 20
```
with
```
echo "--------------------------------------------------------------"
echo "Creating Kube-System Namespace, Kube-DNS, Kube-UI"
echo "--------------------------------------------------------------"
kubectl create -f $VAGRANT_HOME/plugins/namespace/kube-system.json
if [[ "$OSTYPE" == "darwin"* ]]; then
	kubectl create -f $VAGRANT_HOME/plugins/dns/dns-service.yaml
elif [[ "$OSTYPE" == "linux-gnu" ]]; then		
	sed -i -e "s@10.100.0.10@10.0.0.10@g" $VAGRANT_HOME/plugins/dns/dns-service.yaml				
	kubectl create -f $VAGRANT_HOME/plugins/dns/dns-service.yaml
fi
kubectl create -f $VAGRANT_HOME/plugins/dns/dns-controller.yaml
kubectl create -f $VAGRANT_HOME/plugins/kube-ui/kube-ui-controller.yaml
kubectl create -f $VAGRANT_HOME/plugins/kube-ui/kube-ui-service.yaml
sleep 20
```

##Run automated deployment 

Download WSO2 Identity server pack (zip) and copy into packs folder

```
./run.sh 
```
It will download and configure CoreOS vagrant boxes and setup Kubernetes 1.1.1 in a 3 CoreOS nodes. Complied product-mss, build all docker files, deploy k8s replication controllers, services ..etc.

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

## Troubleshooting

#### I'm getting errors while waiting for Kubernetes master to become ready on a MacOS host!

You probably have a pre-existing Kubernetes config file on your system at `~/.kube/config`. Delete or move that file and try again.

#### When I run "kubectl get pods" I can see many pods not become to running state or they have lot of restarts.

This may happend first time when you run the system due to slow internet. Because it took lot of time to download k8s packs. You may resolve this by running following commands
```
./clean.sh
./petstore.sh
```



