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


import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.CarbonServerInfo;
import org.wso2.carbon.osgi.test.util.CarbonSysPropConfiguration;
import org.wso2.carbon.osgi.test.util.OSGiTestConfigurationUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 * OSGi tests class to test MSF4J startup.
 *
 * @since 5.0.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
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

    @Configuration
    public Option[] createConfiguration() {

        List<Option> optionList = new ArrayList<>();
        //TODO: add MSF4J specific bundles

        /*optionList.add(mavenBundle().artifactId("org.wso2.carbon.sample.deployer.mgt").groupId("org.wso2.carbon")
                .versionAsInProject());
        optionList.add(mavenBundle().artifactId("org.wso2.carbon.sample.order.resolver").groupId("org.wso2.carbon")
                .versionAsInProject());*/

        /*optionList.add(
                mavenBundle().groupId("org.wso2.msf4j").artifactId("msf4j-core").versionAsInProject());
        optionList.add(
                mavenBundle().groupId("javax.ws.rs").artifactId("javax.ws.rs-api").versionAsInProject());
        optionList.add(
                mavenBundle().groupId("com.google.code.gson").artifactId("gson").versionAsInProject());
        optionList.add(
                mavenBundle().groupId("com.google.guava").artifactId("guava").versionAsInProject());
        optionList.add(
                mavenBundle().groupId("org.apache.servicemix.bundles").
                        artifactId("org.apache.servicemix.bundles.commons-beanutils").versionAsInProject());
        optionList.add(
                mavenBundle().groupId("org.wso2.msf4j").artifactId("msf4j-analytics").versionAsInProject());
        optionList.add(
                mavenBundle().groupId("org.wso2.msf4j").artifactId("jaxrs-delegates").versionAsInProject());

        optionList.add(
                mavenBundle().groupId("org.wso2.carbon.metrics").
                        artifactId("org.wso2.carbon.metrics.annotation").versionAsInProject());
        optionList.add(
                mavenBundle().groupId("org.wso2.carbon.metrics").
                        artifactId("org.wso2.carbon.metrics.common").versionAsInProject());
        optionList.add(
                mavenBundle().groupId("org.wso2.carbon.metrics").
                        artifactId("org.wso2.carbon.metrics.manager").versionAsInProject());
        optionList.add(
                mavenBundle().groupId("org.wso2.carbon.metrics").
                        artifactId("org.wso2.carbon.metrics.impl").versionAsInProject());
        optionList.add(
                mavenBundle().groupId("org.wso2.carbon.metrics").
                        artifactId("org.wso2.carbon.metrics.das.reporter").versionAsInProject());
        optionList.add(
                mavenBundle().groupId("org.wso2.carbon.analytics-common").
                        artifactId("org.wso2.carbon.databridge.agent").versionAsInProject());
        optionList.add(
                mavenBundle().groupId("org.wso2.carbon.analytics-common").
                        artifactId("org.wso2.carbon.databridge.commons").versionAsInProject());
        optionList.add(
                mavenBundle().groupId("org.wso2.carbon.analytics-common").
                        artifactId("org.wso2.carbon.databridge.commons.thrift").versionAsInProject());

        optionList.add(
                mavenBundle().groupId("org.wso2.orbit.com.lmax").artifactId("disruptor").versionAsInProject());
        optionList.add(
                mavenBundle().groupId("io.dropwizard.metrics").artifactId("metrics-core").versionAsInProject());
        optionList.add(
                mavenBundle().groupId("commons-io.wso2").artifactId("commons-io").versionAsInProject());
        optionList.add(
                mavenBundle().groupId("libthrift.wso2").artifactId("libthrift").versionAsInProject());
        optionList.add(
                mavenBundle().groupId("commons-pool.wso2").artifactId("commons-pool").versionAsInProject());*/


        String currentDir = Paths.get("").toAbsolutePath().toString();
        Path carbonHome = Paths.get(currentDir, "target", "carbon-home");

        CarbonSysPropConfiguration sysPropConfiguration = new CarbonSysPropConfiguration();
        sysPropConfiguration.setCarbonHome(carbonHome.toString());
        sysPropConfiguration.setServerKey("carbon-jndi");
        sysPropConfiguration.setServerName("WSO2 MSF4J Server");
        sysPropConfiguration.setServerVersion("1.1.0");

        optionList = OSGiTestConfigurationUtils.getConfiguration(optionList, null);
        return optionList.toArray(new Option[optionList.size()]);
    }

    @Test
    public void testServerStarup() {
        Assert.assertNotNull(carbonServerInfo, "CarbonServerInfo Service is null");
    }

//    @Test(dependsOnMethods = {"testTransportManagerExistence"})
//    public void testUnsuccessfulStartTransport() {
//
//    }
}
