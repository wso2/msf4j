#!/bin/bash

# exporting paths
source path.sh

# Checking prerequisites for the build
command -v docker >/dev/null 2>&1 || { echo >&2 "Missing Docker!!! Build required docker install in the host. Try 'curl -sSL https://get.docker.com/ | sh' "; $PRE_REQ=1; }

if [ $PRE_REQ -eq 0 ];then
    echo "--------------------------------------------------------------"
    echo "All prerequisite not met. Existing build..."
    echo "--------------------------------------------------------------"
    exit;
fi

git pull

echo "--------------------------------------------------------------"
echo "Building petstore sample"
echo "--------------------------------------------------------------"
cd $HOME
cd ../microservices/pet/
mvn clean install -Dmaven.test.skip=true

cd $HOME
cd ../microservices/security/
mvn clean install -Dmaven.test.skip=true

cd $HOME
cd ../microservices/transaction/
mvn clean install -Dmaven.test.skip=true

cd $HOME
cd ../microservices/fileserver/
mvn clean install -Dmaven.test.skip=true


# copy Pet
echo "--------------------------------------------------------------"
echo "Copy Pet"
echo "--------------------------------------------------------------"
cd $HOME
[ ! -d $SHARE_FOLDER/pet ] && mkdir -p $SHARE_FOLDER/pet
cp -fr $PET_HOME/container/docker $SHARE_FOLDER/pet
[ ! -d $SHARE_FOLDER/pet/docker/packages ] && mkdir -p $SHARE_FOLDER/pet/docker/packages
cp -f $PET_HOME/target/petstore-pet-*.jar $SHARE_FOLDER/pet/docker/packages/petstore-pet.jar
cp -f $PET_HOME/data-agent-conf.xml $SHARE_FOLDER/pet/docker/packages/data-agent-conf.xml
cp -f $PET_HOME/client-truststore.jks $SHARE_FOLDER/pet/docker/packages/client-truststore.jks


echo "--------------------------------------------------------------"
echo "Copy FileServer"
echo "--------------------------------------------------------------"
cd $HOME
[ ! -d $SHARE_FOLDER/pet ] && mkdir -p $SHARE_FOLDER/fileserver
cp -fr $FILESERVER_HOME/container/docker $SHARE_FOLDER/fileserver
[ ! -d $SHARE_FOLDER/fileserver/docker/packages ] && mkdir -p $SHARE_FOLDER/fileserver/docker/packages
cp -f $FILESERVER_HOME/target/petstore-fileserver-*.jar $SHARE_FOLDER/fileserver/docker/packages/petstore-fileserver.jar
cp -f $FILESERVER_HOME/data-agent-conf.xml $SHARE_FOLDER/fileserver/docker/packages/data-agent-conf.xml
cp -f $FILESERVER_HOME/client-truststore.jks $SHARE_FOLDER/fileserver/docker/packages/client-truststore.jks

echo "--------------------------------------------------------------"
echo "Copy FrontEnd Admin"
echo "--------------------------------------------------------------"
cd $HOME

[ ! -d $SHARE_FOLDER/frontend_admin ] && mkdir -p $SHARE_FOLDER/frontend_admin
cp -fr $FRONTEND_ADMIN/container $SHARE_FOLDER/frontend_admin

echo "--------------------------------------------------------------"
echo "Copy FrontEnd User"
echo "--------------------------------------------------------------"
cd $HOME

cd $HOME
[ ! -d $SHARE_FOLDER/frontend_user ] && mkdir -p $SHARE_FOLDER/frontend_user
cp -fr $FRONTEND_USER/container $SHARE_FOLDER/frontend_user


echo "--------------------------------------------------------------"
echo "Copy Security"
echo "--------------------------------------------------------------"
[ ! -d $SHARE_FOLDER/security ] && mkdir -p $SHARE_FOLDER/security
cp -fr $SECURITY/container/docker $SHARE_FOLDER/security
[ ! -d $SHARE_FOLDER/security/docker/packages ] && mkdir -p $SHARE_FOLDER/security/docker/packages
cp -f $SECURITY/target/petstore-security-*.jar $SHARE_FOLDER/security/docker/packages/petstore-security.jar
cp -f $SECURITY/data-agent-conf.xml $SHARE_FOLDER/security/docker/packages/data-agent-conf.xml
cp -f $SECURITY/client-truststore.jks $SHARE_FOLDER/security/docker/packages/client-truststore.jks


echo "--------------------------------------------------------------"
echo "Copy Transaction"
echo "--------------------------------------------------------------"
[ ! -d $SHARE_FOLDER/transaction ] && mkdir -p $SHARE_FOLDER/transaction
cp -fr $TRANSACTION/container/docker $SHARE_FOLDER/transaction
[ ! -d $SHARE_FOLDER/transaction/docker/packages ] && mkdir -p $SHARE_FOLDER/transaction/docker/packages
cp -f $TRANSACTION/target/petstore-txn-*.jar $SHARE_FOLDER/transaction/docker/packages/petstore-txn.jar
cp -f $TRANSACTION/data-agent-conf.xml $SHARE_FOLDER/transaction/docker/packages/data-agent-conf.xml
cp -f $TRANSACTION/client-truststore.jks $SHARE_FOLDER/transaction/docker/packages/client-truststore.jks



echo "--------------------------------------------------------------"
echo "Setting up CoreOS and Kubernetes"
echo "--------------------------------------------------------------"

if [ ! -f /usr/local/bin/kubectl ];then
    # TODO need to check incompatibility	
    if [[ "$OSTYPE" == "linux-gnu" ]]; then
        wget https://storage.googleapis.com/kubernetes-release/release/v1.2.0/bin/linux/amd64/kubectl
    elif [[ "$OSTYPE" == "darwin"* ]]; then
       wget https://storage.googleapis.com/kubernetes-release/release/v1.2.0/bin/darwin/amd64/kubectl
    fi
    chmod +x kubectl
    mv kubectl /usr/local/bin/kubectl
fi
cp -f $HOME/bootstrap.sh $VAGRANT_HOME/docker/  
cd $VAGRANT_HOME
NODE_MEM=2048 NODE_CPUS=2 NODES=2 USE_KUBE_UI=true vagrant up

ssh -i ~/.vagrant.d/insecure_private_key core@172.17.8.102 '/vagrant/bootstrap.sh'
ssh -i ~/.vagrant.d/insecure_private_key core@172.17.8.103 '/vagrant/bootstrap.sh'

source ~/.bash_profile

kubectl get nodes

cd $HOME
# deploying petstore
./petstore.sh
