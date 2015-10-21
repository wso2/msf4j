#!/bin/sh

if [ -z "$1" ] || [ -z "$2" ]; then
    echo "Usage: init.sh <host> <port>"
    exit 1
fi

for i in {0..10}
do
  age=$((1+$i))
  price=`echo 100.99 + $i | bc`

  curl -v -X POST -H "Content-Type:application/json" \
  -d '{"category":{"name":"cat"},"ageMonths":"'"$age"'","price":"'"$price"'","image":"http://foo.com/cat'"$i"'.png"}' \
  http://$1:$2/pet/
done


for i in {0..10}
do
    curl http://$1:$2/pet/pet-$i
    echo ""
done

#172.17.4.201:31485