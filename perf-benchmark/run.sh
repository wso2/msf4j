#!/usr/bin/env bash

baseDir=$(dirname "$0")
testTypeLoc="$baseDir/Test-Types"

function cleanup(){
    local types=$(ls "$testTypeLoc")
    local type=""
    for type in $types
    do
        local typeDir="$testTypeLoc/$type"
        if [ -d "$typeDir" ]
        then
            local vendors=$(ls "$typeDir")
            local vendor=""
            for vendor in $vendors
            do
                local vendorDir="$typeDir/$vendor"
                pidFile=$vendorDir/pid
                if [ -f $pidFile ]
                then
                    $vendorDir/run.sh "stop"
                fi
            done
        fi
    done
}

$baseDir/excecute-tests.sh

if [ ! "$?" = 0 ]
then
    echo "Test were not completed successfully"
    echo "Cleaning up.."
    cleanup
fi