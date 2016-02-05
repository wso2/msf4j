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

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.internal.router.beanconversion.BeanConversionException;
import org.wso2.msf4j.internal.router.beanconversion.BeanConverter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.ws.rs.core.MediaType;

/**
 * Test the HttpServer.
 */
public class HttpServerTest {

    protected static final Type STRING_MAP_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();
    protected static final Gson GSON = new Gson();

    @ClassRule
    public static TemporaryFolder tmpFolder = new TemporaryFolder();

    private static final TestHandler testHandler = new TestHandler();

    private static String hostname = Constants.HOSTNAME;
    private static final int port = Constants.PORT + 1;
    protected static URI baseURI;

    private static final MicroservicesRunner microservicesRunner = new MicroservicesRunner(port);

    @BeforeClass
    public static void setup() throws Exception {
        baseURI = URI.create(String.format("http://%s:%d", hostname, port));
        microservicesRunner
                .deploy(testHandler)
                .start();
    }

    @AfterClass
    public static void teardown() throws Exception {
        microservicesRunner.stop();
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
        testStreamUpload(1000000);
    }

    protected void testStreamUpload(int size) throws IOException {
        //create a random file to be uploaded.
        File fname = tmpFolder.newFile();
        RandomAccessFile randf = new RandomAccessFile(fname, "rw");
        String contentStr = IntStream.range(0, size)
                .mapToObj(value -> String.valueOf((int) (Math.random() * 1000)))
                .collect(Collectors.joining(""));
        randf.write(contentStr.getBytes(Charsets.UTF_8));
        randf.close();

        //test stream upload
        HttpURLConnection urlConn = request("/test/v1/stream/upload", HttpMethod.PUT);
        Files.copy(fname, urlConn.getOutputStream());
        Assert.assertEquals(200, urlConn.getResponseCode());
        String contentFromServer = getContent(urlConn);
        Assert.assertEquals(contentStr, contentFromServer);
        urlConn.disconnect();
    }

    //    @Test
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

    //    @Test
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
     *
     * @throws Exception
     */
    /*@Test
    public void testChannelPipelineModification() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/tweets/1", HttpMethod.GET);
        Assert.assertEquals(200, urlConn.getResponseCode());
        Assert.assertEquals(urlConn.getHeaderField(TestChannelHandler.HEADER_FIELD), TestChannelHandler.HEADER_VALUE);
    }*/
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

    //@Test
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
    public void testHeaderResponse() throws IOException {
        HttpURLConnection urlConn = request("/test/v1/headerResponse", HttpMethod.GET);
        urlConn.addRequestProperty("name", "name1");

        Assert.assertEquals(200, urlConn.getResponseCode());
        Assert.assertEquals("name1", urlConn.getHeaderField("name"));
        urlConn.disconnect();
    }

    @Test
    public void testDefaultQueryParam() throws IOException {
        // Submit with no parameters. Each should get the default values.
        HttpURLConnection urlConn = request("/test/v1/defaultValue", HttpMethod.GET);
        Assert.assertEquals(200, urlConn.getResponseCode());
        JsonObject json = GSON.fromJson(getContent(urlConn), JsonObject.class);

        Type hobbyType = new TypeToken<List<String>>() {
        }.getType();

        Assert.assertEquals(30, json.get("age").getAsLong());
        Assert.assertEquals("hello", json.get("name").getAsString());
        Assert.assertEquals(ImmutableList.of("casking"),
                GSON.fromJson(json.get("hobby").getAsJsonArray(), hobbyType));

        urlConn.disconnect();
    }

    @Test(timeout = 5000)
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
    public void testNoPathGetMethod() throws Exception {
        HttpURLConnection urlConn = request("/test/v1", HttpMethod.GET);
        Assert.assertEquals("no-@Path-GET", getContent(urlConn));
        urlConn.disconnect();
    }

    @Test
    public void testNoPathPostMethod() throws Exception {
        HttpURLConnection urlConn = request("/test/v1", HttpMethod.POST);
        Assert.assertEquals("no-@Path-POST", getContent(urlConn));
        urlConn.disconnect();
    }

