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

package org.wso2.msf4j;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.msf4j.beanconversion.BeanConversionException;
import org.wso2.msf4j.conf.Constants;
import org.wso2.msf4j.exception.TestExceptionMapper;
import org.wso2.msf4j.exception.TestExceptionMapper2;
import org.wso2.msf4j.formparam.util.StreamUtil;
import org.wso2.msf4j.internal.beanconversion.BeanConverter;
import org.wso2.msf4j.pojo.Category;
import org.wso2.msf4j.pojo.Pet;
import org.wso2.msf4j.pojo.TextBean;
import org.wso2.msf4j.pojo.XmlBean;
import org.wso2.msf4j.service.SecondService;
import org.wso2.msf4j.service.TestMicroServiceWithDynamicPath;
import org.wso2.msf4j.service.TestMicroservice;
import org.wso2.msf4j.service.sub.Player;
import org.wso2.msf4j.service.sub.Team;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

/**
 * Test the HttpServer.
 */
public class HttpServerTest {
    public static final String HEADER_KEY_CONNECTION = "CONNECTION";
    public static final String HEADER_VAL_CLOSE = "CLOSE";
    protected static final Type STRING_MAP_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();
    protected static final Gson GSON = new Gson();

    public static File tmpFolder;

    static {
        try {
            tmpFolder = Files.createTempDirectory("msf4j").toFile();
        } catch (IOException e) {
            throw new RuntimeException("Error while creating tenp directory", e);
        }
    }
    private final TestMicroservice testMicroservice = new TestMicroservice();
    private final SecondService secondService = new SecondService();

    private static final int port = Constants.PORT + 1;
    protected static URI baseURI;

    private MicroservicesRunner microservicesRunner;
    private MicroservicesRunner secondMicroservicesRunner;

    @BeforeClass
    public void setup() throws Exception {
        baseURI = URI.create(String.format("http://%s:%d", Constants.HOSTNAME, port));
        microservicesRunner = new MicroservicesRunner(port);
        microservicesRunner
                .addExceptionMapper(new TestExceptionMapper(), new TestExceptionMapper2())
                .deploy(testMicroservice)
                .start();
        microservicesRunner.deploy("/DynamicPath", new TestMicroServiceWithDynamicPath());

        secondMicroservicesRunner = new MicroservicesRunner(port + 1);
        secondMicroservicesRunner.deploy(secondService).start();
    }

    @AfterClass
    public void teardown() throws Exception {
        microservicesRunner.stop();
        secondMicroservicesRunner.stop();
    }

    @Test
    public void testMultipleMicroServiceRunners() throws IOException {
        HttpURLConnection urlConn =
                request("/SecondService/addNumbers/9/25", HttpMethod.GET, false, baseURI.getPort() + 1);
        assertEquals(200, urlConn.getResponseCode());
        String content = getContent(urlConn);

        assertEquals(34, Integer.parseInt(content));
        urlConn.disconnect();
    }

    @Test
    public void testDynamicMicroserviceRegistration() throws IOException {
        HttpURLConnection urlConn = request("/DynamicPath/hello/MSF4J", HttpMethod.GET);
        assertEquals(200, urlConn.getResponseCode());
        String content = getContent(urlConn);
        assertEquals("Hello MSF4J", content);
        urlConn.disconnect();
    }

    @Test
    public void testValidEndPoints() throws IOException {
        HttpURLConnection urlConn = request("/test/v1/resource?num=10", HttpMethod.GET);
        assertEquals(200, urlConn.getResponseCode());
        String content = getContent(urlConn);

        Map<String, String> map = GSON.fromJson(content, STRING_MAP_TYPE);
        assertEquals(1, map.size());
        assertEquals("Handled get in resource end-point", map.get("status"));
        urlConn.disconnect();

        urlConn = request("/test/v1/tweets/1", HttpMethod.GET);
        assertEquals(200, urlConn.getResponseCode());
        content = getContent(urlConn);
        map = GSON.fromJson(content, STRING_MAP_TYPE);
        assertEquals(1, map.size());
        assertEquals("Handled get in tweets end-point, id: 1", map.get("status"));
        urlConn.disconnect();
    }

    @Test
    public void testSmallFileUpload() throws IOException {
        testStreamUpload(10, "testSmallFileUpload.txt");
    }

    @Test
    public void testLargeFileUpload() throws IOException {
        testStreamUpload(1000000, "testLargeFileUpload.txt");
    }

    protected void testStreamUpload(int size, String filename) throws IOException {
        //create a random file to be uploaded.
        File fname = new File(tmpFolder, filename);
        fname.createNewFile();
        RandomAccessFile randf = new RandomAccessFile(fname, "rw");
        String contentStr = IntStream.range(0, size)
                .mapToObj(value -> String.valueOf((int) (Math.random() * 1000)))
                .collect(Collectors.joining(""));
        randf.write(contentStr.getBytes(Charsets.UTF_8));
        randf.close();

        //test stream upload
        HttpURLConnection urlConn = request("/test/v1/stream/upload", HttpMethod.PUT);
        Files.copy(Paths.get(fname.toURI()), urlConn.getOutputStream());
        assertEquals(200, urlConn.getResponseCode());
        String contentFromServer = getContent(urlConn);
        assertEquals(contentStr, contentFromServer);
        urlConn.disconnect();
        fname.delete();
    }

    //    @Test
    public void testStreamUploadFailure() throws IOException {
        //create a random file to be uploaded.
        int size = 20 * 1024;
        File fname = new File(tmpFolder, "testStreamUploadFailure.txt");
        fname.createNewFile();
        RandomAccessFile randf = new RandomAccessFile(fname, "rw");
        randf.setLength(size);
        randf.close();

        HttpURLConnection urlConn = request("/test/v1/stream/upload/fail", HttpMethod.PUT);
        Files.copy(Paths.get(fname.toURI()), urlConn.getOutputStream());
        assertEquals(500, urlConn.getResponseCode());
        urlConn.disconnect();
        fname.delete();
    }

