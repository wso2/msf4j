#!/usr/bin/env bash

baseDir=$(dirname "$0")
concLevels="1 25 50 100 200 400 800 1600 3200"
loopReps=2
testLoops=10000
warmUpConc=50
warmUpLoop=10000

tmpDir="$baseDir/tmpData"
vendorsLoc="$baseDir/Samples"
payload="$baseDir/1kb_rand_data.txt"
timeStmp=$(date +%s)

types=()
declare -A MAP

function printResultStructures(){
    echo "Printing types.."
    for type in ${types[@]}
    do
        echo "$type"
    done
    echo ""
    echo "Printing results map.."
    for key in ${!MAP[@]}
    do
      echo "$key -> ${MAP[$key]}"
    done
}

function hash(){
    echo -n $1 | md5sum | awk '{print $1}'
}

function addNewType(){
    local newType=$1
    local isNew=true
    local type=""
    for type in ${types[@]}
    do
        if [ "$type" = "$newType" ]
        then
            isNew=false
            break
        fi
    done
    if "$isNew"
    then
        types+=($newType)
    fi
}

function addNewVendor(){
    local type=$1
    local vendor=$2
    if [ ${MAP["$type-vendor-n"]} ]
    then
        local vendorN=${MAP["$type-vendor-n"]}
        MAP["$type-vendor-$vendorN"]="$vendor"
        MAP["$type-vendor-n"]=$((vendorN+1))
    else
        MAP["$type-vendor-0"]="$vendor"
        MAP["$type-vendor-n"]=1
    fi
}

function processResults(){
    local metric=$1
    local resultsFile=$2
    local type=""
    local vendorI=0
    local conc=""

    rm -f "$resultsFile"

    for type in "${types[@]}"
    do
        echo "Test: $type," >> "$resultsFile"
        local isPrintH=true
        for conc in $concLevels
        do
            local header=""
            if "$isPrintH"
            then
                header="Concurrency, "
            fi
            local line="$conc, "
            for vendorI in $(seq 0 $((MAP["$type-vendor-n"]-1)))
            do
                local vendor=${MAP["$type-vendor-$vendorI"]}
                if "$isPrintH"
                then
                    header+="$vendor, "
                fi
                local tps=${MAP["$type-$vendor-$conc-$metric"]}
                line+="$tps, "
            done
            if "$isPrintH"
            then
                echo "$header" >> "$resultsFile"
                isPrintH=false
            fi
            echo "$line" >> "$resultsFile"
        done
        echo "" >> "$resultsFile"
    done
    echo "==========================================="
    echo "            Results ($metric)              "
    echo "==========================================="
    cat "$resultsFile"
}

function warmUp(){
    local service=$1
    echo "Warm up service $service"
    ab -k -p "$payload" -c $warmUpConc -n $warmUpLoop -H "Accept:text/plain" "$service" > /dev/null
}

function testConcLevel(){
    local service=$1
    local concLevel=$2
    local type=$3
    local vendor=$4
    local tps=0
    local meanLat=0
    local p90th=0
    for loopRep in $(seq 1 $loopReps)
    do
        local resOut="$tmpDir/$type-$vendor-conc$concLevel-rep$loopRep-loops$testLoops-time$timeStmp-$(uuidgen)"
        echo "Testing service: $service"
        echo "Testing concurrency $concLevel for repetition $loopRep at $resOut"
        ab -k -p "$payload" -c $concLevel -n $testLoops -H "Accept:text/plain" "$service" > "$resOut"

        local tpsV=$(cat "$resOut" | grep -Eo "Requests per second.*" | grep -Eo "[0-9]+" | head -1)
        tps=$(echo "$tps+$tpsV" | bc)
        echo "Tps at concurrency $concLevel in repetition $loopRep is $tpsV"

        local meanLatV=$(cat "$resOut" | grep -Eo "Time per request.*\(mean\)" | grep -Eo "[0-9]+(\.[0-9]+)?")
        meanLat=$(echo "$meanLat+$meanLatV" | bc)
        echo "Latency at concurrency $concLevel in repetition $loopRep is $meanLatV"

        local p90thV=$(cat "$resOut" |grep -Eo "90%.*" | grep -Eo "[0-9]+(\.[0-9]+)?" | tail -1)
        p90th=$(echo "$p90th+$p90thV" | bc)
        echo "90% at concurrency $concLevel in repetition $loopRep is $p90thV"

        echo "Waiting 1s.."
        sleep 1
    done
    echo "For $service"

    tps=$(echo "$tps/$loopReps" | bc)
    MAP["$type-$vendor-$concLevel-tps"]=$tps
    echo -e "\tFinal tps for $tps"

    meanLat=$(echo "$meanLat/$loopReps" | bc)
    MAP["$type-$vendor-$concLevel-meanLat"]=$meanLat
    echo -e "\tFinal mean latency is $meanLat"

    p90th=$(echo "$p90th/$loopReps" | bc)
    MAP["$type-$vendor-$concLevel-p90th"]=$p90th
    echo -e "\tFinal 90th percentile is $p90th"
}

function iterateConcLevels(){
    local vendorDir=$1
    local type=$2
    local vendor=$3
    local service=$4
    echo "Testing concurrency levels in $vendorDir"
    warmUp "$service" # Warm up the service before getting results
    local concLevel=""
    for concLevel in $concLevels
    do
        testConcLevel "$service" "$concLevel" "$type" "$vendor"
    done
}

function iterateVendors(){
    local vendors=$(ls "$1")
    local type=""
    local vendor=""
    for vendor in $vendors
    do
        local vendorDir="$1/$vendor"
        if [ -f "$vendorDir/run.sh" ]
        then
            local startState=$($vendorDir/run.sh "start")
            if [[ "$startState" = *"Server started"* ]]
            then
                echo "Server started in $vendorDir"
                local endpoints=$($vendorDir/run.sh "endpoints")
                endpoints=${endpoints//","/" "}
                for endpoint in $endpoints
                do
                    endpoint=(${endpoint//"->"/" "})
                    local type=${endpoint[0]}
                    local service=${endpoint[1]}
                    addNewType "$type"
                    addNewVendor "$type" "$vendor"
                    iterateConcLevels "$vendorDir" "$type" "$vendor" "$service"
                done
                $vendorDir/run.sh "stop"
            else
                echo "Server not started in $vendorDir"
            fi
        fi
    done

    processResults "tps" "$baseDir/results-tps.csv"
    echo ""
    processResults "meanLat" "$baseDir/results-latency.csv"
    echo ""
    processResults "p90th" "$baseDir/results-90th-percentile.csv"
    echo ""
    #printResultStructures
}

mkdir -p "$tmpDir"
base64 /dev/urandom | head -c 1024 > "$payload"

iterateVendors "$vendorsLoc"