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
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.io.Resources;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.Assert;
import org.wso2.msf4j.ChunkResponder;
import org.wso2.msf4j.HttpResponder;
import org.wso2.msf4j.HttpStreamHandler;
import org.wso2.msf4j.HttpStreamer;
import org.wso2.msf4j.Microservice;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.SortedSet;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Test handler.
 */
@SuppressWarnings("UnusedParameters")
@Path("/test/v1")
public class TestHandler implements Microservice {

    private static final Gson GSON = new Gson();

    @GET
    public String noMethodPathGet() {
        return "no-@Path-GET";
    }

    @POST
    public String noMethodPathPost() {
        return "no-@Path-POST";
    }

    @PUT
    public String noMethodPathPut() {
        return "no-@Path-PUT";
    }

    @DELETE
    public String noMethodPathDelete() {
        return "no-@Path-DELETE";
    }

    @Path("jsonConsumeStringProduce")
    @POST
    @Consumes("text/json")
    @Produces("text/plain")
    public String jsonConsume01(Pet input) {
        return input.getDetails();
    }

    @Path("textConsumeJsonProduce")
    @POST
    @Produces("text/json")
    @Consumes("text/plain")
    public TextBean textConsume01(String input) {
        TextBean textBean = new TextBean();
        textBean.setText(input);
        return textBean;
    }

    @Path("textConsumeTextProduce")
    @POST
    @Consumes("text/plain")
    @Produces("text/plain")
    public String textConsume02(String input) {
        return input + "-processed";
    }

    @Path("textConsumeTextProduceXml")
    @POST
    @Consumes("text/xml")
    @Produces("text/xml")
    public XmlBean textConsume03(XmlBean input) {
        return input;
    }

    @Path("sleep/{seconds}")
    @GET
    public Response testSleep(@PathParam("seconds") int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
            return Response.status(Response.Status.OK).entity("slept: " + seconds + "s").build();
        } catch (InterruptedException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }

    @Path("response/typehtml")
    @GET
    public Response produceHtmlContent0() {
        return Response.ok()
                .type(MediaType.TEXT_HTML_TYPE)
                .entity("Hello")
                .build();
    }

    @Path("response/typehtml/str")
    @GET
    public Response produceHtmlContent1() {
        return Response.ok()
                .type(MediaType.TEXT_HTML)
                .entity("Hello")
                .build();
    }

    @Path("resource")
    @GET
    public Response testGet() {
        JsonObject object = new JsonObject();
        object.addProperty("status", "Handled get in resource end-point");
        return Response.status(Response.Status.OK).entity(object).build();
    }

    @Path("tweets/{id}")
    @GET
    public Response testGetTweet(@PathParam("id") String id) {
        JsonObject object = new JsonObject();
        object.addProperty("status", String.format("Handled get in tweets end-point, id: %s", id));
        return Response.status(Response.Status.OK).entity(object).build();
    }

    @Path("tweets/{id}")
    @PUT
    public Response testPutTweet(@PathParam("id") String id) {
        JsonObject object = new JsonObject();
        object.addProperty("status", String.format("Handled put in tweets end-point, id: %s", id));
        return Response.status(Response.Status.OK).entity(object).build();
    }

    @Path("facebook/{id}/message")
    @DELETE
    public void testNoMethodRoute(@PathParam("id") String id) {

    }

    @Path("facebook/{id}/message")
    @PUT
    public Response testPutMessage(@PathParam("id") String id, @Context HttpRequest request) {
        String message = String.format("Handled put in tweets end-point, id: %s. ", id);
        try {
            String data = getStringContent(request);
            message = message.concat(String.format("Content: %s", data));
        } catch (IOException e) {
            //This condition should never occur
            Assert.fail();
        }
        JsonObject object = new JsonObject();
        object.addProperty("result", message);
        return Response.status(Response.Status.OK).entity(object).build();
    }

    @Path("facebook/{id}/message")
    @POST
    public Response testPostMessage(@PathParam("id") String id, @Context HttpRequest request) {
        String message = String.format("Handled post in tweets end-point, id: %s. ", id);
        try {
            String data = getStringContent(request);
            message = message.concat(String.format("Content: %s", data));
        } catch (IOException e) {
            //This condition should never occur
            Assert.fail();
        }
        JsonObject object = new JsonObject();
        object.addProperty("result", message);
        return Response.status(Response.Status.OK).entity(object).build();
    }

