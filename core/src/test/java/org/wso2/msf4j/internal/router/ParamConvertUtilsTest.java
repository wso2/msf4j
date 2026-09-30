/*
 * Copyright (c) 2026, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.msf4j.internal.router;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.UUID;

public class ParamConvertUtilsTest {

    @DataProvider
    public Object[][] conversions() {
        return new Object[][]{
                {int.class, "42", 42},
                {Integer.class, "42", 42},
                {long.class, "9000000000", 9000000000L},
                {double.class, "1.5", 1.5d},
                {boolean.class, "true", true},
                {Boolean.class, "false", false},
                {String.class, "app-1", "app-1"},
                {int.class, "abc", 0},
                {Integer.class, "abc", 0},
                {boolean.class, "abc", false},
        };
    }

    @Test(dataProvider = "conversions")
    public void testPathParamConversion(Class<?> type, String raw, Object expected) {
        Assert.assertEquals(ParamConvertUtils.createPathParamConverter(type).apply(raw), expected);
    }

    @Test(dataProvider = "conversions")
    public void testCookieParamConversion(Class<?> type, String raw, Object expected) {
        Assert.assertEquals(ParamConvertUtils.createCookieParamConverter(type).apply(raw), expected);
    }

    @Test
    public void testUnregisteredTypeFallsBackToString() {
        String raw = "3f2504e0-4f89-11d3-9a0c-0305e82c3301";
        Assert.assertEquals(ParamConvertUtils.createPathParamConverter(UUID.class).apply(raw), raw);
    }
}
