/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mss.internal.router;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.mss.HttpStreamer;

/**
 * Tests functionality fo HttpResourceModel.
 */
public class HttpResourceModelTest {

    @Test
    public void testStreamingReqSupportedCheckWhenStreamingSupported() throws NoSuchMethodException {
        TestClass testObj = new TestClass();
        HttpResourceModel httpResourceModel = new HttpResourceModel("", testObj.getClass()
                .getMethod("methodWithHttpStreaming", HttpStreamer.class),
                testObj, null);
        Assert.assertTrue(httpResourceModel.isStreamingReqSupported());
        Assert.assertTrue(httpResourceModel.isStreamingReqSupported());
    }

    @Test
    public void testStreamingReqSupportedCheckWhenStreamingUnsupported() throws NoSuchMethodException {
        TestClass testObj = new TestClass();
        HttpResourceModel httpResourceModel = new HttpResourceModel("", testObj.getClass()
                .getMethod("methodWithNoHttpStreaming", Object.class),
                testObj, null);
        Assert.assertTrue(!httpResourceModel.isStreamingReqSupported());
        Assert.assertTrue(!httpResourceModel.isStreamingReqSupported());
    }

    /**
     * Test class used for testing HttpResourceMethod functionality.
     */
    private static class TestClass {

        public void methodWithHttpStreaming(HttpStreamer httpStreamer) {
        }

        public void methodWithNoHttpStreaming(Object object) {
        }

    }

}
