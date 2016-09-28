#!/bin/bash

PRE_REQ=1
IS_PACK="wso2is-5.0.0.zip"
DAS_PACK="wso2das-3.0.0.zip"
HOME=`pwd`
PET_HOME="../../microservices/pet/"
FILESERVER_HOME="../../microservices/fileserver/"
ADMIN="../../microservices/frontend-admin/"
STORE_FE="../../microservices/frontend-user/"
SECURITY="../../microservices/security/"
TRANSACTION="../../microservices/transaction/"

# Checking prerequisites for the build
command -v docker >/dev/null 2>&1 || { echo >&2 "Missing Docker!!! Build required docker install in the host. Try 'curl -sSL https://get.docker.com/ | sh' "; $PRE_REQ=1; }

if [ $PRE_REQ -eq 0 ];then
    echo "--------------------------------------------------------------"
    echo "All prerequisite not met. Existing build..."
    echo "--------------------------------------------------------------"
    exit;
fi

rm -rf das/modules das/repository packages/* security pet transaction fileserver admin-fe store-fe

git pull

[ ! -d $HOME/packages ] && mkdir -p $HOME/packages

echo "--------------------------------------------------------------"
echo "Building petstore sample"
echo "--------------------------------------------------------------"
cd $HOME
cd $PET_HOME
mvn clean install -Dmaven.test.skip=true

cd $HOME
cd ../../microservices/security/
mvn clean install -Dmaven.test.skip=true

cd $HOME
cd ../../microservices/transaction/
mvn clean install -Dmaven.test.skip=true

cd $HOME
cd ../../microservices/fileserver/
mvn clean install -Dmaven.test.skip=true

# Copy DAS CApps
echo "--------------------------------------------------------------"
echo "Copy DAS CApps"
echo "--------------------------------------------------------------"
cd $HOME
../../../../analytics/das-setup/setup.sh -d $HOME/das

# copy Pet
echo "--------------------------------------------------------------"
echo "Copy Pet"
echo "--------------------------------------------------------------"
cd $HOME
[ ! -d $HOME/pet ] && mkdir -p $HOME/pet
cp -f $PET_HOME/target/petstore-pet-*.jar $HOME/packages/petstore-pet.jar
cp -f $PET_HOME/data-agent-conf.xml $HOME/packages/data-agent-conf.xml
cp -f $PET_HOME/client-truststore.jks $HOME/packages/client-truststore.jks
#copy Dockerfile
cp -f ../../microservices/pet/container/docker/Dockerfile $HOME/pet/

echo "--------------------------------------------------------------"
echo "Copy FileServer"
echo "--------------------------------------------------------------"
cd $HOME
[ ! -d $HOME/fileserver ] && mkdir -p $HOME/fileserver
cp -f $FILESERVER_HOME/target/petstore-fileserver-*.jar $HOME/packages/petstore-fileserver.jar
cp -f $FILESERVER_HOME/data-agent-conf.xml $HOME/packages/data-agent-conf.xml
cp -f $FILESERVER_HOME/client-truststore.jks $HOME/packages/client-truststore.jks
#copy Dockerfile
cp -f ../../microservices/fileserver/container/docker/Dockerfile $HOME/fileserver/


echo "--------------------------------------------------------------"
echo "Copy FrontEnd Admin"
echo "--------------------------------------------------------------"
cd $HOME
cp -fr $ADMIN/container/docker $HOME/admin-fe

echo "--------------------------------------------------------------"
echo "Copy FrontEnd User"
echo "--------------------------------------------------------------"
cd $HOME
cp -fr $STORE_FE/container/docker $HOME/store-fe

echo "--------------------------------------------------------------"
echo "Copy Security"
echo "--------------------------------------------------------------"
[ ! -d $HOME/security ] && mkdir -p $HOME/security
cp -f $SECURITY/target/petstore-security-*.jar $HOME/packages/petstore-security.jar
cp -f $SECURITY/data-agent-conf.xml $HOME/packages/data-agent-conf.xml
cp -f $SECURITY/client-truststore.jks $HOME/packages/client-truststore.jks
#copy Dockerfile
cp -f ../../microservices/security/container/docker/Dockerfile $HOME/security/


echo "--------------------------------------------------------------"
echo "Copy Transaction"
echo "--------------------------------------------------------------"
[ ! -d $HOME/transaction ] && mkdir -p $HOME/transaction
cp -f $TRANSACTION/target/petstore-txn-*.jar $HOME/packages/petstore-txn.jar
cp -f $TRANSACTION/data-agent-conf.xml $HOME/packages/data-agent-conf.xml
cp -f $TRANSACTION/client-truststore.jks $HOME/packages/client-truststore.jks
#copy Dockerfile
cp -f ../../microservices/transaction/container/docker/Dockerfile $HOME/transaction/


docker-compose up --build
