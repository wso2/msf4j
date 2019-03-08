#Simple Chat Application MSF4J OSGi bundle sample

This sample demonstrate how to use MSF4J WebSocket support 
to write a simple chat application for OSGi. 

In this sample we exposed chat application as an OSGi service 
which implements org.wso2.msf4j.websocket.WebSocketEndpoint interface
 as shown in the following code.
 
```java
@Component(
        name = "org.wso2.msf4j.chatApp",
        service = WebSocketEndpoint.class,
        immediate = true
)
@ServerEndpoint("/chat/{name}")
public class ChatAppEndpoint implements WebSocketEndpoint {

}
```

In this example we have used dynamic URI to take the name
using **/{name}** in the @ServerEndpoint

Ex : ws://localhost:9090/chat/**john**

This indicates that the person who is joining the chat
is john. 

To access a variable in the URI we can use @PathParam
annotation  as a method parameter 
provided by javax.websocket.

ex : 
```java
@OnOpen
public void onOpen(@PathParam("name") String name, WebSocketConnection webSocketConnection session) {
    String msg = name + " connected to chat";
}
```
Note that @PathParams method parameters should declare first
before giving other parameters.

##Server Push
There are 2 ways which you can do server pushes.
* Using WebSocketConnection webSocketConnection.getBasicRemote
    ```java
    webSocketConnection.pushText(message);
    webSocketConnection.pushBinary(message);
    ```
* Using return types of methods
    ```java
    @OnMessage
    public String onTextMessage(String text, WebSocketConnection webSocketConnection) throws IOException {
        String msg =  "You said : " + text;
        return msg;
    }
    ```
    This return sends the value to Remote Endpoint