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

package org.wso2.msf4j.internal.deployer;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;

/**
 * Test MSSJarProcessor.
 */
public class MSF4JJarProcessorTest {

    @Test
    public void testJAXRSJarObjectInitialization() throws MSF4JJarProcessorException {
        MSF4JJarProcessor mssJarProcessor = new MSF4JJarProcessor();
        String name = "/org.wso2.carbon.mss.jaxrs.sample.simplestockquote-1.0.zip";
        File jar = new File(MSF4JJarProcessorTest.class.getResource(name).getPath());
        Assert.assertTrue("Resource jar does not exist", jar.exists());
        List<Object> instances = mssJarProcessor.setArtifact(jar).process().getResourceInstances();
        Assert.assertTrue("No classes were loaded", !instances.isEmpty());
    }

}
