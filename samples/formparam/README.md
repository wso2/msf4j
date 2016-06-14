# FormParam Sample

This is the MSF4J sample which demonstrate how to use FormParam.

## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample

From the target directory, run
```
java -jar formparam-*.jar
```

## How to test the sample

We will use the cURL command line tool for testing. You can use your preferred HTTP or REST client too.
Simple client is also available in the sample

## Sample CURL Command 
curl -X POST -H "Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW" -F "name=WSO2" -F "age=10" -F "image=@abc.png" "http://localhost:8080/formService/simpleFormStreaming"