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
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.reflect.TypeToken;
import com.google.common.util.concurrent.Service;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Test the HttpServer.
 */
public class HttpServerTest {

  private static final Logger LOG = LoggerFactory.getLogger(HttpServerTest.class);

  protected static final Type STRING_MAP_TYPE = new TypeToken<Map<String, String>>() { }.getType();
  protected static final Gson GSON = new Gson();
  protected static final ExceptionHandler EXCEPTION_HANDLER = new ExceptionHandler() {
    @Override
    public void handle(Throwable t, HttpRequest request, HttpResponder responder) {
      if (t instanceof TestHandler.CustomException) {
        responder.sendStatus(TestHandler.CustomException.HTTP_RESPONSE_STATUS);
      } else {
        super.handle(t, request, responder);
      }
    }
  };

  protected static NettyHttpService service;
  protected static URI baseURI;

  protected static NettyHttpService.Builder createBaseNettyHttpServiceBuilder() {
    List<HttpHandler> handlers = Lists.newArrayList();
    handlers.add(new TestHandler());

    NettyHttpService.Builder builder = NettyHttpService.builder();
    builder.addHttpHandlers(handlers);
    builder.setHttpChunkLimit(75 * 1024);
    builder.setExceptionHandler(EXCEPTION_HANDLER);

    builder.modifyChannelPipeline(new Function<ChannelPipeline, ChannelPipeline>() {
      @Override
      public ChannelPipeline apply(ChannelPipeline channelPipeline) {
        channelPipeline.addAfter("compressor", "testhandler", new TestChannelHandler());
        return channelPipeline;
      }
    });
    return builder;
  }

  @ClassRule
  public static TemporaryFolder tmpFolder = new TemporaryFolder();

  @BeforeClass
  public static void setup() throws Exception {
    service = createBaseNettyHttpServiceBuilder().build();
    service.startAndWait();
    Service.State state = service.state();
    Assert.assertEquals(Service.State.RUNNING, state);

    int port = service.getBindAddress().getPort();
    baseURI = URI.create(String.format("http://localhost:%d", port));
  }

  @AfterClass
  public static void teardown() throws Exception {
    service.stopAndWait();
  }

  @Test
  public void testValidEndPoints() throws IOException {
    HttpURLConnection urlConn = request("/test/v1/resource?num=10", HttpMethod.GET);
    Assert.assertEquals(200, urlConn.getResponseCode());
    String content = getContent(urlConn);

    Map<String, String> map = GSON.fromJson(content, STRING_MAP_TYPE);
    Assert.assertEquals(1, map.size());
    Assert.assertEquals("Handled get in resource end-point", map.get("status"));
    urlConn.disconnect();

    urlConn = request("/test/v1/tweets/1", HttpMethod.GET);
    Assert.assertEquals(200, urlConn.getResponseCode());
    content = getContent(urlConn);
    map = GSON.fromJson(content, STRING_MAP_TYPE);
    Assert.assertEquals(1, map.size());
    Assert.assertEquals("Handled get in tweets end-point, id: 1", map.get("status"));
    urlConn.disconnect();
  }


  @Test
  public void testSmallFileUpload() throws IOException {
    testStreamUpload(10);
  }

  @Test
  public void testLargeFileUpload() throws IOException {
    testStreamUpload(30 * 1024 * 1024);
  }


  protected void testStreamUpload(int size) throws IOException {
    //create a random file to be uploaded.
    File fname = tmpFolder.newFile();
    RandomAccessFile randf = new RandomAccessFile(fname, "rw");
    randf.setLength(size);
    randf.close();

    //test stream upload
    HttpURLConnection urlConn = request("/test/v1/stream/upload", HttpMethod.PUT);
    Files.copy(fname, urlConn.getOutputStream());
    Assert.assertEquals(200, urlConn.getResponseCode());
    urlConn.disconnect();
  }

