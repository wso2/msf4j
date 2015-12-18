#!/bin/bash

# exporting paths
source path.sh

cd $HOME/packs
cd ${IS_PACK%.zip}/bin/
./wso2server.sh stop


kubectl delete services --all
kubectl delete rc --all
kubectl delete pods --all

kubectl delete services --all --namespace=kube-system
kubectl delete rc --all --namespace=kube-system
kubectl delete pods --all --namespace=kube-system