    @Test
    public void testNoPathPutMethod() throws Exception {
        HttpURLConnection urlConn = request("/test/v1", HttpMethod.PUT);
        Assert.assertEquals("no-@Path-PUT", getContent(urlConn));
        urlConn.disconnect();
    }

    @Test
    public void testNoPathDeleteMethod() throws Exception {
        HttpURLConnection urlConn = request("/test/v1", HttpMethod.DELETE);
        Assert.assertEquals("no-@Path-DELETE", getContent(urlConn));
        urlConn.disconnect();
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

    @Test
    public void testConsumeJsonProduceString() throws IOException {
        HttpURLConnection urlConn = request("/test/v1/jsonConsumeStringProduce", HttpMethod.POST);
        urlConn.setRequestProperty(HttpHeaders.Names.CONTENT_TYPE, "text/json");
        Gson gson = new Gson();
        Pet pet = petInstance();
        writeContent(urlConn, gson.toJson(pet));
        Assert.assertEquals(HttpResponseStatus.OK.code(), urlConn.getResponseCode());
        Assert.assertEquals(pet.getDetails(), getContent(urlConn));
        urlConn.disconnect();
    }

    @Test
    public void testConsumeStringProduceJson() throws IOException {
        HttpURLConnection urlConn = request("/test/v1/textConsumeJsonProduce", HttpMethod.POST);
        urlConn.setRequestProperty(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
        String str = "send-something";
        writeContent(urlConn, str);
        Assert.assertEquals(HttpResponseStatus.OK.code(), urlConn.getResponseCode());
        Gson gson = new Gson();
        String content = getContent(urlConn);
        TextBean textBean = gson.fromJson(content, TextBean.class);
        Assert.assertEquals(str, textBean.getText());
        urlConn.disconnect();
    }

    @Test
    public void testConsumeStringProduceString() throws IOException {
        HttpURLConnection urlConn = request("/test/v1/textConsumeTextProduce", HttpMethod.POST);
        urlConn.setRequestProperty(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
        String str = "send-something";
        writeContent(urlConn, str);
        Assert.assertEquals(HttpResponseStatus.OK.code(), urlConn.getResponseCode());
        Assert.assertEquals(str + "-processed", getContent(urlConn));
        urlConn.disconnect();
    }

    @Test
    public void testConsumeXmlProduceXml() throws IOException, BeanConversionException {
        HttpURLConnection urlConn = request("/test/v1/textConsumeTextProduceXml", HttpMethod.POST);
        urlConn.setRequestProperty(HttpHeaders.Names.CONTENT_TYPE, "text/xml");
        XmlBean xmlBean = new XmlBean();
        xmlBean.setName("send-something");
        xmlBean.setId(10);
        xmlBean.setValue(15);
        writeContent(urlConn, (String) BeanConverter.instance("text/xml").toMedia(xmlBean));
        Assert.assertEquals(HttpResponseStatus.OK.code(), urlConn.getResponseCode());
        String respBody = getContent(urlConn);
        XmlBean xmlBean2 = (XmlBean) BeanConverter.instance("text/xml").toObject((String) respBody, XmlBean.class);
        Assert.assertEquals(xmlBean.getName(), xmlBean2.getName());
        Assert.assertEquals(xmlBean.getId(), xmlBean2.getId());
        Assert.assertEquals(xmlBean.getValue(), xmlBean2.getValue());
        urlConn.disconnect();
    }

    @Test
    public void testDownloadPngFile() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/fileserver/png", HttpMethod.GET);
        Assert.assertEquals(HttpResponseStatus.OK.code(), urlConn.getResponseCode());
        String contentType = urlConn.getHeaderField(HttpHeaders.Names.CONTENT_TYPE);
        Assert.assertTrue(contentType.equalsIgnoreCase("image/png"));
        InputStream downStream = urlConn.getInputStream();
        File file = new File(Resources.getResource("testPngFile.png").toURI());
        Assert.assertTrue(isStreamEqual(downStream, new FileInputStream(file)));
    }

    @Test
    public void testDownloadJpgFile() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/fileserver/jpg", HttpMethod.GET);
        Assert.assertEquals(HttpResponseStatus.OK.code(), urlConn.getResponseCode());
        String contentType = urlConn.getHeaderField(HttpHeaders.Names.CONTENT_TYPE);
        Assert.assertTrue(contentType.equalsIgnoreCase("image/jpeg"));
        InputStream downStream = urlConn.getInputStream();
        File file = new File(Resources.getResource("testJpgFile.jpg").toURI());
        Assert.assertTrue(isStreamEqual(downStream, new FileInputStream(file)));
    }

    @Test
    public void testDownloadTxtFile() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/fileserver/txt", HttpMethod.GET);
        Assert.assertEquals(HttpResponseStatus.OK.code(), urlConn.getResponseCode());
        String contentType = urlConn.getHeaderField(HttpHeaders.Names.CONTENT_TYPE);
        Assert.assertTrue(contentType.equalsIgnoreCase("text/plain"));
        InputStream downStream = urlConn.getInputStream();
        File file = new File(Resources.getResource("testTxtFile.txt").toURI());
        Assert.assertTrue(isStreamEqual(downStream, new FileInputStream(file)));
    }

