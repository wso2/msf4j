/*
 * Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.msf4j.test;

import org.wso2.carbon.launcher.CarbonServerEvent;
import org.wso2.carbon.launcher.CarbonServerListener;

/**
 * Pins PAX Exam's RBC RMI hostname to loopback before the OSGi framework
 * starts. Java 11 changed hostname resolution so that the default hostname
 * may resolve to a non-loopback address, breaking the PAX Exam RBC connection.
 * This listener is registered via carbon.server.listeners in launch.properties
 * and runs inside the Carbon container JVM at STARTING time.
 */
public class RBCHostFixListener implements CarbonServerListener {

    @Override
    public void notify(CarbonServerEvent event) {
        if (event.getType() == CarbonServerEvent.STARTING) {
            System.setProperty("org.ops4j.pax.exam.rbc.rmi.host", "localhost");
            System.setProperty("java.rmi.server.hostname", "127.0.0.1");
        }
    }
}
