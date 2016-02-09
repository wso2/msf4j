#!/bin/bash

service=$1

echo "Running test for: $service"

base64 /dev/urandom | head -c 1024 > 1kb_rand_data.txt

rm throughput-results.txt
for i in 1 25 50 100 200 400 800 1600 3200 6400 10000
    do
        nreqs=0
        for j in {1..3}
            do
	            echo start concurrency $i on $(date)
                val=$(ab -k -p 1kb_rand_data.txt -c $i -n 1000000 -H "Accept:text/plain" $service | grep "Requests per second" | grep -Eo "[0-9]*\.[0-9]*" | grep -Eo "^[0-9]*")
                echo "For concurrency level $i -> $val req/sec"
                nreqs=$((nreqs + val))
	            echo end concurrency $i on $(date)
                echo "waiting for 5sec..."
                sleep 5
        done
        nreqs=$((nreqs / 3))
        echo "Average for concurrency level $i -> $nreqs req/sec"
        echo "$nreqs" >> throughput-results.txt
done

echo
echo =============================================
echo =       Throughtput Results req/sec         =
echo =============================================
echo
echo Concurrency levels
echo 1 25 50 100 200 400 800 1600 3200 6400 10000
echo
echo ---------------------------------------------
echo
cat throughput-results.txt
