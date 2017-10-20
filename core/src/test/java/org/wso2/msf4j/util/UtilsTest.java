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

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Tests for utility class.
 */
public class UtilsTest {

    @DataProvider(name = "paths")
    public String[][] paths() {
        return new String[][] {
            // simple paths with or without '/'
            {"path", "path"},
            {"/path", "path"},
            {"path/", "path"},
            {"/path/", "path"},
            {"//path", "path"},
            {"///path", "path"},
            {"///path///", "path"},

            // paths with multiple components
            {"path1/path2", "path1/path2"},
            {"path1//path2", "path1/path2"},
            {"path1///path2", "path1/path2"},
            {"//path1//path2//", "path1/path2"},
            {"path1/path2/path3/path4", "path1/path2/path3/path4"},
            {"path1//path2//path3//path4", "path1/path2/path3/path4"},
            {"//path1//path2//path3//path4//", "path1/path2/path3/path4"},

            // edge cases: empty path
            {"", ""},

            // edge cases: paths consisting only of '/'
            {"/", ""},
            {"//", ""},
            {"///", ""},

            // edge cases: paths consisting only of a single character
            {"p", "p"},
            {"/p", "p"},
            {"p/", "p"}
        };
    }

    @Test(dataProvider = "paths")
    public void testNormalizePath(String path, String expectedPath) {
        assertEquals(expectedPath, Utils.normalizePath(path));
    }
}

