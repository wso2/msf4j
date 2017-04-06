# Request and response interceptors MSF4J fat jar Sample

A fat jar is a jar file which includes all the dependencies in one fat (uber) jar. This mode of creating a fat jar
and running it in a Java process is also referred to as server-less execution.

* See also; [MSF4J Interceptor Service - Deployable Jar mode](../deployable-jar-interceptor-service)
* See also; [MSF4J Interceptor Service - OSGi mode](../osgi-interceptor-service)

## Writing the pom.xml 

Your POM can inherit from [msf4j-service](../../../poms/msf4j-service). 
See details [here](../../../poms/msf4j-service).

The pom should include the following plugins

```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.skife.maven</groupId>
                <artifactId>really-executable-jar-maven-plugin</artifactId>
                <version>${maven.reallyexecutablejarplugin.version}</version>
                <configuration>
                    <programFile>restinterceptor</programFile>
                </configuration>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>really-executable-jar</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
```

The pom should include the following properties

```xml
    <properties>
        <microservice.mainClass>org.wso2.msf4j.samples.fatjarinterceptorservice.Application</microservice.mainClass>
    </properties>
```

## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample


Use following command to run the application
```
java -jar target/fatjar-interceptor-service-\<version>.jar
```

## How to test the sample

Use following cURL command.

```
curl http://localhost:8080/interceptor-service/service-name
```

You should get a successful response if everything worked fine.

## Invoking the service via HTTPS

Use following cURL command.

```
curl --insecure https://localhost:8444/interceptor-service/service-name
```

You should get a successful response if everything worked fine.