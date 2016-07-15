#!/usr/bin/env bash

baseDir=$(dirname "$0")

artifact="$baseDir/target/wildfly-echo-*.war"
startCmd="$baseDir/wildfly-10.0.0.Final/bin/standalone.sh"
stopCmd="$baseDir/wildfly-10.0.0.Final/bin/jboss-cli.sh --connect command=:shutdown"

endpoints=""
endpoints+="Echo->http://localhost:8080/wildfly-echo-message/app/EchoService/echo,"
endpoints+="File-Read-Write->http://localhost:8080/wildfly-echo-message/app/EchoService/fileecho"

lookFor="services are lazy, passive or on-demand"

waitTimeout=30

serverLog="$baseDir/server.log"
pidFile="$baseDir/pid"

function waitFor(){
    local waitTime=0
    while true
    do
        if [ $(tail "$serverLog" | grep "$lookFor" | wc -l) -gt 0 ]
        then
            echo true
            return
        fi
        sleep 1
        ((waitTime++))
        if [ $waitTime -gt $waitTimeout ]
        then
            echo false
            return
        fi
    done
}

function start(){
    stop
    nohup $startCmd > "$serverLog" &
    local pid=$!
    echo $pid > "$pidFile"
    local ret=$(waitFor)
    if [ $ret = "false" ]
    then
        echo "Unable to start server within $waitTimeout"
        stop
    else
        echo "Server started @ $pid"
    fi
}

function stop(){
    if [ -f "$pidFile" ]
    then
        local pid=$(cat "$pidFile")
        kill -9 $pid
        echo "Killed server @ $pid"
        rm -f "$pidFile"
    fi
    rm -f "$serverLog"
    $stopCmd
}

function getEndpoints(){
    echo "$endpoints"
}

function buildSample(){
    local curDir=$(pwd)
    cd $baseDir
    mvn clean package
    if [ ! -d "wildfly-10.0.0.Final" ]
    then
        if [ -f "wildfly-10.0.0.Final.zip" ]
        then
            unzip "wildfly-10.0.0.Final.zip"
        else
            if which wget >/dev/null
            then
                wget "http://download.jboss.org/wildfly/10.0.0.Final/wildfly-10.0.0.Final.zip"
                unzip "wildfly-10.0.0.Final.zip"
            else
                echo "couldnot find 'wget' to download wildfly server"
                echo "failed to prepare wildfly sample"
                return
            fi
        fi
    fi
    rm -f "wildfly-10.0.0.Final/standalone/deployments/wildfly*"
    cd $curDir
    cp -v $artifact "$baseDir/wildfly-10.0.0.Final/standalone/deployments/"
}

if [ "$1" = "start" ]
then
    start
elif [ "$1" = "stop" ]
then
    stop
    echo "Waiting for server termination"
    sleep 1
elif [ "$1" = "endpoints" ]
then
    getEndpoints
elif [ "$1" = "build" ]
then
    buildSample
fi