    @Path("/user/{userId}/message/{messageId}")
    @GET
    public Response testMultipleParametersInPath(@PathParam("userId") String userId,
                                                 @PathParam("messageId") int messageId) {
        JsonObject object = new JsonObject();
        object.addProperty("result", String.format("Handled multiple path parameters %s %d", userId, messageId));
        return Response.status(Response.Status.OK).entity(object).build();
    }

    @Path("/message/{messageId}/user/{userId}")
    @GET
    public Response testMultipleParametersInDifferentParameterDeclarationOrder(@PathParam("userId") String userId,
                                                                               @PathParam("messageId") int messageId) {
        JsonObject object = new JsonObject();
        object.addProperty("result", String.format("Handled multiple path parameters %s %d", userId, messageId));
        return Response.status(Response.Status.OK).entity(object).build();
    }

    @Path("/NotRoutable/{id}")
    @GET
    public Response notRoutableParameterMismatch(@PathParam("userid") String userId) {
        JsonObject object = new JsonObject();
        object.addProperty("result", String.format("Handled Not routable path %s ", userId));
        return Response.status(Response.Status.OK).entity(object).build();
    }

    @Path("/exception")
    @GET
    public void exception() {
        throw new IllegalArgumentException("Illegal argument");
    }

    private String getStringContent(HttpRequest request) throws IOException {
        return ((FullHttpRequest) request).content().toString(Charsets.UTF_8);
    }

    @Path("/multi-match/**")
    @GET
    public String multiMatchAll() {
        return "multi-match-*";
    }

    @Path("/multi-match/{param}")
    @GET
    public String multiMatchParam(@PathParam("param") String param) {
        return "multi-match-param-" + param;
    }

    @Path("/multi-match/foo")
    @GET
    public String multiMatchFoo() {
        return "multi-match-get-actual-foo";
    }

    @Path("/multi-match/foo")
    @PUT
    public String multiMatchParamPut() {
        return "multi-match-put-actual-foo";
    }

    @Path("/multi-match/{param}/bar")
    @GET
    public String multiMatchParamBar(@PathParam("param") String param) {
        return "multi-match-param-bar-" + param;
    }

    @Path("/multi-match/foo/{param}")
    @GET
    public String multiMatchFooParam(@PathParam("param") String param) {
        return "multi-match-get-foo-param-" + param;
    }

    @Path("/multi-match/foo/{param}/bar")
    @GET
    public String multiMatchFooParamBar(@PathParam("param") String param) {
        return "multi-match-foo-param-bar-" + param;
    }

    @Path("/multi-match/foo/bar/{param}")
    @GET
    public String multiMatchFooBarParam(@PathParam("param") String param) {
        return "multi-match-foo-bar-param-" + param;
    }

    @Path("/multi-match/foo/{param}/bar/baz")
    @GET
    public String multiMatchFooParamBarBaz(@PathParam("param") String param) {
        return "multi-match-foo-param-bar-baz-" + param;
    }

    @Path("/multi-match/foo/bar/{param}/{id}")
    @GET
    public String multiMatchFooBarParamId(@PathParam("param") String param, @PathParam("id") String id) {
        return "multi-match-foo-bar-param-" + param + "-id-" + id;
    }

    @Path("/fileserver/{fileType}")
    @GET
    public Response serveFile(@PathParam("fileType") String fileType) throws Exception {
        File file;
        if (fileType.equals("png")) {
            file = new File(Resources.getResource("testPngFile.png").toURI());
            return Response.ok(file).build();
        } else if (fileType.equals("jpg")) {
            file = new File(Resources.getResource("testJpgFile.jpg").toURI());
            return Response.ok(file).build();
        } else if (fileType.equals("txt")) {
            file = new File(Resources.getResource("testTxtFile.txt").toURI());
            return Response.ok(file).build();
        }
        return Response.noContent().build();
    }

    @Path("/stream/upload")
    @PUT
    public void streamUpload(@Context HttpStreamer httpStreamer) throws Exception {
        final StringBuffer sb = new StringBuffer();
        httpStreamer.callback(new HttpStreamHandler() {
            @Override
            public void chunk(ByteBuf request, HttpResponder responder) {
                sb.append(request.toString(Charsets.UTF_8));
            }

            @Override
            public void finished(ByteBuf request, HttpResponder responder) {
                sb.append(request.toString(Charsets.UTF_8));
                responder.sendString(HttpResponseStatus.OK, sb.toString());
            }

            @Override
            public void error(Throwable cause) {
                sb.delete(0, sb.length());
            }
        });
    }

