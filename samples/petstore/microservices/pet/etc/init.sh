#!/bin/sh

for i in {0..10}
do
  age=$((1+$i))
  price=`echo 100.99 + $i | bc`

  curl -v -X POST -H "Content-Type:application/json" \
  -d '{"id":"pet-'"$i"'","category":{"name":"cat"},"ageMonths":"'"$age"'","price":"'"$price"'","dateAdded":1444827363766,"image":"http://foo.com/cat'"$i"'.png"}' \
  http://$1:$2/pet/
done


for i in {0..10}
do
    curl http://$1:$2/pet/pet-$i
    echo ""
done

#172.17.4.201:31485