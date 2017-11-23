/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.msf4j;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.msf4j.service.TestMicroservice;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Testing the "msf4j.host" environment variable.
 */
public class HostBindingTest {
    private final TestMicroservice testMicroservice = new TestMicroservice();
    
    /**
     * Testing happy path.
     */
    @Test
    public void testHostForMicroserviceRunner() throws InterruptedException {
        MicroservicesRunner microservicesRunner = new MicroservicesRunner(3333);
        microservicesRunner.deploy(testMicroservice);
        microservicesRunner.start();
        Thread.sleep(100);
        Assert.assertTrue(this.isHostPortAvailable("127.0.0.1", 3333),
                                                                "Unable to connect to service started on 127.0.0.1");
        Assert.assertTrue(this.isHostPortAvailable("localhost", 3333),
                                                                "Unable to connect to service started on localhost");
        microservicesRunner.stop();
    }
    
    /**
     * Hosting a test microservice on 127.0.0.1 and check if it can be accessed with 127.0.0.1 and that it cannot be
     * accessed through any other network ip.
     */
    @Test
    public void testDifferentHostForMicroserviceRunner() throws SocketException, InterruptedException {
        System.setProperty("msf4j.host", "127.0.0.1");
        MicroservicesRunner microservicesRunner = new MicroservicesRunner(4444);
        microservicesRunner.deploy(testMicroservice);
        microservicesRunner.start();
        Thread.sleep(100);
        Assert.assertTrue(this.isHostPortAvailable("127.0.0.1", 4444),
                                                                "Unable to connect to service started on 127.0.0.1");
        Assert.assertTrue(this.isHostPortAvailable("localhost", 4444),
                                                                "Unable to connect to service started on localhost");
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        while (e.hasMoreElements()) {
            NetworkInterface n = (NetworkInterface) e.nextElement();
            Enumeration ee = n.getInetAddresses();
            while (ee.hasMoreElements()) {
                InetAddress i = (InetAddress) ee.nextElement();
                String networkHost = i.getHostAddress();
                if (!networkHost.equals("127.0.0.1") && !networkHost.equals("localhost")) {
                    Assert.assertFalse(this.isHostPortAvailable(networkHost, 4444),
                                                                    "Should not be able to connect on " + networkHost);
                }
            }
        }
        System.clearProperty("msf4j.host");
        microservicesRunner.stop();
    }
    
    /**
     * Check if a port is open with a given host. An SO timeout of 3000 milliseconds is used.
     * @param host The host to be checked.
     * @param port The port to be checked.
     * @return True if available, else false.
     */
    private boolean isHostPortAvailable(String host, int port) {
        try {
            boolean isConnected;
            Socket client = new Socket();
            client.connect(new InetSocketAddress(host, port), 3000);
            isConnected = client.isConnected();
            client.close();
            return isConnected;
        } catch (IOException ignored) {
            return false;
        }
    }
}
