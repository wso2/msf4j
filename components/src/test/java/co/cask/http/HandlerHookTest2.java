/*
 * Copyright © 2014 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.http;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Service;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * Tests handler hooks.
 */
public class HandlerHookTest2 extends BaseHandlerHookTest {
  private static final Logger LOG = LoggerFactory.getLogger(HandlerHookTest2.class);

  private static String hostname = "127.0.0.1";
  private static NettyHttpService service;
  private static final TestHandlerHook handlerHook1 = new TestHandlerHook();
  private static final TestHandlerHook handlerHook2 = new TestHandlerHook();

  @BeforeClass
  public static void setup() throws Exception {

    NettyHttpService.Builder builder = NettyHttpService.builder();
    builder.addHttpHandlers(ImmutableList.of(new TestHandler()));
    builder.setHandlerHooks(ImmutableList.of(handlerHook1, handlerHook2));
    builder.setHost(hostname);

    service = builder.build();
    service.startAndWait();
    Service.State state = service.state();
    Assert.assertEquals(Service.State.RUNNING, state);

    int port = service.getBindAddress().getPort();
    baseURI = URI.create(String.format("http://%s:%d", hostname, port));
  }

  @Before
  public void reset() {
    handlerHook1.reset();
    handlerHook2.reset();
  }

  @Test
  public void testPreException() throws Exception {
    int status = doGet("/test/v1/resource", "X-Request-Type", "PreException");
    Assert.assertEquals(HttpResponseStatus.INTERNAL_SERVER_ERROR.code(), status);

    // Wait for any post handlers to be called
    TimeUnit.MILLISECONDS.sleep(100);
    Assert.assertEquals(1, handlerHook1.getNumPreCalls());

    // The second pre-call should not have happened due to exception in the first pre-call
    // None of the post calls should have happened.
    Assert.assertEquals(0, handlerHook1.getNumPostCalls());
    Assert.assertEquals(0, handlerHook2.getNumPreCalls());
    Assert.assertEquals(0, handlerHook2.getNumPostCalls());
  }

  @Test
  public void testPostException() throws Exception {
    int status = doGet("/test/v1/resource", "X-Request-Type", "PostException");
    Assert.assertEquals(HttpResponseStatus.OK.code(), status);

    Assert.assertEquals(1, handlerHook1.getNumPreCalls());
    Assert.assertEquals(1, handlerHook1.getNumPostCalls());

    Assert.assertEquals(1, handlerHook2.getNumPreCalls());
    Assert.assertEquals(1, handlerHook2.getNumPostCalls());
  }

  @Test
  public void testUnknownPath() throws Exception {
    int status = doGet("/unknown/path/test/v1/resource");
    Assert.assertEquals(HttpResponseStatus.NOT_FOUND.code(), status);

    // Wait for any post handlers to be called
    TimeUnit.MILLISECONDS.sleep(100);
    Assert.assertEquals(0, handlerHook1.getNumPreCalls());
    Assert.assertEquals(0, handlerHook1.getNumPostCalls());

    Assert.assertEquals(0, handlerHook2.getNumPreCalls());
    Assert.assertEquals(0, handlerHook2.getNumPostCalls());
  }

  @AfterClass
  public static void teardown() throws Exception {
    service.stopAndWait();
  }
}