    @Path("/stream/upload/fail")
    @PUT
    public HttpStreamHandler streamUploadFailure() {
        final int fileSize = 30 * 1024 * 1024;

        return new HttpStreamHandler() {
            int count = 0;
            ByteBuffer offHeapBuffer = ByteBuffer.allocateDirect(fileSize);

            @Override
            public void chunk(ByteBuf request, HttpResponder responder) {
                Preconditions.checkState(count == 1, "chunk error");
                offHeapBuffer.put(request.array());
            }

            @Override
            public void finished(ByteBuf request, HttpResponder responder) {
                int bytesUploaded = offHeapBuffer.position();
                responder.sendString(HttpResponseStatus.OK, "Uploaded:" + bytesUploaded);
            }

            @Override
            public void error(Throwable cause) {
                offHeapBuffer = null;
            }
        };
    }

    @Path("/aggregate/upload")
    @PUT
    public String aggregatedUpload(@Context HttpRequest request) {
        ByteBuf content = ((FullHttpRequest) request).content();
        int bytesUploaded = content.readableBytes();
        return "Uploaded:" + bytesUploaded;
    }

    @Path("/chunk")
    @POST
    public void chunk(HttpRequest request, HttpResponder responder) throws IOException {
        // Echo the POST body of size 1 byte chunk
        ByteBuf content = ((FullHttpRequest) request).content();
        ChunkResponder chunker = responder.sendChunkStart(HttpResponseStatus.OK, null);
        while (content.isReadable()) {
            chunker.sendChunk(content.readSlice(1));
        }
        chunker.close();
    }

    @Path("/gzipfile")
    @GET
    public Response gzipFile() throws IOException, URISyntaxException {
        File file = new File(Resources.getResource("testJpgFile.jpg").toURI());
        return Response.ok().entity(file).build();
    }

    @Path("/uexception")
    @GET
    public void testException() {
        throw Throwables.propagate(new RuntimeException("User Exception"));
    }

    @Path("/noresponse")
    @GET
    public void testNoResponse() {
    }

    @Path("/stringQueryParam/{path}")
    @GET
    public String testStringQueryParam(@PathParam("path") String path, @QueryParam("name") String name) {
        return path + ":" + name;
    }

    @Path("/primitiveQueryParam")
    @GET
    public String testPrimitiveQueryParam(@QueryParam("age") int age) {
        return Integer.toString(age);
    }

    @Path("/sortedSetQueryParam")
    @GET
    public String testSortedSetQueryParam(@QueryParam("id") SortedSet<Integer> ids) {
        return Joiner.on(',').join(ids);
    }

    @Path("/listHeaderParam")
    @GET
    public String testListHeaderParam(@HeaderParam("name") List<String> names) {
        return Joiner.on(',').join(names);
    }

    @Path("/headerResponse")
    @GET
    public Response testHeaderResponse(@HeaderParam("name") String name) {
        return Response.status(HttpResponseStatus.OK.code()).entity("Entity").header("name", name).build();
    }

    @Path("/defaultValue")
    @GET
    public Object testDefaultValue(@DefaultValue("30") @QueryParam("age") Integer age,
                                   @DefaultValue("hello") @QueryParam("name") String name,
                                   @DefaultValue("casking") @HeaderParam("hobby") List<String> hobbies) {
        JsonObject response = new JsonObject();
        response.addProperty("age", age);
        response.addProperty("name", name);
        response.add("hobby", GSON.toJsonTree(hobbies, new TypeToken<List<String>>() {
        }.getType()));

        return response;
    }

    @Path("/connectionClose")
    @GET
    public Response testConnectionClose() {
        return Response.status(Response.Status.OK).entity("Close connection").header("Connection", "close").build();
    }

    @Path("/uploadReject")
    @POST
    public Response testUploadReject() {
        return Response.status(Response.Status.BAD_REQUEST).entity("Rejected").header("Connection", "close").build();
    }

    @Path("/customException")
    @POST
    public void testCustomException() throws CustomException {
        throw new CustomException();
    }

    /**
     * Custom exception class for testing exception handler.
     */
    public static final class CustomException extends Exception {
        public static final HttpResponseStatus HTTP_RESPONSE_STATUS = HttpResponseStatus.INTERNAL_SERVER_ERROR;
    }
}