  @Test
  public void testStreamUploadFailure() throws IOException {
    //create a random file to be uploaded.
    int size = 20 * 1024;
    File fname = tmpFolder.newFile();
    RandomAccessFile randf = new RandomAccessFile(fname, "rw");
    randf.setLength(size);
    randf.close();

    HttpURLConnection urlConn = request("/test/v1/stream/upload/fail", HttpMethod.PUT);
    Files.copy(fname, urlConn.getOutputStream());
    Assert.assertEquals(500, urlConn.getResponseCode());
    urlConn.disconnect();
  }

  @Test
  public void testChunkAggregatedUpload() throws IOException {
    //create a random file to be uploaded.
    int size = 69 * 1024;
    File fname = tmpFolder.newFile();
    RandomAccessFile randf = new RandomAccessFile(fname, "rw");
    randf.setLength(size);
    randf.close();

    //test chunked upload
    HttpURLConnection urlConn = request("/test/v1/aggregate/upload", HttpMethod.PUT);
    urlConn.setChunkedStreamingMode(1024);
    Files.copy(fname, urlConn.getOutputStream());
    Assert.assertEquals(200, urlConn.getResponseCode());

    Assert.assertEquals(size, Integer.parseInt(getContent(urlConn).split(":")[1].trim()));
    urlConn.disconnect();
  }

  @Test
  public void testChunkAggregatedUploadFailure() throws IOException {
    //create a random file to be uploaded.
    int size = 78 * 1024;
    File fname = tmpFolder.newFile();
    RandomAccessFile randf = new RandomAccessFile(fname, "rw");
    randf.setLength(size);
    randf.close();

    //test chunked upload
    HttpURLConnection urlConn = request("/test/v1/aggregate/upload", HttpMethod.PUT);
    urlConn.setChunkedStreamingMode(1024);
    Files.copy(fname, urlConn.getOutputStream());
    Assert.assertEquals(500, urlConn.getResponseCode());
    urlConn.disconnect();
  }

  @Test
  public void testPathWithMultipleMethods() throws IOException {
    HttpURLConnection urlConn = request("/test/v1/tweets/1", HttpMethod.GET);
    Assert.assertEquals(200, urlConn.getResponseCode());
    urlConn.disconnect();

    urlConn = request("/test/v1/tweets/1", HttpMethod.PUT);
    writeContent(urlConn, "data");
    Assert.assertEquals(200, urlConn.getResponseCode());
    urlConn.disconnect();
  }


  @Test
  public void testNonExistingEndPoints() throws IOException {
    HttpURLConnection urlConn = request("/test/v1/users", HttpMethod.POST);
    writeContent(urlConn, "data");
    Assert.assertEquals(404, urlConn.getResponseCode());
    urlConn.disconnect();
  }

  @Test
  public void testPutWithData() throws IOException {
    HttpURLConnection urlConn = request("/test/v1/facebook/1/message", HttpMethod.PUT);
    writeContent(urlConn, "Hello, World");
    Assert.assertEquals(200, urlConn.getResponseCode());

    String content = getContent(urlConn);

    Map<String, String> map = GSON.fromJson(content, STRING_MAP_TYPE);
    Assert.assertEquals(1, map.size());
    Assert.assertEquals("Handled put in tweets end-point, id: 1. Content: Hello, World", map.get("result"));
    urlConn.disconnect();
  }

  @Test
  public void testPostWithData() throws IOException {
    HttpURLConnection urlConn = request("/test/v1/facebook/1/message", HttpMethod.POST);
    writeContent(urlConn, "Hello, World");
    Assert.assertEquals(200, urlConn.getResponseCode());

    String content = getContent(urlConn);

    Map<String, String> map = GSON.fromJson(content, STRING_MAP_TYPE);
    Assert.assertEquals(1, map.size());
    Assert.assertEquals("Handled post in tweets end-point, id: 1. Content: Hello, World", map.get("result"));
    urlConn.disconnect();
  }

