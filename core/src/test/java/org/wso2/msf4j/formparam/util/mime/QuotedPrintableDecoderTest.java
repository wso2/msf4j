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
package org.wso2.msf4j.formparam.util.mime;

import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

public class QuotedPrintableDecoderTest {
    private static final String US_ASCII_CHARSET = "US-ASCII";

    @Test
    public void emptyDecode() throws Exception {
        assertEquals("", "");
    }

    @Test
    public void plainDecode() throws Exception {
        assertEncoded("The quick brown fox jumps over the lazy dog.", "The quick brown fox jumps over the lazy dog.");
    }

    @Test
    public void basicEncodeDecode() throws Exception {
        assertEncoded("= Hello there =\r\n", "=3D Hello there =3D=0D=0A");
    }

    private static void assertEncoded(String clearText, String encoded) throws Exception {
        byte[] expected = clearText.getBytes(US_ASCII_CHARSET);

        ByteArrayOutputStream out = new ByteArrayOutputStream(encoded.length());
        byte[] encodedData = encoded.getBytes(US_ASCII_CHARSET);
        QuotedPrintableDecoder.decode(encodedData, out);
        byte[] actual = out.toByteArray();

        assertArrayEquals(expected, actual);
    }
}
