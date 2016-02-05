#Creating a Microservice using msf4j-microservice-archetype

A Microservice based on WSO2 Microservices Framework can be created with one command using this archetype.

Following are the properties you need to pass in your command which describe the archetype you are going to use.

| Property            | Description                     | Mandatory/Optional |
| ------------------- | ------------------------------- | ------------------ |
| archetypeGroupId    | The groupId of the archetype    | Mandatory          |
| archetypeArtifactId | The artifactId of the archetype | Mandatory          |
| archetypeVersion    | The version of the archetype    | Optional           |

To read more on what other properties you can use when generating a project from an archetype please refer [1]

Following are the properties you need to pass in your command which describe the project you are creating using
this archetype.

| Property   | Description                          | Default value               |
| ---------  | ------------------------------------ | --------------------------- |
| groupId    | The groupId of the project           | org.wso2.msf4j              |
| artifactId | The artifactId of the project        | org.wso2.msf4j.microservice |
| version    | The version of the project           | 1.0.0-SNAPSHOT              |
| package    | The package hierarchy of the project | org.wso2.msf4j.microservice |

Example:

If you execute the following command,

```
mvn archetype:generate -DarchetypeGroupId=org.wso2.msf4j -DarchetypeArtifactId=msf4j-microservice-archetype -DarchetypeVersion=1.0.0-SNAPSHOT -DgroupId=org.sample -DartifactId=org.sample.project -Dversion=1.0.0 -Dpackage=org.sample.project

```
and see a result similar to the following archetype generation is successful.

```
[INFO] project created from Archetype in dir: /home/manurip/Documents/Work/Tasks/RnD/mss/archetype-creation/org.sample.project
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 37.586 s
[INFO] Finished at: 2016-02-04T21:07:36+05:30
[INFO] Final Memory: 16M/981M
[INFO] ------------------------------------------------------------------------

```

Following project will be created with the version 1.0.0

```
org.sample.project
├── pom.xml
└── src
    └── main
        └── java
            └── org
                └── sample
                    └── project
                        ├── Application.java
                        └── MicroService.java
```

If you don’t pass any of the project parameters you will be provided with the option to choose default values for some or all of the variable parameters depending on your choice.


References

[1] [http://maven.apache.org/archetype/maven-archetype-plugin/generate-mojo.html](http://maven.apache.org/archetype/maven-archetype-plugin/generate-mojo.html)
