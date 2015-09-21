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

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 */
public class InternalHttpResponderTest {

  @Test
  public void testSendJson() throws IOException {
    InternalHttpResponder responder = new InternalHttpResponder();
    JsonObject output = new JsonObject();
    output.addProperty("data", "this is some data");
    responder.sendJson(HttpResponseStatus.OK, output);

    InternalHttpResponse response = responder.getResponse();
    Assert.assertEquals(HttpResponseStatus.OK.code(), response.getStatusCode());
    JsonObject responseData = new Gson().fromJson(
      new InputStreamReader(response.getInputSupplier().getInput()), JsonObject.class);
    Assert.assertEquals(output, responseData);
  }

  @Test
  public void testSendString() throws IOException {
    InternalHttpResponder responder = new InternalHttpResponder();
    responder.sendString(HttpResponseStatus.BAD_REQUEST, "bad request");

    validateResponse(responder.getResponse(), HttpResponseStatus.BAD_REQUEST, "bad request");
  }

  @Test
  public void testSendStatus() throws IOException {
    InternalHttpResponder responder = new InternalHttpResponder();
    responder.sendStatus(HttpResponseStatus.NOT_FOUND);

    validateResponse(responder.getResponse(), HttpResponseStatus.NOT_FOUND, null);
  }

  @Test
  public void testSendByteArray() throws IOException {
    InternalHttpResponder responder = new InternalHttpResponder();
    responder.sendByteArray(
      HttpResponseStatus.OK, "abc".getBytes(Charsets.UTF_8), HashMultimap.<String, String>create());

    validateResponse(responder.getResponse(), HttpResponseStatus.OK, "abc");
  }

  @Test
  public void testSendError() throws IOException {
    InternalHttpResponder responder = new InternalHttpResponder();
    responder.sendString(HttpResponseStatus.NOT_FOUND, "not found");

    validateResponse(responder.getResponse(), HttpResponseStatus.NOT_FOUND, "not found");
  }

  @Test
  public void testChunks() throws IOException {
    InternalHttpResponder responder = new InternalHttpResponder();
    ChunkResponder chunkResponder = responder.sendChunkStart(HttpResponseStatus.OK, null);
    chunkResponder.sendChunk(Unpooled.wrappedBuffer("a".getBytes(Charsets.UTF_8)));
    chunkResponder.sendChunk(Unpooled.wrappedBuffer("b".getBytes(Charsets.UTF_8)));
    chunkResponder.sendChunk(Unpooled.wrappedBuffer("c".getBytes(Charsets.UTF_8)));
    chunkResponder.close();

    validateResponse(responder.getResponse(), HttpResponseStatus.OK, "abc");
  }

  @Test
  public void testSendContent() throws IOException {
    InternalHttpResponder responder = new InternalHttpResponder();
    responder.sendContent(HttpResponseStatus.OK, Unpooled.wrappedBuffer("abc".getBytes(Charsets.UTF_8)),
                          "contentType", HashMultimap.<String, String>create());

    validateResponse(responder.getResponse(), HttpResponseStatus.OK, "abc");
  }

  private void validateResponse(InternalHttpResponse response, HttpResponseStatus expectedStatus, String expectedData)
    throws IOException {
    int code = response.getStatusCode();
    Assert.assertEquals(expectedStatus.code(), code);
    if (expectedData != null) {
      // read it twice to make sure the input supplier gives the full stream more than once.
      for (int i = 0; i < 2; i++) {
        try (
          BufferedReader reader = new BufferedReader(new InputStreamReader(response.getInputSupplier().getInput()))
        ) {
          String data = reader.readLine();
          Assert.assertEquals(expectedData, data);
        }
      }
    }
  }
}
