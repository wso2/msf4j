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

package org.wso2.msf4j.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.IOUtils;
import org.wso2.msf4j.HttpStreamHandler;
import org.wso2.msf4j.HttpStreamer;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.exception.MappedException;
import org.wso2.msf4j.exception.MappedException2;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FormItem;
import org.wso2.msf4j.formparam.FormParamIterator;
import org.wso2.msf4j.formparam.exception.FormUploadException;
import org.wso2.msf4j.pojo.Company;
import org.wso2.msf4j.pojo.Person;
import org.wso2.msf4j.pojo.Pet;
import org.wso2.msf4j.pojo.TextBean;
import org.wso2.msf4j.pojo.XmlBean;
import org.wso2.msf4j.service.sub.Team;
import org.wso2.msf4j.util.BufferUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
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
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static org.testng.AssertJUnit.fail;

/**
 * Test service.
 */
@SuppressWarnings("UnusedParameters")
@Path("/test/v1")
public class TestMicroservice implements Microservice {
    private static final String SAMPLE_STRING = "foo";

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
    public Response testPutMessage(@PathParam("id") String id, @Context Request request) {
        String message = String.format("Handled put in tweets end-point, id: %s. ", id);
        try {
            String data = getStringContent(request);
            message = message.concat(String.format("Content: %s", data));
        } catch (IOException e) {
            //This condition should never occur
            fail();
        }
        JsonObject object = new JsonObject();
        object.addProperty("result", message);
        return Response.status(Response.Status.OK).entity(object).build();
    }

