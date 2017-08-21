#Creating a Microservice using the msf4j-microservice Maven archetype

A Microservice based on WSO2 Microservices Framework for Java (MSF4J) can be created with single command 
using this Maven archetype.

Here is an example;

```
mvn archetype:generate -DarchetypeGroupId=org.wso2.msf4j -DarchetypeArtifactId=msf4j-microservice 
-DarchetypeVersion=2.4.0-m1 -DgroupId=org.example -DartifactId=myservice -Dversion=1.0.0-SNAPSHOT 
-Dpackage=org.example.service

```

The above command will create an MSF4J microservice project structure for you similar to the one shown below;

```
myservice
├── pom.xml
└── src
    └── main
        └── java
            └── org
                └── example
                    └── service
                        ├── Application.java
                        └── MyService.java
```

##Properties

The following table lists down the properties specific to the msf4j-microservice Maven archetype;


| Property            | Description                     | Mandatory/Optional |
| ------------------- | ------------------------------- | ------------------ |
| archetypeGroupId    | The groupId of the archetype    | Mandatory          |
| archetypeArtifactId | The artifactId of the archetype | Mandatory          |
| archetypeVersion    | The version of the archetype    | Optional           |


The table below lists down the properties which are specific to the project you create.

| Property        | Description                          | Default value               |
| --------------- | ------------------------------------ | --------------------------- |
| groupId         | The groupId of the project           | org.example                 |
| artifactId      | The artifactId of the project        | msf4j-service               |
| version         | The version of the project           | 1.0.0-SNAPSHOT              |
| package         | The package hierarchy of the project | org.example.service         |
| serviceClass    | The names of the microservice class  | org.example.service         |

For more information about Maven archetypes, see [http://maven.apache.org/archetype/maven-archetype-plugin/generate-mojo.html](http://maven.apache.org/archetype/maven-archetype-plugin/generate-mojo.html)
