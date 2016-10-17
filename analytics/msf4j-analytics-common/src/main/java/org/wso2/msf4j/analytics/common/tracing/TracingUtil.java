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

package org.wso2.msf4j.analytics.common.tracing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

/**
 * Utility methods of for MSF4J tracing.
 */
public class TracingUtil {

    private static final Logger log = LoggerFactory.getLogger(TracingUtil.class);
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final Random random = new Random();

    /**
     * Generate a random string unique identifier.
     */
    public static String generateUniqueId() {
        // UUID.randomUUID().toString() is too slow
        // TODO: Test whether the ID is unique enough
        return System.currentTimeMillis() + "" + String.format("%08d", random.nextInt(100000000));
    }

    /**
     * Publish trace event to DAS in the background.
     */
    public static void pushToDAS(TraceEvent traceEvent, String dasUrl) {
        Future<?> future = executorService.submit(() -> {
            log.debug("Publishing trace event " + traceEvent);
            if (ClientBuilder.newClient().target(dasUrl)
                    .request().post(Entity.json(traceEvent)).getStatus() != Response.Status.OK.getStatusCode()) {
                log.error("Error while publishing trace event " + traceEvent);
            }
        });
        future.isDone(); // Added to avoid findbugs warning
    }

}