  @Test
  public void testNonExistingMethods() throws IOException {
    HttpURLConnection urlConn = request("/test/v1/facebook/1/message", HttpMethod.GET);
    Assert.assertEquals(405, urlConn.getResponseCode());
    urlConn.disconnect();
  }

  @Test
  public void testKeepAlive() throws IOException {
    HttpURLConnection urlConn = request("/test/v1/tweets/1", HttpMethod.PUT, true);
    writeContent(urlConn, "data");
    Assert.assertEquals(200, urlConn.getResponseCode());

    Assert.assertEquals("keep-alive", urlConn.getHeaderField(HttpHeaders.Names.CONNECTION));
    urlConn.disconnect();
  }

  @Test
  public void testMultiplePathParameters() throws IOException {
    HttpURLConnection urlConn = request("/test/v1/user/sree/message/12", HttpMethod.GET);
    Assert.assertEquals(200, urlConn.getResponseCode());

    String content = getContent(urlConn);

    Map<String, String> map = GSON.fromJson(content, STRING_MAP_TYPE);
    Assert.assertEquals(1, map.size());
    Assert.assertEquals("Handled multiple path parameters sree 12", map.get("result"));
    urlConn.disconnect();
  }

  //Test the end point where the parameter in path and order of declaration in method signature are different
  @Test
  public void testMultiplePathParametersWithParamterInDifferentOrder() throws IOException {
    HttpURLConnection urlConn = request("/test/v1/message/21/user/sree", HttpMethod.GET);
    Assert.assertEquals(200, urlConn.getResponseCode());

    String content = getContent(urlConn);

    Map<String, String> map = GSON.fromJson(content, STRING_MAP_TYPE);
    Assert.assertEquals(1, map.size());
    Assert.assertEquals("Handled multiple path parameters sree 21", map.get("result"));
    urlConn.disconnect();
  }

  @Test
  public void testNotRoutablePathParamMismatch() throws IOException {
    HttpURLConnection urlConn = request("/test/v1/NotRoutable/sree", HttpMethod.GET);
    Assert.assertEquals(500, urlConn.getResponseCode());
    urlConn.disconnect();
  }

  @Test
  public void testMultiMatchParamPut() throws Exception {
    HttpURLConnection urlConn = request("/test/v1/multi-match/bar", HttpMethod.PUT);
    Assert.assertEquals(405, urlConn.getResponseCode());
    urlConn.disconnect();
  }

  @Test
  public void testHandlerException() throws Exception {
    HttpURLConnection urlConn = request("/test/v1/uexception", HttpMethod.GET);
    Assert.assertEquals(500, urlConn.getResponseCode());
    Assert.assertEquals("Exception encountered while processing request : User Exception",
                        new String(ByteStreams.toByteArray(urlConn.getErrorStream()), Charsets.UTF_8));
    urlConn.disconnect();
  }

  /**
   * Test that the TestChannelHandler that was added using the builder adds the correct header field and value.
   * @throws Exception
   */
  @Test
  public void testChannelPipelineModification() throws Exception {
    HttpURLConnection urlConn = request("/test/v1/tweets/1", HttpMethod.GET);
    Assert.assertEquals(200, urlConn.getResponseCode());
    Assert.assertEquals(urlConn.getHeaderField(TestChannelHandler.HEADER_FIELD), TestChannelHandler.HEADER_VALUE);
  }

  @Test
  public void testMultiMatchFoo() throws Exception {
    testContent("/test/v1/multi-match/foo", "multi-match-get-actual-foo");
  }

  @Test
  public void testMultiMatchAll() throws Exception {
    testContent("/test/v1/multi-match/foo/baz/id", "multi-match-*");
  }

  @Test
  public void testMultiMatchParam() throws Exception {
    testContent("/test/v1/multi-match/bar", "multi-match-param-bar");
  }

  @Test
  public void testMultiMatchParamBar() throws Exception {
    testContent("/test/v1/multi-match/id/bar", "multi-match-param-bar-id");
  }

