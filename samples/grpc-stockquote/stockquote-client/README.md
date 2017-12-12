# StockQuote gRPC Client Sample

gRPC is a framework which enable client application to directly call methods on a server application on a different 
machine as if it was a local object. gRPC is based around the idea of defining a service, specifying the methods that
 can be called remotely with their parameters and return types.

This sample shows how to create service client using server definition file ([.proto file](../stockquote-service/src/main/proto/helloworld.proto)).

## Writing the pom.xml 

Your POM can inherit from [msf4j-service](../../../poms/msf4j-service). 
See details [here](../../../poms/msf4j-service).

Add following dependencies and plugin definition to the pom file

````xml
    <build>
        <extensions>
            <extension>
                <groupId>kr.motd.maven</groupId>
                <artifactId>os-maven-plugin</artifactId>
                <version>${os.maven.plugin.version}</version>
            </extension>
        </extensions>
        <plugins>
            <plugin>
                <groupId>org.xolstice.maven.plugins</groupId>
                <artifactId>protobuf-maven-plugin</artifactId>
                <configuration>
                    <protocArtifact>com.google.protobuf:protoc:3.4.0:exe:${os.detected.classifier}</protocArtifact>
                    <pluginId>grpc-java</pluginId>
                    <pluginArtifact>io.grpc:protoc-gen-grpc-java:1.7.0:exe:${os.detected.classifier}
                    </pluginArtifact>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>compile</goal>
                            <goal>compile-custom</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <dependency>
            <groupId>org.wso2.msf4j</groupId>
            <artifactId>msf4j-grpc</artifactId>
        </dependency>
    </dependencies>
````

## Writing service client

We need to follow the basic steps to generate service stubs using proto file.

1. First we need to copy proto file to src/main/proto/ directory of the project
2. Run mvn build command and generate service stubs.

```
mvn package
```

3. Using the service stubs, write the client code to invoke service methods


## How to build the sample

From this directory, run

```
mvn clean install
```

## How to run the sample

From the target directory, run
```
java -jar stockquote-client-*.jar
```
Note that you need to start the [stockquote-service](../stockquote-service) before you run this sample.

You should get a successful response if everything worked fine.

