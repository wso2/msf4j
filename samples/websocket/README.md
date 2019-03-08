# MSF4J WebSocket Tutorial

In MSF4J there are two ways to write endpoints such as HTTP and WebSocket.
Here is the documentation for WebSocket in MSF4J.

WSO2 MSF4J is driven by annotations. Since all are written according to standard javax WebSocket
spec this is easy to learn and use. This tutorial goes through the very basic elements of 
WebSocket endpoint.

## Annotations of WebSocket
All these annotations are coming from javax.websocket API. Since you can easily migrate
your code written with javax WebSocket spec for another framework to MSF4J micro-framework.

### @ServerEndpoint
This annotation is a must for the framework to understand which this is a WebSocket server
endpoint. Without this annotation framework cannot understand whether this is a HTTP endpoint 
or a WebSocket endpoint.

```java
@ServerEndpoint("/chat/")
public class ChatAppEndpoint {

}
```

### @PathParam
This annotation is used to take the path parameters of URI. Let's take this server endpoint 
example
```java
@ServerEndpoint("/chat/{name}")
public class ChatEndpoint {
    
}
```
Here in the URI the part defined within curly brackets are known as path parameters.
In here "{name}"

So let's say the client is connected to the endpoint using 
"/chat/john". Then this means "name"="john".

These kind of path parameters can be taken as method parameters in WebSocket methods 
using @PathParam annotation.

```java
@OnOpen
public void myMethod(@PathParam("name") String name) {
    System.out.println("Hi my name is " + name);
}
```


### @OnOpen
This is a method level annotation and is used to trigger when a new connection is opened.
If user need to execute some logic when a new client is connected we can use this annotation 
for that purpose.

|parameter|description|mandatory/optional|
|---------|-----------|------------------|
|Session|Session of the new client connected|optional|
|0 to N no of String parameters annotated with @PathParam|Can have any no. of path parameters|optional|

ex:
```java
@OnOpen
public void onOpen(@PathParam("name") String name, WebSocketConnection webSocketConnection) {
    System.out.println("New client connected");
}
```

Note that @PathParams parameters should declare first before giving other parameters.

### @OnMessage
All the messages coming from the connected clients are coming to methods which are annotated with @OnMessage 

There are 3 kinds of Messages which can be received from from a WebSocket channel.

* Text Messages
* Binary Messages
* Pong Messages

Since there are 3 kinds of messages which can be received via same annotation the way it defines is by specifying 
the method parameters for each message type.
For each and every message type there are mandatory and optional parameters that we can define based on what the user 
wants to receive.

#### Text Messages
|parameter|description|mandatory/optional|
|---------|-----------|------------------|
|text|Text which receives from the client|mandatory|
|webSocketConnection|Connection of the client|optional|
|0 to n String parameters annotated with a @PathParam annotation|To retrieve path parameter|optional|

ex: 
```java
@ServerEndpoint(value = "/chat/{name}")
class exampleApp {
    @OnMessage
    public void onTextMessage(@PathParam("name") String name, String text, WebSocketConnection webSocketConnection) {
        // Your code goes here
    }
}
```

#### Binary Messages
|parameter|description|mandatory/optional|
|---------|-----------|------------------|
|bytes|This should be declared as a byte[] or ByteBuffer to receive the message from the client|mandatory|
|isFinal|This is a boolean which says that if the received buffer is a final fragment of a whole message. If user knows that only full messages are received every time this should not be declared|optional|
|webSocketConnection|Connection of the client|optional|
|0 to n String parameters annotated with a @PathParam annotation|To retrieve path parameter|optional|

ex: with ByteBuffer
```java
@ServerEndpoint(value = "/chat/{name}")
class exampleApp { 
    @OnMessage
    public void onBinaryMessage(byte[] bytes, WebSocketConnection webSocketConnection) {
        // Your code goes here
    }
}
```

ex: with byte array
```java
@ServerEndpoint(value = "/chat/{name}")
class exampleApp { 
    @OnMessage
    public void onBinaryMessage(ByteBuffer byteBuffer, boolean isFinalFragment, WebSocketConnection webSocketConnection) {
        // Your code goes here
    }
}
```

#### Pong Messages
This is received when server sends a ping message to check the connection.

|parameter|description|mandatory/optional|
|---------|-----------|------------------|
|pongMessage|PongMessage which receives from the client|mandatory|
|webSocketConnection|Connection of the client|optional|
|0 to n String parameters annotated with a @PathParam annotation|To retrieve path parameter|optional|

ex:
```java
@ServerEndpoint(value = "/chat/{name}")
class exampleApp { 
    @OnMessage
    public void onPongMessage(PongMessage pongMessage, WebSocketConnection webSocketConnection) {
       // Your code goes here
    }
}
```
_**Note: If you define other than these parameters the server might give an error since it cannot understand the type of message
this specific @OnMessage annotated method is dispatching**_


### @OnClose
OnClose is called when a connection between the client and the server is closed. By this user can identify the specific code
and the reason if exists for user to leave the server.

|parameter|description|mandatory/optional|
|---------|-----------|------------------|
|pongMessage|PongMessage which receives from the client|mandatory|
|webSocketConnection|Connection of the client|optional|
|0 to n String parameters annotated with a @PathParam annotation|To retrieve path parameter|optional|

ex: 
```java
@ServerEndpoint(value = "/chat/{name}")
class exampleApp { 
    @OnClose
    public void onClose(@PathParam("name") String name, CloseReason closeReason, WebSocketConnection webSocketConnection) {
        // Your code goes here
    }
}
```

### @OnError
This method is called when an error occurred in the server when reading or writing data to the client. 

|parameter|description|mandatory/optional|
|---------|-----------|------------------|
|throwable|Throwable which is thrown during the reading or writing data|mandatory|
|webSocketConnection|Connection of the client|optional|
|0 to n String parameters annotated with a @PathParam annotation|To retrieve path parameter|optional|

ex: 
```java
@ServerEndpoint(value = "/chat/{name}")
class exampleApp { 
    @OnError
    public void onError(Throwable throwable, WebSocketConnection webSocketConnection) {
        // Your code goes here
    }
}
```

## Server Push
Server push is one of the unique feature of WebSocket protocol. Unlike HTTP the server can write data to the client 
without a client request using server push. This allows user to exchange real time data from server to the client without 
methods like polling.

There are 2 ways which you can do server push is MSf4J.
* Using Session.getBasicRemote
    ```java
    webSocketConnection.pushText(message);
    webSocketConnection.pushBinary(message);
    ```
    This can be used in anywhere in the program to messages to the client.
    
* Using return types of methods
    ```java
    @OnMessage
    public String onTextMessage(String text, WebSocketConnection webSocketConnection) throws IOException {
        String msg =  "You said : " + text;
        return msg;
    }
    ```
    This return statements will return the message to the same client who sent the message.