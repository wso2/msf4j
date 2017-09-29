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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static org.testng.AssertJUnit.assertEquals;

public class MimeUtilityTest {

    @Test
    public void testDecodeText() throws IOException {
        String sampleText = "abc";
        assertEquals(sampleText, MimeUtility.decodeText(sampleText));

        sampleText = "If you can read this you understand the example.";
        String decodedText = MimeUtility.decodeText("=?ISO-8859-1?B?SWYgeW91IGNhbiByZWFkIHRoaXMgeW8=?= " +
                                                    "=?ISO-8859-2?B?dSB1bmRlcnN0YW5kIHRoZSBleGFtcGxlLg==?=\"\r\n");
        assertEquals(sampleText, decodedText);
    }

    @Test(expectedExceptions = UnsupportedEncodingException.class)
    public void decodeInvalidEncoding() throws Exception {
        MimeUtility.decodeText("=?invalid?B?xyz-?=");
    }

}
