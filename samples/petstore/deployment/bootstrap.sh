#!/bin/bash

HOME="/vagrant"
PET=$HOME/pet/docker
FILESERVER=$HOME/fileserver/docker
FRONTEND_ADMIN=$HOME/frontend_admin/container
FRONTEND_USER=$HOME/frontend_user/container
SECURITY=$HOME/security/docker
TRANSACTION=$HOME/transaction/docker


# creating fileserver folder
mkdir /home/core/fileserver
chmod -R 777 /home/core/fileserver

echo "------------------------------------------------------------------"
echo " load / pull kubernetes/redis:v1"
echo "------------------------------------------------------------------"

if [ -f $HOME/redis.tgz ];then
    cd $HOME
    docker load < redis.tgz
else
    docker pull kubernetes/redis:v1
    sleep 2
    docker save kubernetes/redis:v1 > $HOME/redis.tgz
fi

echo "------------------------------------------------------------------"
echo " load / pull redis"
echo "------------------------------------------------------------------"

if [ -f $HOME/redis-org.tgz ];then
    cd $HOME
    docker load < redis-org.tgz
else
    docker pull redis
    sleep 2
    docker save redis > $HOME/redis-org.tgz
fi


echo "------------------------------------------------------------------"
echo " load / pull ubuntu:14.04"
echo "------------------------------------------------------------------"

if [ -f $HOME/ubuntu.tgz ];then
    cd $HOME
    docker load < ubuntu.tgz
else
    docker pull ubuntu:14.04 
    sleep 2
    docker save ubuntu:14.04 > $HOME/ubuntu.tgz
fi

echo "------------------------------------------------------------------"
echo " load / pull php:5.6-apache"
echo "------------------------------------------------------------------"

if [ -f $HOME/php.tgz ];then
    cd $HOME
    docker load < php.tgz
else
    docker pull php:5.6-apache 
    sleep 2
    docker save php:5.6-apache > $HOME/php.tgz
fi


echo "------------------------------------------------------------------"
echo " load / pull java:8-jre"
echo "------------------------------------------------------------------"

if [ -f $HOME/java.tgz ];then
    cd $HOME
    docker load < java.tgz
else
    docker pull java:8-jre
    sleep 2
    docker save java:8-jre > $HOME/java.tgz
fi


#echo "------------------------------------------------------------------"
#echo " load / pull tomcat:8.0.28-jre8"
#echo "------------------------------------------------------------------"
#
#if [ -f $HOME/tomcat.tgz ];then
#    cd $HOME
#    docker load < tomcat.tgz
#else
#    docker pull tomcat:8.0.28-jre8
#    sleep 2
#    docker save tomcat:8.0.28-jre8 > $HOME/tomcat.tgz
#fi


echo "------------------------------------------------------------------"
echo "building / load pet docker"
echo "------------------------------------------------------------------"

cd $PET
docker build -t wso2msf4j/petstore-pet .
sleep 3
if docker images |grep wso2msf4j/petstore-pet >/dev/null 2>&1
    then
        echo "wso2msf4j/petstore-pet image build success!!"
    else
        echo "wso2msf4j/petstore-pet building again........."
        cd $PET
        docker build -t wso2msf4j/petstore-pet .
fi


echo "------------------------------------------------------------------"
echo "building fileserver docker"
echo "------------------------------------------------------------------"
cd $FILESERVER
docker build -t wso2msf4j/petstore-fileserver .

if docker images |grep wso2msf4j/petstore-fileserver >/dev/null 2>&1
    then
        echo "wso2msf4j/petstore-fileserver image build success!!"
    else
        cd $FILESERVER
        docker build -t wso2msf4j/petstore-fileserver .
fi


echo "------------------------------------------------------------------"
echo "building FrontEnd Admin"
echo "------------------------------------------------------------------"
cd $FRONTEND_ADMIN
docker build -t wso2msf4j/petstore-admin-fe .

if docker images |grep wso2msf4j/petstore-admin-fe >/dev/null 2>&1
    then
        echo "wso2msf4j/petstore-admin-fe image build success!!"
    else
        cd $FRONTEND_ADMIN
        docker build -t wso2msf4j/petstore-admin-fe .
fi


echo "------------------------------------------------------------------"
echo "building FrontEnd User"
echo "------------------------------------------------------------------"
cd $FRONTEND_USER
docker build -t wso2msf4j/petstore-store-fe .

if docker images |grep wso2msf4j/petstore-store-fe >/dev/null 2>&1
    then
        echo "wso2msf4j/petstore-store-fe image build success!!"
    else
        cd $FRONTEND_USER
        docker build -t wso2msf4j/petstore-store-fe .
fi


echo "------------------------------------------------------------------"
echo "building Security"
echo "------------------------------------------------------------------"
cd $SECURITY
docker build -t wso2msf4j/petstore-security .

if docker images |grep wso2msf4j/petstore-security >/dev/null 2>&1
    then
        echo "wso2msf4j/petstore-security image build success!!"
    else
        cd $SECURITY
        docker build -t wso2msf4j/petstore-security .
fi


echo "------------------------------------------------------------------"
echo "building Transaction"
echo "------------------------------------------------------------------"
cd $TRANSACTION
docker build -t wso2msf4j/petstore-txn .

if docker images |grep wso2msf4j/petstore-txn >/dev/null 2>&1
    then
        echo "wso2msf4j/petstore-txn image build success!!"
    else
        cd $TRANSACTION
        docker build -t wso2msf4j/petstore-txn .
fi



