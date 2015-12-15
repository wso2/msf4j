# Automated deployment of the Petstore sample

## Support Operating Systems
 * Linux
 * MacOS X

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


##Run automated deployment 

Download WSO2 Identity server pack (zip) and copy into packs folder

Run the following commands.
```
export KUBERNETES_MASTER=http://172.17.8.101:8080
```
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