  @Test
  public void testMultiMatchFooParamBar() throws Exception {
    testContent("/test/v1/multi-match/foo/id/bar", "multi-match-foo-param-bar-id");
  }

  @Test
  public void testMultiMatchFooBarParam() throws Exception {
    testContent("/test/v1/multi-match/foo/bar/id", "multi-match-foo-bar-param-id");
  }

  @Test
  public void testMultiMatchFooBarParamId() throws Exception {
    testContent("/test/v1/multi-match/foo/bar/bar/bar", "multi-match-foo-bar-param-bar-id-bar");
  }

  @Test
  public void testMultiMatchFooPut() throws Exception {
    testContent("/test/v1/multi-match/foo", "multi-match-put-actual-foo", HttpMethod.PUT);
  }

//  @Test
  public void testChunkResponse() throws IOException {
    HttpURLConnection urlConn = request("/test/v1/chunk", HttpMethod.POST);
    try {
      writeContent(urlConn, "Testing message");
      String response = getContent(urlConn);
      Assert.assertEquals("Testing message", response);
    } finally {
      urlConn.disconnect();
    }
  }

  @Test
  public void testStringQueryParam() throws IOException {
    // First send without query, for String type, should get defaulted to null.
    testContent("/test/v1/stringQueryParam/mypath", "mypath:null", HttpMethod.GET);

    // Then send with query, should response with the given name.
    testContent("/test/v1/stringQueryParam/mypath?name=netty", "mypath:netty", HttpMethod.GET);
  }

  @Test
  public void testPrimitiveQueryParam() throws IOException {
    // For primitive type, if missing parameter, should get defaulted to Java primitive default value.
    testContent("/test/v1/primitiveQueryParam", "0", HttpMethod.GET);

    testContent("/test/v1/primitiveQueryParam?age=20", "20", HttpMethod.GET);
  }

  @Test
  public void testSortedSetQueryParam() throws IOException {
    // For collection, if missing parameter, should get defaulted to empty collection
    testContent("/test/v1/sortedSetQueryParam", "", HttpMethod.GET);

    // Try different way of passing the ids, they should end up de-dup and sorted.
    testContent("/test/v1/sortedSetQueryParam?id=30&id=10&id=20&id=30", "10,20,30", HttpMethod.GET);
    testContent("/test/v1/sortedSetQueryParam?id=10&id=30&id=20&id=20", "10,20,30", HttpMethod.GET);
    testContent("/test/v1/sortedSetQueryParam?id=20&id=30&id=20&id=10", "10,20,30", HttpMethod.GET);
  }

  @Test
  public void testListHeaderParam() throws IOException {
    List<String> names = ImmutableList.of("name1", "name3", "name2", "name1");

    HttpURLConnection urlConn = request("/test/v1/listHeaderParam", HttpMethod.GET);
    for (String name : names) {
      urlConn.addRequestProperty("name", name);
    }

    Assert.assertEquals(200, urlConn.getResponseCode());
    Assert.assertEquals(Joiner.on(',').join(names), getContent(urlConn));
    urlConn.disconnect();
  }

  @Test
  public void testDefaultQueryParam() throws IOException {
    // Submit with no parameters. Each should get the default values.
    HttpURLConnection urlConn = request("/test/v1/defaultValue", HttpMethod.GET);
    Assert.assertEquals(200, urlConn.getResponseCode());
    JsonObject json = GSON.fromJson(getContent(urlConn), JsonObject.class);

    Type hobbyType = new TypeToken<List<String>>() { }.getType();

    Assert.assertEquals(30, json.get("age").getAsLong());
    Assert.assertEquals("hello", json.get("name").getAsString());
    Assert.assertEquals(ImmutableList.of("casking"),
                        GSON.fromJson(json.get("hobby").getAsJsonArray(), hobbyType));

    urlConn.disconnect();
  }

