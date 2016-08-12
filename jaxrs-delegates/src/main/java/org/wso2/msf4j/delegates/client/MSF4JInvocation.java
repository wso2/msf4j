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

package org.wso2.msf4j.delegates.client;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Future;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * Implementation class of JAX-RS request invocation.
 */
// TODO: Complete the spec implementation and use carbon-transports.
public class MSF4JInvocation implements Invocation {

    @Override
    public Invocation property(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response invoke() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T invoke(Class<T> responseType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T invoke(GenericType<T> responseType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future<Response> submit() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Future<T> submit(Class<T> responseType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Future<T> submit(GenericType<T> responseType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Future<T> submit(InvocationCallback<T> callback) {
        throw new UnsupportedOperationException();
    }

    /**
     * Implementation class of JAX-RS request invocation builder.
     */
    public static class Builder implements Invocation.Builder {

        private static final Logger log = LoggerFactory.getLogger(Builder.class);
        private MSF4JClientRequestContext clientRequestContext;
        private List<Object> providerComponents = new ArrayList<>();

        /**
         * Constructor of MSF4JInvocation.Builder
         *
         * @param clientRequestContext ClientRequestContext to create the InvocationBuilder
         * @param providerComponents   List of provider components
         */
        public Builder(MSF4JClientRequestContext clientRequestContext, List<Object> providerComponents) {
            this.clientRequestContext = clientRequestContext;
            this.providerComponents.addAll(providerComponents);
        }

        @Override
        public Invocation build(String method) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Invocation build(String method, Entity<?> entity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Invocation buildGet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Invocation buildDelete() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Invocation buildPost(Entity<?> entity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Invocation buildPut(Entity<?> entity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public AsyncInvoker async() {
            throw new UnsupportedOperationException();
        }

        /**
         * Set accept media types.
         *
         * @param mediaTypes Array of accpet media types as strings
         * @return This Invocation.Builder
         */
        @Override
        public Invocation.Builder accept(String... mediaTypes) {
            clientRequestContext.setAcceptResponseTypesStr(Arrays.asList(mediaTypes));
            return this;
        }

        /**
         * Set accept media types.
         *
         * @param mediaTypes Array of accpet media types as MediaTypes.
         * @return This Invocation.Builder
         */
        @Override
        public Invocation.Builder accept(MediaType... mediaTypes) {
            clientRequestContext.setAcceptResponseTypes(Arrays.asList(mediaTypes));
            return this;
        }

        @Override
        public Invocation.Builder acceptLanguage(Locale... locales) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Invocation.Builder acceptLanguage(String... locales) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Invocation.Builder acceptEncoding(String... encodings) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Invocation.Builder cookie(Cookie cookie) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Invocation.Builder cookie(String name, String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Invocation.Builder cacheControl(CacheControl cacheControl) {
            throw new UnsupportedOperationException();
        }

        /**
         * Set headers of the request.
         *
         * @param name  Header name
         * @param value Header value
         * @return This Invocation.Builder
         */
        @Override
        public Invocation.Builder header(String name, Object value) {
            clientRequestContext.getHeaders().add(name, value.toString());
            return this;
        }

        @Override
        public Invocation.Builder headers(MultivaluedMap<String, Object> headers) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Invocation.Builder property(String name, Object value) {
            throw new UnsupportedOperationException();
        }

        /**
         * Make a HTTP GET request.
         *
         * @return Response object
         */
        @Override
        public Response get() {
            clientRequestContext.setMethod(HttpMethod.GET);
            return sendRequest(clientRequestContext);
        }

        @Override
        public <T> T get(Class<T> responseType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T get(GenericType<T> responseType) {
            throw new UnsupportedOperationException();
        }

        /**
         * Make a HTTP PUT request.
         *
         * @param entity Entity of the request
         * @return Response object
         */
        @Override
        public Response put(Entity<?> entity) {
            clientRequestContext.setMethod(HttpMethod.PUT);
            // TODO: implement entity support
            return sendRequest(clientRequestContext);
        }

        @Override
        public <T> T put(Entity<?> entity, Class<T> responseType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T put(Entity<?> entity, GenericType<T> responseType) {
            throw new UnsupportedOperationException();
        }


        /**
         * Make a HTTP POST request.
         *
         * @param entity Entity of the request
         * @return Response object
         */
        @Override
        public Response post(Entity<?> entity) {
            clientRequestContext.setMethod(HttpMethod.POST);
            clientRequestContext.setMediaType(entity.getMediaType());
            clientRequestContext.setEntity(entity.getEntity());
            return sendRequest(clientRequestContext);
        }

        @Override
        public <T> T post(Entity<?> entity, Class<T> responseType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T post(Entity<?> entity, GenericType<T> responseType) {
            throw new UnsupportedOperationException();
        }


        /**
         * Make a HTTP DELETE request.
         *
         * @return Response object
         */
        @Override
        public Response delete() {
            clientRequestContext.setMethod(HttpMethod.DELETE);
            return sendRequest(clientRequestContext);
        }

        @Override
        public <T> T delete(Class<T> responseType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T delete(GenericType<T> responseType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Response head() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Response options() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T options(Class<T> responseType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T options(GenericType<T> responseType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Response trace() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T trace(Class<T> responseType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T trace(GenericType<T> responseType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Response method(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T method(String name, Class<T> responseType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T method(String name, GenericType<T> responseType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Response method(String name, Entity<?> entity) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T method(String name, Entity<?> entity, Class<T> responseType) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <T> T method(String name, Entity<?> entity, GenericType<T> responseType) {
            throw new UnsupportedOperationException();
        }

        //TODO: Update the implementation to use carbon-transport client
        private Response sendRequest(ClientRequestContext clientRequestContext) {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            CloseableHttpResponse response = null;
            try {
                HttpRequestBase httpRequestBase = makeRequest(clientRequestContext);
                response = httpclient.execute(httpRequestBase);
                return makeJaxRsResponse(clientRequestContext, response);
            } catch (IOException e) {
                log.error("Error occurred while completing the request", e);
            } finally {
                if (response != null) {
                    try {
                        response.close();
                    } catch (IOException e) {
                        log.warn("Failed to clean resources of the request", e);
                    }
                }
            }
            return null;
        }

        private HttpRequestBase makeRequest(ClientRequestContext clientRequestContext) throws IOException {
            for (Object providerComponent : providerComponents) {
                if (providerComponent instanceof ClientRequestFilter) {
                    ClientRequestFilter clientRequestFilter = (ClientRequestFilter) providerComponent;
                    clientRequestFilter.filter(clientRequestContext);
                }
            }
            HttpRequestBase httpRequestBase;
            String httpMethod = clientRequestContext.getMethod();
            if (HttpMethod.GET.equals(httpMethod)) {
                httpRequestBase = new HttpGet(clientRequestContext.getUri());
            } else if (HttpMethod.POST.equals(httpMethod)) {
                HttpPost httpPost = new HttpPost(clientRequestContext.getUri());
                Object entity = clientRequestContext.getEntity();
                // TODO: implement message body writer support
                Gson gson = new Gson();
                HttpEntity httpEntity = new StringEntity(gson.toJson(entity),
                        ContentType.create(clientRequestContext.getMediaType().toString()));
                httpPost.setEntity(httpEntity);
                httpRequestBase = httpPost;
            } else if (HttpMethod.PUT.equals(httpMethod)) {
                HttpPut httpPut = new HttpPut(clientRequestContext.getUri());
                // TODO: implement message body writer support
                httpRequestBase = httpPut;
            } else if (HttpMethod.DELETE.equals(httpMethod)) {
                httpRequestBase = new HttpDelete(clientRequestContext.getUri());
            } else {
                return null;
            }
            MultivaluedMap<String, Object> headers = clientRequestContext.getHeaders();
            for (Map.Entry<String, List<Object>> entry : headers.entrySet()) {
                List<Object> values = entry.getValue();
                for (Object value : values) {
                    httpRequestBase.setHeader(entry.getKey(), value.toString());
                }
            }
            return httpRequestBase;
        }

        private Response makeJaxRsResponse(ClientRequestContext clientRequestContext, CloseableHttpResponse response)
                throws IOException {
            ClientResponseContext clientResponseContext = new MSF4JClientResponseContext(response);
            for (Object providerComponent : providerComponents) {
                if (providerComponent instanceof ClientResponseFilter) {
                    ClientResponseFilter clientResponseFilter = (ClientResponseFilter) providerComponent;
                    clientResponseFilter.filter(clientRequestContext, clientResponseContext);
                }
            }
            Response.ResponseBuilder responseBuilder = Response.status(clientResponseContext.getStatus());
            for (Map.Entry<String, List<String>> entry : clientResponseContext.getHeaders().entrySet()) {
                List<String> values = entry.getValue();
                for (Object value : values) {
                    responseBuilder.header(entry.getKey(), value.toString());
                }
            }
            if (clientResponseContext.hasEntity()) {
                // TODO: implement message body reader support
                InputStream content = clientResponseContext.getEntityStream();
                ByteArrayOutputStream result = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = content.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                responseBuilder.entity(result.toString(Charset.defaultCharset().displayName()));
            }
            return responseBuilder.build();
        }
    }
}
