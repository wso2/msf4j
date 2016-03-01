#!/bin/bash

# exporting paths
source path.sh

cd $HOME/packs
[ -d ${DAS_PACK%.zip} ] && rm -fr ${DAS_PACK%.*}
unzip $DAS_PACK
echo "--------------------------------------------------------------"
echo "Setting up WSO2 Data Analytics Server"
echo "--------------------------------------------------------------"
echo "DAS Pack:"${DAS_PACK%.zip}
../../../../analytics/das-setup/./setup.sh -d ${DAS_PACK%.zip}
pwd
echo "--------------------------------------------------------------"
echo "Starting WSO2 Data Analytics Server"
echo "--------------------------------------------------------------"
cd ${DAS_PACK%.zip}/bin/
./wso2server.sh start -DportOffset=1

echo "--------------------------------------------------------------"
echo "Creating Kube-System Namespace, Kube-DNS, Kube-UI"
echo "--------------------------------------------------------------"
#kubectl create -f $VAGRANT_HOME/plugins/namespace/kube-system.json
#kubectl create -f $VAGRANT_HOME/plugins/dns/dns-service.yaml
#kubectl create -f $VAGRANT_HOME/plugins/dns/dns-controller.yaml
#kubectl create -f $VAGRANT_HOME/plugins/kube-ui/kube-ui-controller.yaml
#kubectl create -f $VAGRANT_HOME/plugins/kube-ui/kube-ui-service.yaml
#sleep 20


echo "--------------------------------------------------------------"
echo "Creating services for external endpoints"
echo "--------------------------------------------------------------"
cd $HOME
kubectl create -f external-endpoints/


echo "--------------------------------------------------------------"
echo "Deploying Redis Cluster"
echo "--------------------------------------------------------------"
cd $HOME
cd $REDIS_HOME/container/kubernetes/
kubectl create -f redis-master.yaml
sleep 30
kubectl create -f redis-sentinel-service.yaml
kubectl create -f redis-controller.yaml
kubectl create -f redis-sentinel-controller.yaml
kubectl scale rc redis --replicas=3
sleep 30
kubectl scale rc redis-sentinel --replicas=3
sleep 30
kubectl delete pods redis-master


sleep 20

echo "--------------------------------------------------------------"
echo "Deploying Pet"
echo "--------------------------------------------------------------"

cd $HOME
cd $PET_HOME/container/kubernetes/
kubectl create -f .


echo "--------------------------------------------------------------"
echo "Deploying FileServer"
echo "--------------------------------------------------------------"
cd $HOME
kubectl label nodes 172.17.8.102 disktype=ssd
cd $FILESERVER_HOME/container/kubernetes/
kubectl create -f .


echo "--------------------------------------------------------------"
echo "Deploying FrontEnd Admin"
echo "--------------------------------------------------------------"
cd $HOME
cd $FRONTEND_ADMIN/container/kubernetes/
kubectl create -f .

echo "--------------------------------------------------------------"
echo "Deploying FrontEnd User"
echo "--------------------------------------------------------------"
cd $HOME
cd $FRONTEND_USER/container/kubernetes/
kubectl create -f .


echo "--------------------------------------------------------------"
echo "Deploying Security"
echo "--------------------------------------------------------------"
cd $HOME
cd $SECURITY/container/kubernetes/
kubectl create -f .


echo "--------------------------------------------------------------"
echo "Deploying Transaction"
echo "--------------------------------------------------------------"
cd $HOME
cd $TRANSACTION/container/kubernetes/
kubectl create -f .

