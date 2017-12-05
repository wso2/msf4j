/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.msf4j.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.util.client.websocket.WebSocketClient;
import org.wso2.msf4j.websocket.endpoint.ChatAppEndpoint;
import org.wso2.msf4j.websocket.endpoint.EchoEndpoint;
import org.wso2.msf4j.websocket.exception.WebSocketEndpointAnnotationException;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import javax.net.ssl.SSLException;

/**
 * Test the WebSocket endpoint deployment.
 * Check all the methods of the endpoint.
 */
public class DeploymentTest {

    private static final Logger log = LoggerFactory.getLogger(DeploymentTest.class);
    private final String host = "localhost";
    private final String port = "8080";
    private final int sleepTime = 100;

    private String echoUrl = "ws://" + host + ":" + port + "/echo";
    private String chatUrl = "ws://" + host + ":" + port + "/chat/";

    private MicroservicesRunner microservicesRunner = new MicroservicesRunner();

    @BeforeClass
    public void setup() throws WebSocketEndpointAnnotationException {
        log.info(System.lineSeparator() +
                         "--------------------------------WebSocket Deployment Test--------------------------------");
        microservicesRunner.deployWebSocketEndpoint(new EchoEndpoint());
        microservicesRunner.deployWebSocketEndpoint(new ChatAppEndpoint());
        microservicesRunner.start();
    }

    @Test(description = "Testing the echoing the message sent by client for text, binary and pong messages.")
    public void testReply() throws InterruptedException, SSLException, URISyntaxException {
        WebSocketClient echoClient = new WebSocketClient(echoUrl);
        //Test handshake
        Assert.assertTrue(echoClient.handhshake());

        //Test Echo String
        String textSent = "test";
        echoClient.sendText(textSent);
        Thread.sleep(sleepTime);
        String textReceived = echoClient.getTextReceived();
        Assert.assertEquals(textReceived, textSent);

        //Test echo binaryMessage
        byte[] bytes = {1, 2, 3, 4, 5};
        ByteBuffer bufferSent = ByteBuffer.wrap(bytes);
        echoClient.sendBinary(bufferSent);
        Thread.sleep(sleepTime);
        ByteBuffer bufferReceived = echoClient.getBufferReceived();
        Assert.assertEquals(bufferReceived, bufferSent);

        //Test the Pong Message
        byte[] pingBytes = {6, 7, 8, 9, 10};
        ByteBuffer pingBufferSent = ByteBuffer.wrap(pingBytes);
        echoClient.sendPing(pingBufferSent);
        Thread.sleep(sleepTime);
        ByteBuffer pongBufferReceived = echoClient.getBufferReceived();
        Assert.assertEquals(pongBufferReceived, pingBufferSent);

        //Closing the connection
        echoClient.shutDown();
    }

    @Test(description = "Testing broadcasting messages for text, binary and pong using two clients.")
    public void testBroadcast() throws InterruptedException, SSLException, URISyntaxException {
        //Initializing local variables
        String textReceived;
        String client1Name = "abc";
        String client2Name = "xyz";
        WebSocketClient chatClient1 = new WebSocketClient(chatUrl + client1Name);
        WebSocketClient chatClient2 = new WebSocketClient(chatUrl + client2Name);

        //Check the handshake
        Assert.assertTrue(chatClient1.handhshake());
        Assert.assertTrue(chatClient2.handhshake());
        Thread.sleep(sleepTime);
        textReceived = chatClient1.getTextReceived();
        Assert.assertEquals(textReceived, client2Name + " connected to chat");

        //Check the broadcast text
        String textSent = "test";
        chatClient1.sendText(textSent);
        Thread.sleep(sleepTime);
        Assert.assertEquals(chatClient1.getTextReceived(), client1Name + ":" + textSent);
        Assert.assertEquals(chatClient2.getTextReceived(), client1Name + ":" + textSent);

        //Check close connection
        chatClient2.shutDown();
        Thread.sleep(sleepTime);
        Assert.assertEquals(chatClient1.getTextReceived(), client2Name + " left the chat");
        chatClient1.shutDown();
    }

    @AfterClass
    public void cleanUp() {
        microservicesRunner.stop();
    }
}
