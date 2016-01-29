/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.kernel.utils.CarbonServerInfo;

import javax.inject.Inject;

/**
 * Base OSGi class to test the OSGi status of the org.wso2.carbon.core bundle.
 *
 * @since 5.0.0
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class BaseOSGiTest {

    @Inject
    private BundleContext bundleContext;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Test
    public void testBundleContextStatus() {
        Assert.assertNotNull(bundleContext, "Bundle Context is null");
    }

    @Test
    public void testCarbonCoreBundleStatus() {

        Bundle coreBundle = null;
        for (Bundle bundle : bundleContext.getBundles()) {
            if (bundle.getSymbolicName().equals("org.wso2.carbon.core")) {
                coreBundle = bundle;
                break;
            }
        }
        Assert.assertNotNull(coreBundle, "Carbon Core bundle not found");
        Assert.assertEquals(coreBundle.getState(), Bundle.ACTIVE, "Carbon Core Bundle is not activated");
    }
}
