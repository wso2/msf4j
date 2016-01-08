# SimpleStockQuote Deployable jar Sample

This sample shows how to develop and deploy a microservice as a mss deployable jar.
See also.. [mss lite](../stockquote-mss-lite), [mss bundle](../stockquote-mss-bundle)


## pom.xml in deployable jar project


You have to inherit from [mss-jar-parent](../../mss-jar-parent) when creating the pom.xml for deployable jars. 
See details [here](../../mss-jar-parent).


## Using classes of other OSGi bundles


If you need to use classes of other OSGi bundles in the service classes of a deployable jar you have to 
create a dummy OSGi fragment bundle as workaround.

To create the OSGi fragment bundle you have to create the following pom.xml
```xml
<project xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="http://maven.apache.org/POM/4.0.0"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>
    <groupId>org.wso2.carbon.mss</groupId>
    <artifactId>deployable-jar-fragment</artifactId>
    <version>1.0.0</version>
    <packaging>bundle</packaging>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.felix</groupId>
                <artifactId>maven-bundle-plugin</artifactId>
                <version>3.0.1</version>
                <extensions>true</extensions>
                <configuration>
                    <instructions>
                        <Bundle-SymbolicName>${project.artifactId}</Bundle-SymbolicName>
                        <Bundle-Name>${project.artifactId}</Bundle-Name>

                        <Import-Package>
                            package1.to.use.in.jar;resolution:=optional,
                            package2.to.use.in.jar;resolution:=optional
                        </Import-Package>

                        <Private-Package>
                        </Private-Package>
                        <Fragment-Host>org.wso2.carbon.mss</Fragment-Host>
                    </instructions>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

In the above pom you can specify the packages that you want to use in the deployable jar. 
To create the fragment bundle from the above pom run
```
mvn clean install
```

After creating the bundle copy it to "[MSS-SERVER-HOME]/osgi/dropins" directory and restart the server.
Now you can deploy jars that use the classes in packages you imported in the fragment bundle.


## How to build the sample



From this directory, run

```
mvn clean install
```

## How to run the sample



Unzip wso2 MSS product and navigate to the bin directory. Then run the following command to start the MSS server.
```
./wso2server.sh
```

The copy the target/stockquote-mss-deployable-jar-1.0.0-SNAPSHOT.jar to deployment/microservices directory of WSO2 MSS.
Then the jar will be automatically deployed to the server runtime.


## How to test the sample



Use following cURL commands.
```
curl http://localhost:8080/stockquote/IBM
```