    @Test
    public void testGzipCompressionWithNoGzipAccept() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/gzipfile", HttpMethod.GET);
        Assert.assertEquals(HttpResponseStatus.OK.code(), urlConn.getResponseCode());
        String contentEncoding = urlConn.getHeaderField(HttpHeaders.Names.CONTENT_ENCODING);
        Assert.assertTrue(contentEncoding == null || !contentEncoding.contains("gzip"));
        InputStream downStream = urlConn.getInputStream();
        Assert.assertTrue(IOUtils.toByteArray(downStream).length ==
                IOUtils.toByteArray(Resources.getResource("testJpgFile.jpg").openStream()).length);
    }

    @Test
    public void testGzipCompressionWithGzipAccept() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/gzipfile", HttpMethod.GET);
        urlConn.addRequestProperty(HttpHeaders.Names.ACCEPT_ENCODING, "gzip");
        Assert.assertEquals(HttpResponseStatus.OK.code(), urlConn.getResponseCode());
        String contentEncoding = urlConn.getHeaderField(HttpHeaders.Names.CONTENT_ENCODING);
        Assert.assertTrue("gzip".equalsIgnoreCase(contentEncoding));
        InputStream downStream = urlConn.getInputStream();
        Assert.assertTrue(IOUtils.toByteArray(downStream).length <
                IOUtils.toByteArray(Resources.getResource("testJpgFile.jpg").openStream()).length);
    }

    @Test
    public void testContentTypeSetting0() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/response/typehtml", HttpMethod.GET);
        Assert.assertEquals(HttpResponseStatus.OK.code(), urlConn.getResponseCode());
        String contentType = urlConn.getHeaderField(HttpHeaders.Names.CONTENT_TYPE);
        Assert.assertTrue(contentType.equalsIgnoreCase(MediaType.TEXT_HTML));
        String content = getContent(urlConn);
        Assert.assertEquals("Hello", content);
        urlConn.disconnect();
    }

    @Test
    public void testContentTypeSetting1() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/response/typehtml/str", HttpMethod.GET);
        Assert.assertEquals(HttpResponseStatus.OK.code(), urlConn.getResponseCode());
        String contentType = urlConn.getHeaderField(HttpHeaders.Names.CONTENT_TYPE);
        Assert.assertTrue(contentType.equalsIgnoreCase(MediaType.TEXT_HTML));
        String content = getContent(urlConn);
        Assert.assertEquals("Hello", content);
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

    protected boolean isStreamEqual(InputStream input1, InputStream input2) throws IOException {
        if (!(input1 instanceof BufferedInputStream)) {
            input1 = new BufferedInputStream(input1);
        }
        if (!(input2 instanceof BufferedInputStream)) {
            input2 = new BufferedInputStream(input2);
        }
        int ch = input1.read();
        while (-1 != ch) {
            int ch2 = input2.read();
            if (ch != ch2) {
                return false;
            }
            ch = input1.read();
        }
        int ch2 = input2.read();
        return (ch2 == -1);
    }

    protected Pet petInstance() {
        Pet pet = new Pet();
        pet.setCategory(new Category("dog"));
        pet.setAgeMonths(3);
        pet.setDetails("small-cat");
        pet.setPrice(10.5f);
        pet.setImage("cat.png");
        return pet;
    }
}
