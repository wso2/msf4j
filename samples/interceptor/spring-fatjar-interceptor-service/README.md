# Request and response interceptors MSF4J Spring fat jar Sample

A fat jar is a jar file which includes all the dependencies in one fat (uber) jar. This mode of creating a fat jar
and running it in a Java process is also referred to as server-less execution.

* See also; [MSF4J Interceptor Service - Fat Jar mode](../fatjar-interceptor-service)
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
        <microservice.mainClass>
            org.wso2.msf4j.samples.springfatjarinterceptorservice.Application
        </microservice.mainClass>
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
java -jar target/spring-fatjar-interceptor-service-\<version>.jar
```

## How to test the sample

Use following cURL command.

```
curl http://localhost:8090/reception-service/say-hello/John
```

You should get a hello response if everything worked fine (Hello John in this case).