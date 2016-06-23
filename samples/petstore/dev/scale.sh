#!/bin/bash

kubectl scale rc pet --replicas=3
sleep 20
kubectl scale rc security --replicas=3
sleep 20
kubectl scale rc store-fe --replicas=3
sleep 20
kubectl scale rc txn --replicas=3
kubectl scale rc admin-fe --replicas=3
