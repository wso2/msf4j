/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.msf4j.util;

import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Tests for utility class.
 */
public class UtilsTest {

    @Test
    public void testNormalizePath() {
        assertEquals("hello", Utils.normalizePath("hello"));
        assertEquals("hello", Utils.normalizePath("/hello"));
        assertEquals("hello", Utils.normalizePath("hello/"));
        assertEquals("hello", Utils.normalizePath("/hello/"));
        assertEquals("hello", Utils.normalizePath("//hello"));
        assertEquals("hello", Utils.normalizePath("///hello"));
        assertEquals("hello", Utils.normalizePath("///hello///"));

        assertEquals("hel/lo", Utils.normalizePath("hel/lo"));
        assertEquals("hel/lo", Utils.normalizePath("hel//lo"));
        assertEquals("hel/lo", Utils.normalizePath("hel///lo"));
        assertEquals("hel/lo", Utils.normalizePath("//hel//lo//"));

        assertEquals("h/e/ll/o", Utils.normalizePath("h/e/ll/o"));
        assertEquals("h/e/ll/o", Utils.normalizePath("h//e//ll//o"));
        assertEquals("h/e/ll/o", Utils.normalizePath("//h//e//ll//o//"));

        assertEquals("", Utils.normalizePath(""));
        assertEquals("", Utils.normalizePath("/"));
        assertEquals("", Utils.normalizePath("//"));
        assertEquals("", Utils.normalizePath("///"));
        assertEquals("h", Utils.normalizePath("h"));
        assertEquals("h", Utils.normalizePath("/h"));
        assertEquals("h", Utils.normalizePath("h/"));
    }
}

