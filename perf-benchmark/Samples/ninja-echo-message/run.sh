#!/usr/bin/env bash

baseDir=$(dirname "$0")

artifact="$baseDir/target/ninja-echo-message-*"
startCmd="java -jar $artifact"

endpoints=""
endpoints+="Echo->http://localhost:8080/EchoService/echo,"
endpoints+="File-Read-Write->http://localhost:8080/EchoService/fileecho"

lookFor="Started jetty"

waitTimeout=15

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
}

function getEndpoints(){
    echo "$endpoints"
}

function buildSample(){
    local curDir=$(pwd)
    cd $baseDir
    mvn clean package
    cd $curDir
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
