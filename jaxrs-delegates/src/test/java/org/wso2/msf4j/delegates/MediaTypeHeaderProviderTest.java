/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.msf4j.delegates;

import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

public class MediaTypeHeaderProviderTest extends Assert {

    @Test(expected = IllegalArgumentException.class)
    public void testNullValue() throws Exception {
        MediaType.valueOf(null);
    }

    @Test
    public void testTypeWithExtendedParameters() {
        MediaType mt = MediaType.valueOf("multipart/related;type=application/dicom+xml");

        assertEquals("multipart", mt.getType());
        assertEquals("related", mt.getSubtype());
        ;
    }

    @Test
    public void testTypeWithExtendedParametersQuote() {
        MediaType mt = MediaType.valueOf("multipart/related;type=\"application/dicom+xml\"");

        assertEquals("multipart", mt.getType());
        assertEquals("related", mt.getSubtype());
    }

    @Test
    public void testTypeWithExtendedAndBoundaryParameter() {
        MediaType mt = MediaType.valueOf(
                "multipart/related; type=application/dicom+xml; " +
                        "boundary=\"uuid:b9aecb2a-ab37-48d6-a1cd-b2f4f7fa63cb\"");
        assertEquals("multipart", mt.getType());
        assertEquals("related", mt.getSubtype());
    }

    @Test
    public void testSimpleType() {
        MediaType m = MediaType.valueOf("text/html");
        assertEquals("Media type was not parsed correctly",
                m, new MediaType("text", "html"));
        assertEquals("Media type was not parsed correctly",
                MediaType.valueOf("text/html "), new MediaType("text", "html"));
    }

    @Test
    public void testBadType() {
        try {
            new MediaTypeHeaderProvider().fromString("texthtml");
            fail("Parse exception must've been thrown");
        } catch (Exception pe) {
            // expected
        }

    }

    @Test
    public void testTypeWithParameters() {
        MediaType mt = MediaType.valueOf("text/html;q=1234;b=4321");

        assertEquals("text", mt.getType());
        assertEquals("html", mt.getSubtype());
    }

    @Test
    public void testSimpleToString() {
        MediaTypeHeaderProvider provider =
                new MediaTypeHeaderProvider();

        assertEquals("simple media type is not serialized", "text/plain",
                provider.toString(new MediaType("text", "plain")));
    }

}