  @Test (timeout = 5000)
  public void testConnectionClose() throws Exception {
    URL url = baseURI.resolve("/test/v1/connectionClose").toURL();

    // Fire http request using raw socket so that we can verify the connection get closed by the server
    // after the response.
    Socket socket = createRawSocket(url);
    try {
      PrintStream printer = new PrintStream(socket.getOutputStream(), false, "UTF-8");
      printer.printf("GET %s HTTP/1.1\r\n", url.getPath());
      printer.printf("Host: %s:%d\r\n", url.getHost(), url.getPort());
      printer.print("\r\n");
      printer.flush();

      // Just read everything from the response. Since the server will close the connection, the read loop should
      // end with an EOF. Otherwise there will be timeout of this test case
      String response = CharStreams.toString(new InputStreamReader(socket.getInputStream(), Charsets.UTF_8));
      Assert.assertTrue(response.startsWith("HTTP/1.1 200 OK"));
    } finally {
      socket.close();
    }
  }

  @Test
  public void testUploadReject() throws Exception {
    HttpURLConnection urlConn = request("/test/v1/uploadReject", HttpMethod.POST, true);
    try {
      urlConn.setChunkedStreamingMode(1024);
      urlConn.getOutputStream().write("Rejected Content".getBytes(Charsets.UTF_8));
      try {
        urlConn.getInputStream();
        Assert.fail();
      } catch (IOException e) {
        // Expect to get exception since server response with 400. Just drain the error stream.
        ByteStreams.toByteArray(urlConn.getErrorStream());
        Assert.assertEquals(HttpResponseStatus.BAD_REQUEST.code(), urlConn.getResponseCode());
      }
    } finally {
      urlConn.disconnect();
    }
  }

  @Test
  public void testSleep() throws Exception {
    HttpURLConnection urlConn = request("/test/v1/sleep/10", HttpMethod.GET);
    Assert.assertEquals(200, urlConn.getResponseCode());
    urlConn.disconnect();
  }

  @Test
  public void testWrongMethod() throws IOException {
    HttpURLConnection urlConn = request("/test/v1/customException", HttpMethod.GET);
    Assert.assertEquals(HttpResponseStatus.METHOD_NOT_ALLOWED.code(), urlConn.getResponseCode());
    urlConn.disconnect();
  }

  @Test
  public void testExceptionHandler() throws IOException {
    HttpURLConnection urlConn = request("/test/v1/customException", HttpMethod.POST);
    Assert.assertEquals(TestHandler.CustomException.HTTP_RESPONSE_STATUS.code(), urlConn.getResponseCode());
    urlConn.disconnect();
  }

  protected Socket createRawSocket(URL url) throws IOException {
    return new Socket(url.getHost(), url.getPort());
  }

  protected void testContent(String path, String content) throws IOException {
    testContent(path, content, HttpMethod.GET);
  }

  protected void testContent(String path, String content, HttpMethod method) throws IOException {
    HttpURLConnection urlConn = request(path, method);
    Assert.assertEquals(200, urlConn.getResponseCode());
    Assert.assertEquals(content, getContent(urlConn));
    urlConn.disconnect();
  }

  protected HttpURLConnection request(String path, HttpMethod method) throws IOException {
    return request(path, method, false);
  }

  protected HttpURLConnection request(String path, HttpMethod method, boolean keepAlive) throws IOException {
    URL url = baseURI.resolve(path).toURL();
    HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
    if (method == HttpMethod.POST || method == HttpMethod.PUT) {
      urlConn.setDoOutput(true);
    }
    urlConn.setRequestMethod(method.name());
    if (!keepAlive) {
      urlConn.setRequestProperty(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
    }

    return urlConn;
  }

  protected String getContent(HttpURLConnection urlConn) throws IOException {
    return new String(ByteStreams.toByteArray(urlConn.getInputStream()), Charsets.UTF_8);
  }

  protected void writeContent(HttpURLConnection urlConn, String content) throws IOException {
    urlConn.getOutputStream().write(content.getBytes(Charsets.UTF_8));
  }
}
