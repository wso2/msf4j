#!/usr/bin/env bash

baseDir=$(dirname "$0")
vendorsLoc="$baseDir/Samples"

function cleanup(){
    local vendors=$(ls "$vendorsLoc")
    local type=""
    local vendor=""
    for vendor in $vendors
    do
        local vendorDir="$vendorsLoc/$vendor"
        if [ -f "$vendorDir/run.sh" ]
        then
            local pidFile=$vendorDir/pid
            if [ -f $pidFile ]
            then
                $vendorDir/run.sh "stop"
            fi
        fi
    done
}

function buildSamples(){
    local vendors=$(ls "$vendorsLoc")
    local type=""
    local vendor=""
    for vendor in $vendors
    do
        local vendorDir="$vendorsLoc/$vendor"
        if [ -f "$vendorDir/run.sh" ]
        then
            $vendorDir/run.sh "build"
        fi
    done
}

if [ "$1" = "build" ]
then
    cleanup
    buildSamples
else
    cleanup
    $baseDir/excecute-tests.sh
fi

if [ ! "$?" = 0 ]
then
    echo "Test were not completed successfully"
    echo "Cleaning up.."
    cleanup
fi