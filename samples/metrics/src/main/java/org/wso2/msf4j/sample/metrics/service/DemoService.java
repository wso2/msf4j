/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.msf4j.sample.metrics.service;

import org.wso2.carbon.metrics.core.annotation.Counted;
import org.wso2.carbon.metrics.core.annotation.Metered;
import org.wso2.carbon.metrics.core.annotation.Timed;

import java.util.Random;
import java.util.concurrent.atomic.LongAdder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 * Demonstrating the use of Metrics Annotations.
 */
@Path("/demo")
public class DemoService {

    private final Random random = new Random();

    private final LongAdder longAdder = new LongAdder();

    @GET
    @Path("/rand/{bound}")
    @Metered
    public int getRandomInt(@PathParam("bound") int bound) {
        return random.nextInt(bound);
    }

    @GET
    @Path("/echo/{string}")
    @Timed
    public String echo(@PathParam("string") String string) {
        try {
            Thread.sleep(random.nextInt(5000));
        } catch (InterruptedException e) {
        }
        return string;
    }

    @GET
    @Path("/count/{number}")
    @Counted(monotonic = true)
    public long getCount(@PathParam("number") long number) {
        longAdder.add(number);
        return longAdder.longValue();
    }

}
