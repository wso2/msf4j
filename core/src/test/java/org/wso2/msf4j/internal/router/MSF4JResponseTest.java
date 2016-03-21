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

package org.wso2.msf4j.internal.router;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import javax.ws.rs.core.Response;

/**
 * Test MSF4JResponse and MSF4J ResponseBuilder.
 */
public class MSF4JResponseTest {

    @Test
    public void testStatusOk() {
        Response response = Response
                .status(Response.Status.OK.getStatusCode())
                .build();
        Assert.assertTrue(response.getStatus() == Response.Status.OK.getStatusCode());
    }

    @Test
    public void testStatusNotFound() {
        Response response = Response
                .status(Response.Status.NOT_FOUND.getStatusCode())
                .build();
        Assert.assertTrue(response.getStatus() == Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testEntity() {
        Response response = Response
                .status(Response.Status.OK.getStatusCode())
                .entity("Entity")
                .build();
        Assert.assertTrue(response.getStatus() == Response.Status.OK.getStatusCode());
        Assert.assertEquals(response.getEntity(), "Entity");
    }

    @Test
    public void testSingleHeaderSingleVal() {
        Response response = Response
                .status(Response.Status.OK.getStatusCode())
                .header("key1", "val1")
                .build();
        Assert.assertEquals("val1", response.getStringHeaders().getFirst("key1"));
    }

    @Test
    public void testMultipleHeaderSingleVal() {
        Response response = Response
                .status(Response.Status.OK.getStatusCode())
                .header("key1", "val1")
                .header("key2", "val2")
                .build();
        Assert.assertEquals("val1", response.getStringHeaders().getFirst("key1"));
        Assert.assertEquals("val2", response.getStringHeaders().getFirst("key2"));
    }

    @Test
    public void testSingleHeaderRepeatedSingleVal() {
        Response response = Response
                .status(Response.Status.OK.getStatusCode())
                .header("key1", "val1")
                .header("key1", "val2")
                .build();
        Assert.assertEquals("val1", response.getStringHeaders().get("key1").get(0));
        Assert.assertEquals("val2", response.getStringHeaders().get("key1").get(1));
    }

    @Test
    public void testSingleHeaderListVal() {
        Response response = Response
                .status(Response.Status.OK.getStatusCode())
                .header("key1", Arrays.asList(new String[]{"val1", "val2"}))
                .build();
        Assert.assertEquals("val1", response.getStringHeaders().get("key1").get(0));
        Assert.assertEquals("val2", response.getStringHeaders().get("key1").get(1));
    }

}
