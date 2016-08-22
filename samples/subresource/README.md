# JAX-RS Sub-Resource Sample

This is the MSF4J Sub resource sample that demonstrate hwo to use sub-resource with msf4j.

## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample

From the target directory, run
```
java -jar helloworld-*.jar
```

## How to test the sample

We will use the cURL command line tool for testing. You can use your preferred HTTP or REST client too.
Try out the following curl commands

```
curl http://localhost:8080/country/SL/team
curl http://localhost:8080/country/SL/team/123/details/name
curl http://localhost:8080/country/SL/team/123/bowlerType
curl -X POST -H "Content-Type: application/x-www-form-urlencoded" -d 'type=All rounder&countryName=Sri Lanka' "http://localhost:8080/country/2123/team/123"
curl -X POST -H "Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW" -F "type=All rounder" -F "countryName=Sri Lanka" "http://localhost:8080/country/2123/team/123"
```

