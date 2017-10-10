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
package org.wso2.msf4j.formparam;

import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.util.StreamUtil;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class FormParamIteratorTest {

    @Test
    public void testFormParamIterator() throws IOException {
        CarbonMessage carbonMessage = new DefaultCarbonMessage();
        carbonMessage.setHeader("Content-Type", "multipart/form-data; boundary=----abc");
        carbonMessage.addMessageBody(Charset.defaultCharset()
                                            .encode("------abc\r\nContent-Disposition: form-data; " +
                                                    "name=\"name\"\r\n\r\nWSO2\r\n------abc--"));
        carbonMessage.setEndOfMsgAdded(true);
        Request request = new Request(carbonMessage);
        FormParamIterator formParamIterator = new FormParamIterator(request);
        assertTrue(formParamIterator.hasNext());

        FormItem formItem = formParamIterator.next();
        assertTrue(formItem.isFormField());
        assertEquals(formItem.getFieldName(), "name");
        assertEquals(formItem.getContentType(), null);
        assertEquals(StreamUtil.asString(formItem.openStream()), "WSO2");

    }
}