    @Path("facebook/{id}/message")
    @POST
    public Response testPostMessage(@PathParam("id") String id, @Context Request request) {
        String message = String.format("Handled post in tweets end-point, id: %s. ", id);
        try {
            String data = getStringContent(request);
            message = message.concat(String.format("Content: %s", data));
        } catch (IOException e) {
            //This condition should never occur
            fail();
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

    private String getStringContent(Request request) throws IOException {
        return Charset.defaultCharset().decode(BufferUtil.merge(request.getFullMessageBody())).toString();
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
        if ("png".equals(fileType)) {
            file = new File(Thread.currentThread().getContextClassLoader().getResource("testPngFile.png").toURI());
            return Response.ok(file).build();
        } else if ("jpg".equals(fileType)) {
            file = new File(Thread.currentThread().getContextClassLoader().getResource("testJpgFile.jpg").toURI());
            return Response.ok(file).header("X-Custom-Header", "wso2").build();
        } else if ("txt".equals(fileType)) {
            file = new File(Thread.currentThread().getContextClassLoader().getResource("testTxtFile.txt").toURI());
            return Response.ok(file).build();
        }
        return Response.noContent().build();
    }

    @Path("/fileserver/ip/{fileType}")
    @GET
    public Response serveInputStream(@PathParam("fileType") String fileType) throws Exception {
        if ("png".equals(fileType)) {
            InputStream ipStream = new FileInputStream(
                    new File(Thread.currentThread().getContextClassLoader().getResource("testPngFile.png").toURI()));
            return Response.ok(ipStream).type("image/png").build();
        } else if ("jpg".equals(fileType)) {
            InputStream ipStream = new FileInputStream(
                    new File(Thread.currentThread().getContextClassLoader().getResource("testJpgFile.jpg").toURI()));
            return Response.ok(ipStream).type("image/jpeg").header("X-Custom-Header", "wso2").build();
        } else if ("txt".equals(fileType)) {
            InputStream ipStream = new FileInputStream(
                    new File(Thread.currentThread().getContextClassLoader().getResource("testTxtFile.txt").toURI()));
            return Response.ok(ipStream).type("text/plain").build();
        }
        return Response.noContent().build();
    }

    @Path("/stream/upload")
    @PUT
    public void streamUpload(@Context HttpStreamer httpStreamer) throws Exception {
        final StringBuffer sb = new StringBuffer();
        httpStreamer.callback(new HttpStreamHandler() {

            private org.wso2.msf4j.Response response;

            @Override
            public void init(org.wso2.msf4j.Response response) {

                this.response = response;
            }

            @Override
            public void chunk(ByteBuffer content) throws Exception {
                sb.append(Charset.defaultCharset().decode(content).toString());
            }

            @Override
            public void end() throws Exception {
                response.setStatus(Response.Status.OK.getStatusCode());
                response.setEntity(sb.toString());
                response.send();
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
            private org.wso2.msf4j.Response response;
            ByteBuffer offHeapBuffer = ByteBuffer.allocateDirect(fileSize);

            @Override
            public void init(org.wso2.msf4j.Response response) {
                this.response = response;
            }

            @Override
            public void chunk(ByteBuffer content) throws Exception {
                offHeapBuffer.put(content.array());
            }

            @Override
            public void end() throws Exception {
                int bytesUploaded = offHeapBuffer.position();
                response.setStatus(Response.Status.OK.getStatusCode());
                response.setEntity("Uploaded:" + bytesUploaded);
                response.send();
            }

            @Override
            public void error(Throwable cause) {
                offHeapBuffer = null;
            }
        };
    }

    @Path("/aggregate/upload")
    @PUT
    public String aggregatedUpload(@Context Request request) {
        ByteBuffer content = BufferUtil.merge(request.getFullMessageBody());
        int bytesUploaded = content.capacity();
        return "Uploaded:" + bytesUploaded;
    }

    @Path("/gzipfile")
    @GET
    public Response gzipFile() throws IOException, URISyntaxException {
        File file = new File(Thread.currentThread().getContextClassLoader().getResource("testJpgFile.jpg").toURI());
        return Response.ok().entity(file).build();
    }

    @Path("/uexception")
    @GET
    public void testException() {
        throw new RuntimeException("User Exception");
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
        StringBuilder response = new StringBuilder();
        ids.forEach(id -> response.append(id).append(","));
        if (response.length() > 0) {
            response.setLength(response.length() - 1);
        }
        return response.toString();
    }

    @Path("/listHeaderParam")
    @GET
    public String testListHeaderParam(@HeaderParam("name") List<String> names) {
        StringBuilder response = new StringBuilder();
        names.forEach(name -> response.append(name).append(","));
        response.setLength(response.length() - 1);
        return response.toString();
    }

    @Path("/headerResponse")
    @GET
    public Response testHeaderResponse(@HeaderParam("name") String name) {
        return Response.status(Response.Status.OK.getStatusCode()).entity("Entity").header("name", name).build();
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

    @Path("/mappedException")
    @GET
    public void testExceptionMapping() throws MappedException {
        throw new MappedException("Mapped exception thrown");
    }

    @Path("/mappedException2")
    @GET
    public void testExceptionMapping2() throws MappedException2 {
        throw new MappedException2("Mapped exception 2 thrown");
    }

    @Path("/formParam")
    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA})
    public Response tesFormParamWithURLEncoded(@FormParam("name") String name, @FormParam("age") int age) {
        return Response.ok().entity(name + ":" + age).build();
    }

    @Path("/formDataParam")
    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA})
    public Response tesFormDataParam(@FormDataParam("name") String name, @FormDataParam("age") int age) {
        return Response.ok().entity(name + ":" + age).build();
    }

    @Path("/formParamWithList")
    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA})
    public Response tesFormParamList(@FormParam("names") List<String> names) {
        return Response.ok().entity(names.size()).build();
    }

    @Path("/formParamWithSet")
    @POST
    @Consumes({MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA})
    public Response tesFormParamSet(@FormParam("names") Set<String> names) {
        return Response.ok().entity(names.size()).build();
    }

    @POST
    @Path("/testFormParamWithFile")
    public Response testFormParamWithFile(@Context FormParamIterator formParamIterator) {
        String response = "";
        try {
            while (formParamIterator.hasNext()) {
                FormItem item = formParamIterator.next();
                response = item.getName();
            }
        } catch (FormUploadException e) {
            response = e.getMessage();
        }
        return Response.ok().entity(response).build();
    }

    @POST
    @Path("/complexForm")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response complexForm(@FormDataParam("file") File file,
                                @FormDataParam("id") int id,
                                @FormDataParam("people") List<Person> personList,
                                @FormDataParam("company") Company company) {
        return Response.ok().entity(file.getName() + ":" + id + ":" + personList.size() + ":" + company.getType())
                .build();
    }


    @POST
    @Path("/multipleFiles")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response multipleFiles(@FormDataParam("files") List<File> files) {
        return Response.ok().entity(files.size()).build();
    }

    @POST
    @Path("/streamFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response multipleFiles(@FormDataParam("file") FileInfo fileInfo,
                                  @FormDataParam("file") InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            while (bufferedReader.ready()) {
                stringBuilder.append(bufferedReader.readLine());
            }
        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return Response.ok().entity(stringBuilder.toString() + "-" + fileInfo.getFileName()).build();
    }

    @POST
    @Path("/getAllFormItemsURLEncoded")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getAllFormItemsURLEncoded(@Context MultivaluedMap formItemMultivaluedMap) {
        int noOfCompanies = ((ArrayList) formItemMultivaluedMap.get("names")).size();
        String type = formItemMultivaluedMap.getFirst("type").toString();
        String response = "No of Companies-" + noOfCompanies + " type-" + type;
        return Response.ok().entity(response).build();
    }

    @POST
    @Path("/getAllFormItemsMultipart")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response getAllFormItemsMultipart(@Context MultivaluedMap formItemMultivaluedMap) {
        ArrayList files = (ArrayList) formItemMultivaluedMap.get("file");
        String person = formItemMultivaluedMap.getFirst("people").toString();
        JsonParser parser = new JsonParser();
        String name = parser.parse(person).getAsJsonArray().get(0).getAsJsonObject().get("name").getAsString();
        String response =
                "FileCount-" + files.size() + " SecondFileName-" + ((File) files.get(1)).getName() + " FirstPerson-" +
                        name;
        return Response.ok().entity(response).build();
    }

    @POST
    @Path("/getAllFormItemsXFormUrlEncoded")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getAllFormItemsXFormUrlEncoded(@Context MultivaluedMap formItemMultivaluedMap) {
        ArrayList names = (ArrayList) formItemMultivaluedMap.get("names");
        String type = formItemMultivaluedMap.getFirst("type").toString();
        String response = "Type = " + type + " No of names = " + names.size() + " First name = " + names.get(1);
        return Response.ok().entity(response).build();
    }

    @GET
    @Path("{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}/states")
    public Response testPathParamWithRegexOne(@PathParam("assetType") String assetType, @PathParam("id") String id) {
        StringBuilder sb = new StringBuilder();
        sb.append("Asset Type = ").append(assetType).append(", Asset Id = ").append(id);
        return Response.ok().entity(sb.toString()).build();
    }

    @GET
    @Path("/endpoints/{assetType : [a-zA-Z][a-zA-Z_0-9]*}/{id}")
    public Response testPathParamWithRegexTwo(@PathParam("assetType") String assetType, @PathParam("id") String id) {
        StringBuilder sb = new StringBuilder();
        sb.append("Asset Type = ").append(assetType).append(", Asset Id = ").append(id);
        return Response.ok().entity(sb.toString()).build();
    }

    private static int initialValue = 0;

    @GET
    @Path("/testDualInvocation1")
    public Response testDualInvocation1() {
        initialValue++;
        return Response.ok().entity(initialValue).build();
    }

    @GET
    @Path("/testDualInvocation2")
    public Response testDualInvocation2() {
        int returnVal = initialValue + 1;
        initialValue = 0;
        return Response.ok().entity(returnVal).build();
    }

    @GET
    @Path("/testJsonProduceWithString")
    @Produces(MediaType.APPLICATION_JSON)
    public Response testJsonProduceWithString() {
        String res = "{\"abc\":[{\"name\":\"Richard Stallman\",\"age\":63}, {\"name\":\"Linus Torvalds\",\"age\":46}]}";
        return Response.ok().entity(res).build();
    }

    @GET
    @Path("/testJsonProduceWithJsonArray")
    @Produces(MediaType.APPLICATION_JSON)
    public Response testJsonProduceWithJsonArray() {
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(new JsonPrimitive("12"));
        jsonArray.add(new JsonPrimitive("15"));
        jsonArray.add(new JsonPrimitive("15"));
        return Response.ok().entity(jsonArray).build();
    }

    @GET
    @Path("/testJsonProduceWithJsonObject")
    @Produces(MediaType.APPLICATION_JSON)
    public Response testJsonProduceWithJJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("name", new JsonPrimitive("WSO2"));
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(new JsonPrimitive("APIM"));
        jsonArray.add(new JsonPrimitive("IS"));
        jsonArray.add(new JsonPrimitive("MSF4J"));
        jsonObject.add("products", jsonArray);
        return Response.ok().entity(jsonObject).build();
    }

    /**
     * Operation with no content in the response and sets a value in the session.
     */
    @GET
    @Path("/set-session/{value}")
    public void setObjectInSession(@Context Request request, @PathParam("value") String value) {
        request.getSession().setAttribute(SAMPLE_STRING, value);
    }

    /**
     * Operation which returns content in the response and sets a value in the session.
     */
    @GET
    @Path("/set-session2/{value}")
    public String setObjectInSession2(@Context Request request, @PathParam("value") String value) {
        request.getSession().setAttribute(SAMPLE_STRING, value);
        return value;
    }

    /**
     * Operation which retrieves value set in the session in the {@link #setObjectInSession} &
     * {@link #setObjectInSession2} methods.
     */
    @GET
    @Path("/get-session")
    public String getObjectFromSession(@Context Request request) {
        return (String) request.getSession().getAttribute(SAMPLE_STRING);
    }

    @GET
    @Path("/expire-session")
    public void expireSession(@Context Request request) {
        request.getSession().invalidate();
    }

    @GET
    @Path("/cookie")
    public String echoCookieValue(@CookieParam("name") String name) {
        return name;
    }


    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{countryId}/team")
    public Team getCountryTeam(@PathParam("countryId") String countryId) {
        return new Team(countryId);
    }


    /**
     * Custom exception class for testing exception handler.
     */
    public static final class CustomException extends Exception {
        public static final int HTTP_RESPONSE_STATUS = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
    }
}
