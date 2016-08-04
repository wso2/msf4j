#!/bin/bash

service=$1
fileName=""
if [ -z $2 ]; then
    fileName="latency-results.csv"
else
    fileName="latency-results-$2.csv"
fi

echo "Running test for: $service"

base64 /dev/urandom | head -c 1024 > 1kb_rand_data.txt

rm $fileName
echo min, mean, [+/-sd], median, max, 66%, 75%, 80%, 90%, 95%, 98%, 99% >> $fileName
for i in 1 2 3
    do
for i in 1 25 50 100 200 400 800 1600 3200
    do
        echo start concurrency $i on $(date)
        val=$(ab -k -p 1kb_rand_data.txt -c $i -n 500000 -H "Accept:text/plain" $service \
        | pcregrep -M "Total:.*(\n|.)* 100%" \
        | sed '4d' \
        | sed '11d' \
        | sed '2,3d' \
        | sed -r 's/\Total:\s*//' \
        | sed -r 's/[0-9]{1,10}%\s*//' \
        | xargs \
        | tr ' ' ','/)
       # echo "Latency information for concurrency level $i" >> $fileName
        echo "$val" >> $fileName
       # echo "\n" >> $fileName
        echo "waiting for 5sec..."
        sleep 5
done
done
echo
echo ==================
echo =  Latency Info  =
echo ==================
echo
cat $fileName