    @Test
    public void testChunkAggregatedUpload() throws IOException {
        //create a random file to be uploaded.
        int size = 69 * 1024;
        File fname = new File(tmpFolder, "testChunkAggregatedUpload.txt");
        fname.createNewFile();
        RandomAccessFile randf = new RandomAccessFile(fname, "rw");
        randf.setLength(size);
        randf.close();

        //test chunked upload
        HttpURLConnection urlConn = request("/test/v1/aggregate/upload", HttpMethod.PUT);
        urlConn.setChunkedStreamingMode(1024);
        Files.copy(Paths.get(fname.toURI()), urlConn.getOutputStream());
        assertEquals(200, urlConn.getResponseCode());

        assertEquals(size, Integer.parseInt(getContent(urlConn).split(":")[1].trim()));
        urlConn.disconnect();
        fname.delete();
    }

    //    @Test
    public void testChunkAggregatedUploadFailure() throws IOException {
        //create a random file to be uploaded.
        int size = 78 * 1024;
        File fname = new File(tmpFolder, "testChunkAggregatedUploadFailure.txt");
        fname.createNewFile();
        RandomAccessFile randf = new RandomAccessFile(fname, "rw");
        randf.setLength(size);
        randf.close();

        //test chunked upload
        HttpURLConnection urlConn = request("/test/v1/aggregate/upload", HttpMethod.PUT);
        urlConn.setChunkedStreamingMode(1024);
        Files.copy(Paths.get(fname.toURI()), urlConn.getOutputStream());
        assertEquals(500, urlConn.getResponseCode());
        urlConn.disconnect();
        fname.delete();
    }

    @Test
    public void testPathWithMultipleMethods() throws IOException {
        HttpURLConnection urlConn = request("/test/v1/tweets/1", HttpMethod.GET);
        assertEquals(200, urlConn.getResponseCode());
        urlConn.disconnect();

        urlConn = request("/test/v1/tweets/1", HttpMethod.PUT);
        writeContent(urlConn, "data");
        assertEquals(200, urlConn.getResponseCode());
        urlConn.disconnect();
    }


    @Test
    public void testNonExistingEndPoints() throws IOException {
        HttpURLConnection urlConn = request("/test/v1/users", HttpMethod.POST);
        writeContent(urlConn, "data");
        assertEquals(404, urlConn.getResponseCode());
        urlConn.disconnect();
    }

    @Test
    public void testPutWithData() throws IOException {
        HttpURLConnection urlConn = request("/test/v1/facebook/1/message", HttpMethod.PUT);
        writeContent(urlConn, "Hello, World");
        assertEquals(200, urlConn.getResponseCode());

        String content = getContent(urlConn);

        Map<String, String> map = GSON.fromJson(content, STRING_MAP_TYPE);
        assertEquals(1, map.size());
        assertEquals("Handled put in tweets end-point, id: 1. Content: Hello, World", map.get("result"));
        urlConn.disconnect();
    }

    @Test
    public void testPostWithData() throws IOException {
        HttpURLConnection urlConn = request("/test/v1/facebook/1/message", HttpMethod.POST);
        writeContent(urlConn, "Hello, World");
        assertEquals(200, urlConn.getResponseCode());

        String content = getContent(urlConn);

        Map<String, String> map = GSON.fromJson(content, STRING_MAP_TYPE);
        assertEquals(1, map.size());
        assertEquals("Handled post in tweets end-point, id: 1. Content: Hello, World", map.get("result"));
        urlConn.disconnect();
    }

    @Test
    public void testNonExistingMethods() throws IOException {
        HttpURLConnection urlConn = request("/test/v1/facebook/1/message", HttpMethod.GET);
        assertEquals(405, urlConn.getResponseCode());
        urlConn.disconnect();
    }

    @Test
    public void testKeepAlive() throws IOException {
        HttpURLConnection urlConn = request("/test/v1/tweets/1", HttpMethod.PUT, true);
        writeContent(urlConn, "data");
        assertEquals(200, urlConn.getResponseCode());

        assertEquals("keep-alive", urlConn.getHeaderField(HEADER_KEY_CONNECTION));
        urlConn.disconnect();
    }

    @Test
    public void testMultiplePathParameters() throws IOException {
        HttpURLConnection urlConn = request("/test/v1/user/sree/message/12", HttpMethod.GET);
        assertEquals(200, urlConn.getResponseCode());

        String content = getContent(urlConn);

        Map<String, String> map = GSON.fromJson(content, STRING_MAP_TYPE);
        assertEquals(1, map.size());
        assertEquals("Handled multiple path parameters sree 12", map.get("result"));
        urlConn.disconnect();
    }

    //Test the end point where the parameter in path and order of declaration in method signature are different
    @Test
    public void testMultiplePathParametersWithParamterInDifferentOrder() throws IOException {
        HttpURLConnection urlConn = request("/test/v1/message/21/user/sree", HttpMethod.GET);
        assertEquals(200, urlConn.getResponseCode());

        String content = getContent(urlConn);

        Map<String, String> map = GSON.fromJson(content, STRING_MAP_TYPE);
        assertEquals(1, map.size());
        assertEquals("Handled multiple path parameters sree 21", map.get("result"));
        urlConn.disconnect();
    }

    @Test
    public void testNotRoutablePathParamMismatch() throws IOException {
        HttpURLConnection urlConn = request("/test/v1/NotRoutable/sree", HttpMethod.GET);
        assertEquals(500, urlConn.getResponseCode());
        urlConn.disconnect();
    }

