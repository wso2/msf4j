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
package org.wso2.msf4j.formparam.util;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Iterator;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

public class FormItemHeaderTest {

    private FormItemHeader formItemHeader;

    @BeforeClass
    public void setup() {
        formItemHeader = new FormItemHeader();
        assertFalse(formItemHeader.getHeaders("Test").hasNext());
        formItemHeader.addHeader("Name", "WSO2");
        formItemHeader.addHeader("Name", "Apache");
        formItemHeader.addHeader("Product", "MSF4J");
    }

    @Test
    public void testGetHeader() {
        assertEquals("WSO2", formItemHeader.getHeader("Name"));
        assertEquals("MSF4J", formItemHeader.getHeader("Product"));
    }

    @Test
    public void testGetHeaders() {
        Iterator<String> names = formItemHeader.getHeaders("Name");
        assertEquals("WSO2", names.next());
        assertEquals("Apache", names.next());

        names = formItemHeader.getHeaders("Product");
        assertEquals("MSF4J", names.next());
    }

    @Test
    public void testGetHeaderNames() {
        Iterator<String> headerName = formItemHeader.getHeaderNames();
        assertEquals("name", headerName.next());
        assertEquals("product", headerName.next());
        assertFalse(headerName.hasNext());
    }
}
