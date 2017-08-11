/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */
package org.wso2.msf4j.osgi.test;


import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.kernel.CarbonServerInfo;

import javax.inject.Inject;

/**
 * OSGi tests class to test MSF4J startup.
 *
 * @since 5.0.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class MSF4JStartupTest {
    private static final String TRANSPORT_ID = "DummyTransport";

//    @Inject
//    private TransportManager transportManager;

//    @Inject
//    private CommandProvider transportCommandProvider;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Inject
    BundleContext bundleContext;

    @Test
    public void testServerStarup() {
        Assert.assertNotNull(carbonServerInfo, "CarbonServerInfo Service is null");
    }

//    @Test(dependsOnMethods = {"testTransportManagerExistence"})
//    public void testUnsuccessfulStartTransport() {
//
//    }
}
