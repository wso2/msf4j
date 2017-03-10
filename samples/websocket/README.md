#MSF4J Websocket Support
This explains how to write a 
MSF4J WebSocket Server endpoint accurately in OSGi and Fat jar mode.
In the current implementation MSF4J supports only annotation based approach
to write a WebSocket endpoint.

Samples :
* [Echo Server - Fat Jar](echoServer/fatjar) - Running a basic echo server in fat jar mode
* [Echo Server - OSGi](echoServer/bundle) - Running a basic echo server in OSGi mode
* [Chat Application - Fat jar](chatApp/fatjar) - Running a chat application with multiple users in fat jar mode
* [Chat Application - OSGi](chatApp/bundle) - Running a chat application with multiple users in OSGi mode

##WebSocket Server Endpoint
WebSocket server endpoint is the API that is exposed to the user. 
To do so @ServerEndpoint from javax.websocket should be implemented.

```java
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint("/test")
public class TestEndpoint {

}
```

This way you can define a server endpoint. Once you define the 
server endpoint this class can be exposed as a WebSocket endpoint
to the user.

####URI mapping of @ServerEndpoint
Server endpoint can contain variables which can be used as parameters in the processing </br>
ex : @ServerEndpoint("/chat/{name}") </br>
This endpoint contains **{name}** which indicates that **"name"** is a variable using **"{}"**.
So if some client connects to server as </br>
"ws://host:port/chat/**john**" </br>
This indicates that **name = john** </br>
To obtain those mapped URI parameter values, String parameters annotated with a @PathParam annotation is used.

#####@PathParam 
As explained above @PathParam can be used to obtain URI mapped path parameter values.
```java
@ServerEndpoint("/test/{name}")
public class TestEndpoint {
    @OnMessage
    public void onTextMessage(@PathParam("name") String name, String text, Session session) {
        //User source code for a text message
    }
}
```
_Note that those @PathParam values should be declared first in a method._

###Method Annotations of a WebSocket Server Endpoint
####@OnOpen
@OnOpen annotated method is called when a new client is connected to the endpoint. 
You can add any logic which you want to execute when a user is connected to the 
endpoint.
</br></br>
@OnOpen method parameters : 
* javax.websocket.Session - Optional
* 0 to n String parameters annotated with a @PathParam annotation - Optional

```java
import javax.websocket.server.ServerEndpoint;
import javax.websocket.OnOpen;

@ServerEndpoint("/test")
public class TestEndpoint {
    @OnOpen
    public void onOpen(Session session) {
        System.out.println("New client is connected");
    }
}
```

####@OnMessage
@OnMessage annotated method receives the messages to the endpoint.
</br>
There can be 3 types of Messages
#####Text
All the text messages from clients are dispatched to this method. </br>
method parameters : 

|parameter|Description|Mandatory/Optional|
|---------|-----------|------------------|
|String|Indicates that this @OnMessage is for text messages|Mandatory|
|javax.websocket.Session|Indicated session details aren needed by the user|Optional|
|0 to n String parameters annotated with a [@PathParam](#####@PathParam) annotation|Indicated user needs path parameters for the processing|Optional|
    
#####Binary
All the binary messages are dispatched to this method. </br>
method parameters : 

|parameter|Description|Mandatory/Optional|
|---------|-----------|------------------|
|java.nio.ByteBuffer / byte array (byte[])|Only one parameter with one the mentioned data structures. This indicates that this @OnMessage method is for binary messages.|Mandatory|
|javax.websocket.Session|Indicated session details aren needed by the user|Optional|
|0 to n String parameters annotated with a [@PathParam](#####@PathParam) annotation|Indicated user needs path parameters for the processing|Optional|

#####Pong
All the pong messages are dispatched to this method. </br>
method parameters :

|parameter|Description|Mandatory/Optional|
|---------|-----------|------------------|
|javax.websocket.PongMessage|Indicates that this @OnMessage method is for Pong Messages|Mandatory|
|javax.websocket.Session|Indicated session details aren needed by the user|Optional|

####@OnClose
@OnClose annotated method runs when the connection between a client and 
the server is closed. </br>
@OnClose method parameters : 

|parameter|Description|Mandatory/Optional|
|---------|-----------|------------------|
|javax.websocket.CloseReason|To identify the reason and close code to the closure of the connection|Optional|
|javax.websocket.Session|Indicated session details aren needed by the user|Optional|
|0 to n String parameters annotated with a [@PathParam](#####@PathParam) annotation|Indicated user needs path parameters for the processing|Optional|

####@OnError
@OnError annotated method runs when an error occurs in the endpoint. </br>
@OnError method parameters : 

|parameter|Description|Mandatory/Optional|
|---------|-----------|------------------|
|Throwable|Indicated the user needs to know the reason for the error|Mandatory|
|javax.websocket.Session|Indicated session details aren needed by the user|Optional|
|0 to n String parameters annotated with a [@PathParam](#####@PathParam) annotation|Indicated user needs path parameters for the processing|Optional|

##WebSocket Server Push
One of the main feature of WebSocket protocol is the ability to send server 
data to the client without client request. In MSF4J this can be done is 2 ways
* Using Session </br>
session.getBasicRemote().sendText(message);</br>
session.getBasicRemote().sendBinary(message); </br>
can be used to push data to client. <br>
Look at [Chat Application - Fat jar](chatApp/fatjar) sample
* Using method return types </br>
If the developer needs to send a message to the user as soon as a message
received then this approach can be used easily. Current implementation 
supports 3 kinds of return types for any javax.websocket method annotations
    * String
    * byte[]
    * java.nio.ByteBuffer </br>
    
    Look at [Chat Application - Fat jar](chatApp/fatjar) sample </br>
    _Note : If developer does not need to send anything after a message is 
    received then make the method return type **void**_



