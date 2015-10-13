# Executable jar Sample

This sample demonstrates how to create an uber jar which contains your microservice as well as all its dependencies.

Here's how to deploy & run your microservice.

```
public static void main(String[] args) {
    new MicroservicesRunner().deploy(new StockQuoteService()).start();
}
```

How to run the sample
------------------------------------------
1. Use maven to build the sample
```
mvn clean package
```
2. Use following command to run the application
```
java -jar target/SimpleStockQuote-1.0.0.jar
```
How to test the sample
------------------------------------------

Use following cURL commands.
```
curl --user john:john http://localhost:8080/StockQuote/get/IBM

```