    @Test
    public void testMultiMatchParamPut() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/multi-match/bar", HttpMethod.PUT);
        assertEquals(405, urlConn.getResponseCode());
        urlConn.disconnect();
    }

    @Test
    public void testHandlerException() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/uexception", HttpMethod.GET);
        assertEquals(500, urlConn.getResponseCode());
        // assertEquals("Exception encountered while processing request : User Exception",
        //         new String(ByteStreams.toByteArray(urlConn.getErrorStream()), Charsets.UTF_8));
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
        assertEquals(200, urlConn.getResponseCode());
        assertEquals(urlConn.getHeaderField(TestChannelHandler.HEADER_FIELD), TestChannelHandler.HEADER_VALUE);
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
            assertEquals("Testing message", response);
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
        HttpURLConnection urlConn = request("/test/v1/listHeaderParam", HttpMethod.GET);
        urlConn.addRequestProperty("name", "name1,name3,name2,name1");
        assertEquals(200, urlConn.getResponseCode());
        assertEquals("name1,name3,name2,name1", getContent(urlConn));
        urlConn.disconnect();
    }

    @Test
    public void testHeaderResponse() throws IOException {
        HttpURLConnection urlConn = request("/test/v1/headerResponse", HttpMethod.GET);
        urlConn.addRequestProperty("name", "name1");

        assertEquals(200, urlConn.getResponseCode());
        assertEquals("name1", urlConn.getHeaderField("name"));
        urlConn.disconnect();
    }

    @Test
    public void testDefaultQueryParam() throws IOException {
        // Submit with no parameters. Each should get the default values.
        HttpURLConnection urlConn = request("/test/v1/defaultValue", HttpMethod.GET);
        assertEquals(200, urlConn.getResponseCode());
        JsonObject json = GSON.fromJson(getContent(urlConn), JsonObject.class);

        Type hobbyType = new TypeToken<List<String>>() {
        }.getType();

        assertEquals(30, json.get("age").getAsLong());
        assertEquals("hello", json.get("name").getAsString());
        assertEquals(Collections.singletonList("casking"),
                     GSON.fromJson(json.get("hobby").getAsJsonArray(), hobbyType));

        urlConn.disconnect();
    }

    @Test(timeOut = 5000)
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
            String response = IOUtils.toString(new InputStreamReader(socket.getInputStream(), Charsets.UTF_8));
            assertTrue(response.startsWith("HTTP/1.1 200 OK"));
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
                fail();
            } catch (IOException e) {
                // Expect to get exception since server response with 400. Just drain the error stream.
                IOUtils.toByteArray(urlConn.getErrorStream());
                assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), urlConn.getResponseCode());
            }
        } finally {
            urlConn.disconnect();
        }
    }

    @Test
    public void testNoPathGetMethod() throws Exception {
        HttpURLConnection urlConn = request("/test/v1", HttpMethod.GET);
        assertEquals("no-@Path-GET", getContent(urlConn));
        urlConn.disconnect();
    }

    @Test
    public void testNoPathPostMethod() throws Exception {
        HttpURLConnection urlConn = request("/test/v1", HttpMethod.POST);
        assertEquals("no-@Path-POST", getContent(urlConn));
        urlConn.disconnect();
    }

    @Test
    public void testNoPathPutMethod() throws Exception {
        HttpURLConnection urlConn = request("/test/v1", HttpMethod.PUT);
        assertEquals("no-@Path-PUT", getContent(urlConn));
        urlConn.disconnect();
    }

    @Test
    public void testNoPathDeleteMethod() throws Exception {
        HttpURLConnection urlConn = request("/test/v1", HttpMethod.DELETE);
        assertEquals("no-@Path-DELETE", getContent(urlConn));
        urlConn.disconnect();
    }

    @Test
    public void testSleep() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/sleep/10", HttpMethod.GET);
        assertEquals(200, urlConn.getResponseCode());
        urlConn.disconnect();
    }

    @Test
    public void testWrongMethod() throws IOException {
        HttpURLConnection urlConn = request("/test/v1/customException", HttpMethod.GET);
        assertEquals(Response.Status.METHOD_NOT_ALLOWED.getStatusCode(), urlConn.getResponseCode());
        urlConn.disconnect();
    }

    @Test
    public void testExceptionHandler() throws IOException {
        HttpURLConnection urlConn = request("/test/v1/customException", HttpMethod.POST);
        assertEquals(TestMicroservice.CustomException.HTTP_RESPONSE_STATUS, urlConn.getResponseCode());
        urlConn.disconnect();
    }

    @Test
    public void testConsumeJsonProduceString() throws IOException {
        HttpURLConnection urlConn = request("/test/v1/jsonConsumeStringProduce", HttpMethod.POST);
        urlConn.setRequestProperty(HttpHeaders.CONTENT_TYPE, "text/json");
        Gson gson = new Gson();
        Pet pet = petInstance();
        writeContent(urlConn, gson.toJson(pet));
        assertEquals(Response.Status.OK.getStatusCode(), urlConn.getResponseCode());
        assertEquals(pet.getDetails(), getContent(urlConn));
        urlConn.disconnect();
    }

    @Test
    public void testConsumeStringProduceJson() throws IOException {
        HttpURLConnection urlConn = request("/test/v1/textConsumeJsonProduce", HttpMethod.POST);
        urlConn.setRequestProperty(HttpHeaders.CONTENT_TYPE, "text/plain");
        String str = "send-something";
        writeContent(urlConn, str);
        assertEquals(Response.Status.OK.getStatusCode(), urlConn.getResponseCode());
        Gson gson = new Gson();
        String content = getContent(urlConn);
        TextBean textBean = gson.fromJson(content, TextBean.class);
        assertEquals(str, textBean.getText());
        urlConn.disconnect();
    }

    @Test
    public void testConsumeStringProduceString() throws IOException {
        HttpURLConnection urlConn = request("/test/v1/textConsumeTextProduce", HttpMethod.POST);
        urlConn.setRequestProperty(HttpHeaders.CONTENT_TYPE, "text/plain");
        String str = "send-something";
        writeContent(urlConn, str);
        assertEquals(Response.Status.OK.getStatusCode(), urlConn.getResponseCode());
        assertEquals(str + "-processed", getContent(urlConn));
        urlConn.disconnect();
    }

    @Test
    public void testConsumeXmlProduceXml() throws IOException, BeanConversionException {
        HttpURLConnection urlConn = request("/test/v1/textConsumeTextProduceXml", HttpMethod.POST);
        urlConn.setRequestProperty(HttpHeaders.CONTENT_TYPE, "text/xml");
        XmlBean xmlBean = new XmlBean();
        xmlBean.setName("send-something");
        xmlBean.setId(10);
        xmlBean.setValue(15);
        writeContent(urlConn, Charset.defaultCharset()
                .decode(BeanConverter.getConverter("text/xml").convertToMedia(xmlBean)).toString());
        assertEquals(Response.Status.OK.getStatusCode(), urlConn.getResponseCode());
        String respBody = getContent(urlConn);
        XmlBean xmlBean2 = (XmlBean) BeanConverter.getConverter("text/xml").convertToObject(
                ByteBuffer.wrap(respBody.getBytes(Charset.defaultCharset())), XmlBean.class);
        assertEquals(xmlBean.getName(), xmlBean2.getName());
        assertEquals(xmlBean.getId(), xmlBean2.getId());
        assertEquals(xmlBean.getValue(), xmlBean2.getValue());
        urlConn.disconnect();
    }

    @Test
    public void testDownloadPngFile() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/fileserver/png", HttpMethod.GET);
        assertEquals(Response.Status.OK.getStatusCode(), urlConn.getResponseCode());
        String contentType = urlConn.getHeaderField(HttpHeaders.CONTENT_TYPE);
        assertTrue("image/png".equalsIgnoreCase(contentType));
        InputStream downStream = urlConn.getInputStream();
        File file = new File(Thread.currentThread().getContextClassLoader().getResource("testPngFile.png").toURI());
        assertTrue(isStreamEqual(downStream, new FileInputStream(file)));
    }

    @Test
    public void testDownloadPngFileFromInputStream() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/fileserver/ip/png", HttpMethod.GET);
        assertEquals(Response.Status.OK.getStatusCode(), urlConn.getResponseCode());
        String contentType = urlConn.getHeaderField(HttpHeaders.CONTENT_TYPE);
        assertTrue("image/png".equalsIgnoreCase(contentType));
        InputStream downStream = urlConn.getInputStream();
        File file = new File(Thread.currentThread().getContextClassLoader().getResource("testPngFile.png").toURI());
        assertTrue(isStreamEqual(downStream, new FileInputStream(file)));
    }


    @Test
    public void testDownloadJpgFile() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/fileserver/jpg", HttpMethod.GET);
        assertEquals(Response.Status.OK.getStatusCode(), urlConn.getResponseCode());
        assertEquals("wso2", urlConn.getHeaderField("X-Custom-Header"));
        String contentType = urlConn.getHeaderField(HttpHeaders.CONTENT_TYPE);
        assertTrue("image/jpeg".equalsIgnoreCase(contentType));
        InputStream downStream = urlConn.getInputStream();
        File file = new File(Thread.currentThread().getContextClassLoader().getResource("testJpgFile.jpg").toURI());
        assertTrue(isStreamEqual(downStream, new FileInputStream(file)));
    }

    @Test
    public void testDownloadJpgFileFromInputStream() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/fileserver/ip/jpg", HttpMethod.GET);
        assertEquals(Response.Status.OK.getStatusCode(), urlConn.getResponseCode());
        assertEquals("wso2", urlConn.getHeaderField("X-Custom-Header"));
        String contentType = urlConn.getHeaderField(HttpHeaders.CONTENT_TYPE);
        assertTrue("image/jpeg".equalsIgnoreCase(contentType));
        InputStream downStream = urlConn.getInputStream();
        File file = new File(Thread.currentThread().getContextClassLoader().getResource("testJpgFile.jpg").toURI());
        assertTrue(isStreamEqual(downStream, new FileInputStream(file)));
    }

    @Test
    public void testDownloadTxtFile() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/fileserver/txt", HttpMethod.GET);
        assertEquals(Response.Status.OK.getStatusCode(), urlConn.getResponseCode());
        String contentType = urlConn.getHeaderField(HttpHeaders.CONTENT_TYPE);
        assertTrue("text/plain".equalsIgnoreCase(contentType));
        InputStream downStream = urlConn.getInputStream();
        File file = new File(Thread.currentThread().getContextClassLoader().getResource("testTxtFile.txt").toURI());
        assertTrue(isStreamEqual(downStream, new FileInputStream(file)));
    }

    @Test
    public void testDownloadTxtFileFromInputStream() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/fileserver/ip/txt", HttpMethod.GET);
        assertEquals(Response.Status.OK.getStatusCode(), urlConn.getResponseCode());
        String contentType = urlConn.getHeaderField(HttpHeaders.CONTENT_TYPE);
        assertTrue("text/plain".equalsIgnoreCase(contentType));
        InputStream downStream = urlConn.getInputStream();
        File file = new File(Thread.currentThread().getContextClassLoader().getResource("testTxtFile.txt").toURI());
        assertTrue(isStreamEqual(downStream, new FileInputStream(file)));
    }

    @Test
    public void testGzipCompressionWithNoGzipAccept() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/gzipfile", HttpMethod.GET);
        assertEquals(Response.Status.OK.getStatusCode(), urlConn.getResponseCode());
        String contentEncoding = urlConn.getHeaderField(HttpHeaders.CONTENT_ENCODING);
        assertTrue(contentEncoding == null || !contentEncoding.contains("gzip"));
        InputStream downStream = urlConn.getInputStream();
        assertTrue(IOUtils.toByteArray(downStream).length == IOUtils.toByteArray(
                Thread.currentThread().getContextClassLoader().getResource("testJpgFile.jpg").openStream()).length);
    }

    @Test
    public void testGzipCompressionWithGzipAccept() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/gzipfile", HttpMethod.GET);
        urlConn.addRequestProperty(HttpHeaders.ACCEPT_ENCODING, "gzip");
        assertEquals(Response.Status.OK.getStatusCode(), urlConn.getResponseCode());
        String contentEncoding = urlConn.getHeaderField(HttpHeaders.CONTENT_ENCODING);
        assertTrue("gzip".equalsIgnoreCase(contentEncoding));
        InputStream downStream = urlConn.getInputStream();
        assertTrue(IOUtils.toByteArray(downStream).length < IOUtils.toByteArray(
                Thread.currentThread().getContextClassLoader().getResource("testJpgFile.jpg").openStream()).length);
    }

    @Test
    public void testContentTypeSetting0() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/response/typehtml", HttpMethod.GET);
        assertEquals(Response.Status.OK.getStatusCode(), urlConn.getResponseCode());
        String contentType = urlConn.getHeaderField(HttpHeaders.CONTENT_TYPE);
        assertTrue(contentType.equalsIgnoreCase(MediaType.TEXT_HTML));
        String content = getContent(urlConn);
        assertEquals("Hello", content);
        urlConn.disconnect();
    }

    @Test
    public void testContentTypeSetting1() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/response/typehtml/str", HttpMethod.GET);
        assertEquals(Response.Status.OK.getStatusCode(), urlConn.getResponseCode());
        String contentType = urlConn.getHeaderField(HttpHeaders.CONTENT_TYPE);
        assertTrue(contentType.equalsIgnoreCase(MediaType.TEXT_HTML));
        String content = getContent(urlConn);
        assertEquals("Hello", content);
        urlConn.disconnect();
    }

    @Test
    public void testExceptionMapper() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/mappedException", HttpMethod.GET);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), urlConn.getResponseCode());
        assertEquals(MediaType.TEXT_PLAIN, urlConn.getHeaderField(HttpHeaders.CONTENT_TYPE));
        urlConn.disconnect();
    }

    @Test
    public void testExceptionMapper2() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/mappedException2", HttpMethod.GET);
        assertEquals(Response.Status.EXPECTATION_FAILED.getStatusCode(), urlConn.getResponseCode());
        urlConn.disconnect();
    }

    @Test
    public void tesFormParamWithURLEncoded() throws IOException {
        HttpURLConnection connection = request("/test/v1/formParam", HttpMethod.POST);
        String rawData = "name=wso2&age=10";
        ByteBuffer encodedData = Charset.defaultCharset().encode(rawData);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
        connection.setRequestProperty("Content-Length", String.valueOf(encodedData.array().length));
        try (OutputStream os = connection.getOutputStream()) {
            os.write(Arrays.copyOf(encodedData.array(), encodedData.limit()));
        }

        InputStream inputStream = connection.getInputStream();
        String response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        connection.disconnect();
        assertEquals(response, "wso2:10");
    }

    @Test
    public void testFormParamWithMultipart() throws IOException, URISyntaxException {
        HttpURLConnection connection = request("/test/v1/formParam", HttpMethod.POST);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addPart("name", new StringBody("wso2", ContentType.TEXT_PLAIN));
        builder.addPart("age", new StringBody("10", ContentType.TEXT_PLAIN));
        HttpEntity build = builder.build();
        connection.setRequestProperty("Content-Type", build.getContentType().getValue());
        try (OutputStream out = connection.getOutputStream()) {
            build.writeTo(out);
        }

        InputStream inputStream = connection.getInputStream();
        String response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        connection.disconnect();
        assertEquals(response, "wso2:10");
    }

    @Test
    public void testFormDataParamWithSimpleRequest() throws IOException, URISyntaxException {
        // Send x-form-url-encoded request
        HttpURLConnection connection = request("/test/v1/formDataParam", HttpMethod.POST);
        String rawData = "name=wso2&age=10";
        ByteBuffer encodedData = Charset.defaultCharset().encode(rawData);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
        connection.setRequestProperty("Content-Length", String.valueOf(encodedData.array().length));
        try (OutputStream os = connection.getOutputStream()) {
            os.write(Arrays.copyOf(encodedData.array(), encodedData.limit()));
        }

        InputStream inputStream = connection.getInputStream();
        String response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        connection.disconnect();
        assertEquals(response, "wso2:10");

        // Send multipart/form-data request
        connection = request("/test/v1/formDataParam", HttpMethod.POST);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addPart("name", new StringBody("wso2", ContentType.TEXT_PLAIN));
        builder.addPart("age", new StringBody("10", ContentType.TEXT_PLAIN));
        HttpEntity build = builder.build();
        connection.setRequestProperty("Content-Type", build.getContentType().getValue());
        try (OutputStream out = connection.getOutputStream()) {
            build.writeTo(out);
        }

        inputStream = connection.getInputStream();
        response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        connection.disconnect();
        assertEquals(response, "wso2:10");
    }

    @Test
    public void tesFormParamWithCollection() throws IOException {
        // Send x-form-url-encoded request
        HttpURLConnection connection = request("/test/v1/formParamWithList", HttpMethod.POST);
        String rawData = "names=WSO2&names=IBM";
        ByteBuffer encodedData = Charset.defaultCharset().encode(rawData);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
        connection.setRequestProperty("Content-Length", String.valueOf(encodedData.array().length));
        try (OutputStream os = connection.getOutputStream()) {
            os.write(Arrays.copyOf(encodedData.array(), encodedData.limit()));
        }

        InputStream inputStream = connection.getInputStream();
        String response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        connection.disconnect();
        assertEquals(response, "2");

        // Send multipart/form-data request
        connection = request("/test/v1/formParamWithList", HttpMethod.POST);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.addPart("names", new StringBody("WSO2", ContentType.TEXT_PLAIN));
        builder.addPart("names", new StringBody("IBM", ContentType.TEXT_PLAIN));
        builder.addPart("names", new StringBody("Oracle", ContentType.TEXT_PLAIN));
        HttpEntity build = builder.build();
        connection.setRequestProperty("Content-Type", build.getContentType().getValue());
        try (OutputStream out = connection.getOutputStream()) {
            build.writeTo(out);
        }

        inputStream = connection.getInputStream();
        response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        connection.disconnect();
        assertEquals(response, "3");

        // Send x-form-url-encoded request
        connection = request("/test/v1/formParamWithSet", HttpMethod.POST);
        rawData = "names=WSO2&names=IBM&names=IBM";
        encodedData = Charset.defaultCharset().encode(rawData);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
        connection.setRequestProperty("Content-Length", String.valueOf(encodedData.array().length));
        try (OutputStream os = connection.getOutputStream()) {
            os.write(Arrays.copyOf(encodedData.array(), encodedData.limit()));
        }

        inputStream = connection.getInputStream();
        response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        connection.disconnect();
        assertEquals(response, "2");

        // Send multipart/form-data request
        connection = request("/test/v1/formParamWithSet", HttpMethod.POST);
        builder = MultipartEntityBuilder.create();
        builder.addPart("names", new StringBody("WSO2", ContentType.TEXT_PLAIN));
        builder.addPart("names", new StringBody("IBM", ContentType.TEXT_PLAIN));
        builder.addPart("names", new StringBody("IBM", ContentType.TEXT_PLAIN));
        builder.addPart("names", new StringBody("Oracle", ContentType.TEXT_PLAIN));
        build = builder.build();
        connection.setRequestProperty("Content-Type", build.getContentType().getValue());
        try (OutputStream out = connection.getOutputStream()) {
            build.writeTo(out);
        }

        inputStream = connection.getInputStream();
        response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        connection.disconnect();
        assertEquals(response, "3");
    }

    @Test
    public void testFormParamWithFile() throws IOException, URISyntaxException {
        HttpURLConnection connection = request("/test/v1/testFormParamWithFile", HttpMethod.POST);
        File file = new File(Thread.currentThread().getContextClassLoader().getResource("testJpgFile.jpg").toURI());
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        FileBody fileBody = new FileBody(file, ContentType.DEFAULT_BINARY);
        builder.addPart("form", fileBody);
        HttpEntity build = builder.build();
        connection.setRequestProperty("Content-Type", build.getContentType().getValue());
        try (OutputStream out = connection.getOutputStream()) {
            build.writeTo(out);
        }

        InputStream inputStream = connection.getInputStream();
        String response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        connection.disconnect();
        assertEquals(response, file.getName());
    }

    @Test
    public void testFormDataParamWithComplexForm() throws IOException, URISyntaxException {
        HttpURLConnection connection = request("/test/v1/complexForm", HttpMethod.POST);
        StringBody companyText = new StringBody("{\"type\": \"Open Source\"}", ContentType.APPLICATION_JSON);
        StringBody personList = new StringBody(
                "[{\"name\":\"Richard Stallman\",\"age\":63}, {\"name\":\"Linus Torvalds\",\"age\":46}]",
                ContentType.APPLICATION_JSON);
        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addTextBody("id", "1")
                .addPart("company", companyText)
                .addPart("people", personList)
                .addBinaryBody("file",
                        new File(Thread.currentThread().getContextClassLoader().getResource("testTxtFile.txt").toURI()),
                               ContentType.DEFAULT_BINARY, "testTxtFile.txt").build();

        connection.setRequestProperty("Content-Type", reqEntity.getContentType().getValue());
        try (OutputStream out = connection.getOutputStream()) {
            reqEntity.writeTo(out);
        }

        InputStream inputStream = connection.getInputStream();
        String response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        connection.disconnect();
        assertEquals(response, "testTxtFile.txt:1:2:Open Source");
    }

    @Test
    public void testFormDataParamWithMultipleFiles() throws IOException, URISyntaxException {
        HttpURLConnection connection = request("/test/v1/multipleFiles", HttpMethod.POST);
        File file1 = new File(Thread.currentThread().getContextClassLoader().getResource("testTxtFile.txt").toURI());
        File file2 = new File(Thread.currentThread().getContextClassLoader().getResource("testPngFile.png").toURI());
        HttpEntity reqEntity = MultipartEntityBuilder.create().
                addBinaryBody("files", file1, ContentType.DEFAULT_BINARY, file1.getName())
                .addBinaryBody("files", file2, ContentType.DEFAULT_BINARY,
                        file2.getName()).build();

        connection.setRequestProperty("Content-Type", reqEntity.getContentType().getValue());
        try (OutputStream out = connection.getOutputStream()) {
            reqEntity.writeTo(out);
        }

        InputStream inputStream = connection.getInputStream();
        String response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        connection.disconnect();
        assertEquals(response, "2");
    }

    @Test
    public void testFormDataParamWithFileStream() throws IOException, URISyntaxException {
        HttpURLConnection connection = request("/test/v1/streamFile", HttpMethod.POST);
        File file = new File(Thread.currentThread().getContextClassLoader().getResource("testTxtFile.txt").toURI());
        HttpEntity reqEntity = MultipartEntityBuilder.create().
                addBinaryBody("file", file, ContentType.DEFAULT_BINARY, file.getName()).build();

        connection.setRequestProperty("Content-Type", reqEntity.getContentType().getValue());
        try (OutputStream out = connection.getOutputStream()) {
            reqEntity.writeTo(out);
        }

        InputStream inputStream = connection.getInputStream();
        String response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        connection.disconnect();
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            while (bufferedReader.ready()) {
                stringBuilder.append(bufferedReader.readLine());
            }
        }
        assertEquals(response, stringBuilder.toString() + "-" + file.getName());
    }

    @Test
    public void testGetAllFormItemsWithURLEncoded() throws IOException, URISyntaxException {
        HttpURLConnection connection = request("/test/v1/getAllFormItemsURLEncoded", HttpMethod.POST);
        String rawData = "names=WSO2&names=IBM&age=10&type=Software";
        ByteBuffer encodedData = Charset.defaultCharset().encode(rawData);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
        connection.setRequestProperty("Content-Length", String.valueOf(encodedData.array().length));
        try (OutputStream os = connection.getOutputStream()) {
            os.write(Arrays.copyOf(encodedData.array(), encodedData.limit()));
        }

        InputStream inputStream = connection.getInputStream();
        String response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        connection.disconnect();
        assertEquals("No of Companies-2 type-Software", response);
    }

    @Test
    public void getAllFormItemsMultipart() throws IOException, URISyntaxException {
        HttpURLConnection connection = request("/test/v1/getAllFormItemsMultipart", HttpMethod.POST);
        StringBody companyText = new StringBody("{\"type\": \"Open Source\"}", ContentType.APPLICATION_JSON);
        StringBody personList = new StringBody(
                "[{\"name\":\"Richard Stallman\",\"age\":63}, {\"name\":\"Linus Torvalds\",\"age\":46}]",
                ContentType.APPLICATION_JSON);
        HttpEntity reqEntity = MultipartEntityBuilder.create()
                .addTextBody("id", "1")
                .addPart("company", companyText)
                .addPart("people", personList)
                .addBinaryBody("file",
                        new File(Thread.currentThread().getContextClassLoader().getResource("testTxtFile.txt").toURI()),
                        ContentType.DEFAULT_BINARY, "testTxtFile.txt")
                .addBinaryBody("file",
                        new File(Thread.currentThread().getContextClassLoader().getResource("testPngFile.png").toURI()),
                        ContentType.DEFAULT_BINARY, "testPngFile.png")
                .build();

        connection.setRequestProperty("Content-Type", reqEntity.getContentType().getValue());
        try (OutputStream out = connection.getOutputStream()) {
            reqEntity.writeTo(out);
        }

        InputStream inputStream = connection.getInputStream();
        String response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        connection.disconnect();
        assertEquals("FileCount-2 SecondFileName-testPngFile.png FirstPerson-Richard Stallman", response);

        connection = request("/test/v1/getAllFormItemsXFormUrlEncoded", HttpMethod.POST);
        String rawData = "names=WSO2&names=IBM&type=Software";
        ByteBuffer encodedData = Charset.defaultCharset().encode(rawData);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
        connection.setRequestProperty("Content-Length", String.valueOf(encodedData.array().length));
        try (OutputStream os = connection.getOutputStream()) {
            os.write(Arrays.copyOf(encodedData.array(), encodedData.limit()));
        }

        inputStream = connection.getInputStream();
        response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        connection.disconnect();
        assertEquals("Type = Software No of names = 2 First name = IBM", response);
    }

    @Test
    public void testPathParamWithRegexOne() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/WSDL/12/states", HttpMethod.GET);

        InputStream inputStream = urlConn.getInputStream();
        String response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        urlConn.disconnect();
        assertEquals(response, "Asset Type = WSDL, Asset Id = 12");
    }

    @Test
    public void testPathParamWithRegexTwo() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/endpoints/WADL/10", HttpMethod.GET);
        InputStream inputStream = urlConn.getInputStream();
        String response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        urlConn.disconnect();
        assertEquals(response, "Asset Type = WADL, Asset Id = 10");
    }

    @Test
    public void testDualInvocation() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/testDualInvocation1", HttpMethod.GET);
        InputStream inputStream = urlConn.getInputStream();
        String response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        urlConn.disconnect();
        assertEquals("1", response);

        urlConn = request("/test/v1/testDualInvocation2", HttpMethod.GET);
        inputStream = urlConn.getInputStream();
        response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        urlConn.disconnect();
        assertEquals("2", response);
    }

    @Test
    public void testJsonProduceWithStringJsonArrayAndJsonObject() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/testJsonProduceWithString", HttpMethod.GET);
        InputStream inputStream = urlConn.getInputStream();
        String response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        urlConn.disconnect();
        assertEquals("{\"abc\":[{\"name\":\"Richard Stallman\",\"age\":63}, {\"name\":\"Linus Torvalds\",\"age\":46}]}",
                response);

        urlConn = request("/test/v1/testJsonProduceWithJsonArray", HttpMethod.GET);
        inputStream = urlConn.getInputStream();
        response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        urlConn.disconnect();
        assertEquals("[\"12\",\"15\",\"15\"]", response);

        urlConn = request("/test/v1/testJsonProduceWithJsonObject", HttpMethod.GET);
        inputStream = urlConn.getInputStream();
        response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        urlConn.disconnect();
        assertEquals("{\"name\":\"WSO2\",\"products\":[\"APIM\",\"IS\",\"MSF4J\"]}", response);
    }

    @Test
    public void testSetAndGetFromSession() throws Exception {
        long value = System.currentTimeMillis();

        // Request to first operation
        HttpURLConnection urlConn = request("/test/v1/set-session/" + value, HttpMethod.GET);
        assertEquals(204, urlConn.getResponseCode());
        String setCookieHeader = urlConn.getHeaderField("Set-Cookie");
        assertNotNull(setCookieHeader);
        urlConn.disconnect();

        // Request to 2nd operation
        urlConn = request("/test/v1/get-session/", HttpMethod.GET);
        urlConn.setRequestProperty("Cookie", setCookieHeader);
        assertEquals(200, urlConn.getResponseCode());
        setCookieHeader = urlConn.getHeaderField("Set-Cookie");
        assertNull(setCookieHeader);
        String content = getContent(urlConn); // content retrieved & returned from session
        assertEquals(String.valueOf(value), content);
        urlConn.disconnect();
    }

    @Test
    public void testSetAndGetFromSession2() throws Exception {
        long value = System.currentTimeMillis();

        // Request to first operation
        HttpURLConnection urlConn = request("/test/v1/set-session2/" + value, HttpMethod.GET);
        assertEquals(200, urlConn.getResponseCode());
        String setCookieHeader = urlConn.getHeaderField("Set-Cookie");
        assertNotNull(setCookieHeader);
        String content = getContent(urlConn);
        assertEquals(String.valueOf(value), content);
        urlConn.disconnect();

        // Request to 2nd operation
        urlConn = request("/test/v1/get-session/", HttpMethod.GET);
        urlConn.setRequestProperty("Cookie", setCookieHeader);
        assertEquals(200, urlConn.getResponseCode());
        setCookieHeader = urlConn.getHeaderField("Set-Cookie");
        assertNull(setCookieHeader);
        content = getContent(urlConn);  // content retrieved & returned from session
        assertEquals(String.valueOf(value), content);
        urlConn.disconnect();
    }

    @Test
    public void testSessionExpiry() throws Exception {
        long value = System.currentTimeMillis();

        // Request to first operation
        HttpURLConnection urlConn = request("/test/v1/set-session2/" + value, HttpMethod.GET);
        assertEquals(200, urlConn.getResponseCode());
        String setCookieHeader = urlConn.getHeaderField("Set-Cookie");
        assertNotNull(setCookieHeader);
        String content = getContent(urlConn);
        assertEquals(String.valueOf(value), content);
        urlConn.disconnect();

        // Request to 2nd operation
        urlConn = request("/test/v1/get-session/", HttpMethod.GET);
        urlConn.setRequestProperty("Cookie", setCookieHeader);
        assertEquals(200, urlConn.getResponseCode());
        setCookieHeader = urlConn.getHeaderField("Set-Cookie");
        assertNull(setCookieHeader);
        content = getContent(urlConn);  // content retrieved & returned from session
        assertEquals(String.valueOf(value), content);
        urlConn.disconnect();

        // Expire the session
        urlConn = request("/test/v1/expire-session/", HttpMethod.GET);
        urlConn.setRequestProperty("Cookie", setCookieHeader);
        assertEquals(204, urlConn.getResponseCode());
        setCookieHeader = urlConn.getHeaderField("Set-Cookie");
        assertNull(setCookieHeader);
        urlConn.disconnect();

        // Try to retrieve the object stored in the expired session
        urlConn = request("/test/v1/get-session/", HttpMethod.GET);
        urlConn.setRequestProperty("Cookie", setCookieHeader);
        assertEquals(204, urlConn.getResponseCode());
        setCookieHeader = urlConn.getHeaderField("Set-Cookie");
        assertNotNull(setCookieHeader);
        content = getContent(urlConn);  // content retrieved & returned from session
        assertEquals("", content);
        urlConn.disconnect();
    }

    @Test
    public void testCookieParam() throws Exception {
        String value = "wso2";

        HttpURLConnection urlConn = request("/test/v1/cookie/", HttpMethod.GET);
        urlConn.setRequestProperty("Cookie", "name=" + value);
        assertEquals(200, urlConn.getResponseCode());
        String content = getContent(urlConn);
        assertEquals(value, content);
        urlConn.disconnect();
    }

    @Test
    public void testSubResources() throws Exception {
        HttpURLConnection urlConn = request("/test/v1/SL/team", HttpMethod.GET);
        InputStream inputStream = urlConn.getInputStream();
        String response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        urlConn.disconnect();
        Team team = GSON.fromJson(response, Team.class);
        assertEquals("Cricket", team.getTeamType());
        assertEquals("SL", team.getCountryId());


        urlConn = request("/test/v1/SL/team", HttpMethod.POST);
        String rawData = "countryName=SriLanka";
        ByteBuffer encodedData = Charset.defaultCharset().encode(rawData);
        urlConn.setRequestMethod("POST");
        urlConn.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
        urlConn.setRequestProperty("Content-Length", String.valueOf(encodedData.array().length));
        try (OutputStream os = urlConn.getOutputStream()) {
            os.write(Arrays.copyOf(encodedData.array(), encodedData.limit()));
        }
        inputStream = urlConn.getInputStream();
        response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        urlConn.disconnect();
        team = GSON.fromJson(response, Team.class);
        assertEquals("Cricket", team.getTeamType());
        assertEquals("SL", team.getCountryId());
        assertEquals("SriLanka", team.getCountryName());


        urlConn = request("/test/v1/SL/team/123", HttpMethod.POST);
        rawData = "countryName=SriLanka&type=Batsman";
        encodedData = Charset.defaultCharset().encode(rawData);
        urlConn.setRequestMethod("POST");
        urlConn.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
        urlConn.setRequestProperty("Content-Length", String.valueOf(encodedData.array().length));
        try (OutputStream os = urlConn.getOutputStream()) {
            os.write(Arrays.copyOf(encodedData.array(), encodedData.limit()));
        }
        inputStream = urlConn.getInputStream();
        response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        urlConn.disconnect();
        Player player = GSON.fromJson(response, Player.class);
        assertEquals("player_1", player.getName());
        assertEquals(123, player.getPlayerId());
        assertEquals("SL", player.getCountryId());
        assertEquals("SriLanka", player.getCountryName());
        assertEquals(30, player.getAge());
        assertEquals("Batsman", player.getType());


        urlConn = request("/test/v1/SL/team/123/details/name", HttpMethod.POST);
        rawData = "countryName=SriLanka&type=Batsman";
        encodedData = Charset.defaultCharset().encode(rawData);
        urlConn.setRequestMethod("POST");
        urlConn.setRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
        urlConn.setRequestProperty("Content-Length", String.valueOf(encodedData.array().length));
        try (OutputStream os = urlConn.getOutputStream()) {
            os.write(Arrays.copyOf(encodedData.array(), encodedData.limit()));
        }
        inputStream = urlConn.getInputStream();
        response = StreamUtil.asString(inputStream);
        IOUtils.closeQuietly(inputStream);
        urlConn.disconnect();
        assertEquals("SL_123_name_Batsman_SriLanka", response);
    }

    protected Socket createRawSocket(URL url) throws IOException {
        return new Socket(url.getHost(), url.getPort());
    }

    protected void testContent(String path, String content) throws IOException {
        testContent(path, content, HttpMethod.GET);
    }

    protected void testContent(String path, String content, String method) throws IOException {
        HttpURLConnection urlConn = request(path, method);
        assertEquals(200, urlConn.getResponseCode());
        assertEquals(content, getContent(urlConn));
        urlConn.disconnect();
    }

    protected HttpURLConnection request(String path, String method) throws IOException {
        return request(path, method, false);
    }

    protected HttpURLConnection request(String path, String method, boolean keepAlive) throws IOException {
        URL url = baseURI.resolve(path).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
            urlConn.setDoOutput(true);
        }
        urlConn.setRequestMethod(method);
        if (!keepAlive) {
            urlConn.setRequestProperty(HEADER_KEY_CONNECTION, HEADER_VAL_CLOSE);
        }

        return urlConn;
    }

    protected HttpURLConnection request(String path, String method, boolean keepAlive, int port) throws IOException {
        URL url = URI.create(String.format("http://%s:%d", Constants.HOSTNAME, port)).resolve(path).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
            urlConn.setDoOutput(true);
        }
        urlConn.setRequestMethod(method);
        if (!keepAlive) {
            urlConn.setRequestProperty(HEADER_KEY_CONNECTION, HEADER_VAL_CLOSE);
        }

        return urlConn;
    }

    protected String getContent(HttpURLConnection urlConn) throws IOException {
        return new String(IOUtils.toByteArray(urlConn.getInputStream()), Charsets.UTF_8);
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
