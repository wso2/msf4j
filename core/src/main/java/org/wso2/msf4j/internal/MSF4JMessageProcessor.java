/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.msf4j.internal;

import com.google.common.net.HttpHeaders;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.CarbonMessageProcessor;
import org.wso2.carbon.messaging.Constants;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.carbon.messaging.TransportSender;
import org.wso2.msf4j.internal.router.MicroserviceMetadata;

/**
 * Process carbon messages for MSF4J.
 */
public class MSF4JMessageProcessor implements CarbonMessageProcessor {

    private static String MSF4J_MSG_PROC_ID = "MSF4J-CM-PROCESSOR";
    private MicroservicesRegistry microservicesRegistry;

    public MSF4JMessageProcessor(MicroservicesRegistry microservicesRegistry) {
        this.microservicesRegistry = microservicesRegistry;
    }

    @Override
    public boolean receive(CarbonMessage carbonMessage, CarbonCallback carbonCallback) throws Exception {
        String url = (String) carbonMessage.getProperty(Constants.TO);
        MicroserviceMetadata microserviceMetadata = microservicesRegistry.getHttpResourceHandler();

        DefaultCarbonMessage cMsg = new DefaultCarbonMessage();
        cMsg.setStringMessageBody(url);
        //cMsg.setHeader(HttpHeaders.TRANSFER_ENCODING, io.netty.handler.codec.http.HttpHeaders.Values.CHUNKED);
        cMsg.setHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(url.length()));
        carbonCallback.done(cMsg);
        return true;
    }

    @Override
    public void setTransportSender(TransportSender transportSender) {

    }

    @Override
    public String getId() {
        return MSF4J_MSG_PROC_ID;
    }
}
