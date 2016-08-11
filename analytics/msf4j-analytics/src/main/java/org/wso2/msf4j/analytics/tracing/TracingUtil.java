/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.wso2.msf4j.analytics.tracing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

/**
 * Class to hold constants used in tracing.
 */
class TracingUtil {

    private static final Logger log = LoggerFactory.getLogger(TracingUtil.class);

    static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

    static void pushToDAS(TraceEvent traceEvent, String dasUrl) {
        log.info("Publishing trace event " + traceEvent);
        if (ClientBuilder.newClient().target(dasUrl)
                .request().post(Entity.json(traceEvent)).getStatus() != Response.Status.OK.getStatusCode()) {
            log.warn("Error while publishing trace event " + traceEvent);
        }
    }

}
