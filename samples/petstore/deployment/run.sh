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

#git pull

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


echo "--------------------------------------------------------------"
echo "Copy FileServer"
echo "--------------------------------------------------------------"
cd $HOME
[ ! -d $SHARE_FOLDER/pet ] && mkdir -p $SHARE_FOLDER/fileserver
cp -fr $FILESERVER_HOME/container/docker $SHARE_FOLDER/fileserver
[ ! -d $SHARE_FOLDER/fileserver/docker/packages ] && mkdir -p $SHARE_FOLDER/fileserver/docker/packages
cp -f $FILESERVER_HOME/target/petstore-fileserver-*.jar $SHARE_FOLDER/fileserver/docker/packages/petstore-fileserver.jar

echo "--------------------------------------------------------------"
echo "Copy FrontEnd Admin"
echo "--------------------------------------------------------------"
cd $HOME
#get latest from git
#if [ ! -d petstore-admin-fe ];then
#   echo "--------------------------------------------------------------"
#   echo "Clone source code from https://github.com/lakwarus/petstore-admin-fe.git"
#   echo "--------------------------------------------------------------"
#   git clone https://github.com/lakwarus/petstore-admin-fe.git
#else
#    echo "-------------------------------------------------------------------"
#    echo "Fetching new updates from https://github.com/lakwarus/petstore-admin-fe.git"
#    echo "-------------------------------------------------------------------"
#    cd petstore-admin-fe
#    git pull
#fi

[ ! -d $SHARE_FOLDER/frontend_admin ] && mkdir -p $SHARE_FOLDER/frontend_admin
cp -fr $FRONTEND_ADMIN/container $SHARE_FOLDER/frontend_admin

#cp -fr $FRONTEND_ADMIN/container/docker $SHARE_FOLDER/frontend_admin
#[ ! -d $SHARE_FOLDER/frontend_admin/docker/packages ] && mkdir -p $SHARE_FOLDER/frontend_admin/docker/packages
#cp -f $FRONTEND_ADMIN/target/petstore-admin.war $SHARE_FOLDER/frontend_admin/docker/packages/


echo "--------------------------------------------------------------"
echo "Copy FrontEnd User"
echo "--------------------------------------------------------------"
cd $HOME
#get latest from git
#if [ ! -d petstore-user-fe ];then
#   echo "--------------------------------------------------------------"
#   echo "Clone source code from github.com/lakwarus/petstore-user-fe.git"
#   echo "--------------------------------------------------------------"
#   git clone https://github.com/lakwarus/petstore-user-fe.git
#else
#    echo "-------------------------------------------------------------------"
#    echo "Fetching new updates from https://github.com/lakwarus/petstore-user-fe.git"
#    echo "-------------------------------------------------------------------"
#    cd petstore-user-fe
#    git pull
#fi

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


echo "--------------------------------------------------------------"
echo "Copy Transaction"
echo "--------------------------------------------------------------"
[ ! -d $SHARE_FOLDER/transaction ] && mkdir -p $SHARE_FOLDER/transaction
cp -fr $TRANSACTION/container/docker $SHARE_FOLDER/transaction
[ ! -d $SHARE_FOLDER/transaction/docker/packages ] && mkdir -p $SHARE_FOLDER/transaction/docker/packages
cp -f $TRANSACTION/target/petstore-txn-*.jar $SHARE_FOLDER/transaction/docker/packages/petstore-txn.jar

#echo "--------------------------------------------------------------"
#echo "Cleaning up old docker files"
#echo "--------------------------------------------------------------"
#[ -f $SHARE_FOLDER/fileserver.tgz ] && rm $SHARE_FOLDER/fileserver.tgz 
#[ -f $SHARE_FOLDER/pet.tgz ] && rm $SHARE_FOLDER/pet.tgz 


echo "--------------------------------------------------------------"
echo "Setting up CoreOS and Kubernetes"
echo "--------------------------------------------------------------"

if [ ! -f /usr/local/bin/kubectl ];then
    # TODO need to check incompatibility	
    if [[ "$OSTYPE" == "linux-gnu" ]]; then
        wget https://storage.googleapis.com/kubernetes-release/release/v1.1.1/bin/linux/amd64/kubectl
    elif [[ "$OSTYPE" == "darwin"* ]]; then
       wget https://storage.googleapis.com/kubernetes-release/release/v1.1.1/bin/darwin/amd64/kubectl
    fi
    chmod +x kubectl
    mv kubectl /usr/local/bin/kubectl
fi
cp -f $HOME/bootstrap.sh $VAGRANT_HOME/docker/  
cd $VAGRANT_HOME
NODE_MEM=2048 NODE_CPUS=2 NODES=2 USE_KUBE_UI=true vagrant up

kubectl get nodes

cd $HOME
# deploying petstore
./petstore.sh
