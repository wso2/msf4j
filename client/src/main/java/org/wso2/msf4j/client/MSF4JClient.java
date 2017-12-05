/*
 *  Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.msf4j.client;

import feign.Client;
import feign.Feign;
import feign.RequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.hystrix.FallbackFactory;
import feign.hystrix.HystrixFeign;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.utils.StringUtils;
import org.wso2.msf4j.analytics.common.tracing.TracingConstants;
import org.wso2.msf4j.client.codec.DefaultErrorDecoder;
import org.wso2.msf4j.client.codec.RestErrorResponseMapper;
import org.wso2.msf4j.client.exception.RestServiceException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

/**
 * MSF4J client.
 *
 * @param <T> REST service API interface
 */
public class MSF4JClient<T> {
    private static final Logger log = LoggerFactory.getLogger(MSF4JClient.class);
    private final T api;

    public MSF4JClient(T api) {
        this.api = api;
    }

    public T api() {
        return api;
    }

    /**
     * MSF4J client builder
     *
     * @param <T> API interface
     */
    public static class Builder<T> {

        private final List<RequestInterceptor> requestInterceptors = new ArrayList<RequestInterceptor>();
        private final Map<String, Class<? extends RestServiceException>> errorCodeExceptionMap = new HashMap<>();
        private FallbackFactory<? extends T> fallbackFactory;
        private boolean enableCircuitBreaker;
        private boolean enableTracing;
        private String instanceName;
        private String analyticsEndpoint;
        private String serviceEndpoint;
        private SSLContext sslContext;
        private HostnameVerifier hostnameVerifier;
        private boolean decode404;
        private Class<T> apiClass;
        private ErrorDecoder errorDecoder = new DefaultErrorDecoder(errorCodeExceptionMap);
        private Encoder encoder = new GsonEncoder(ModelUtils.GSON);
        private Decoder decoder = new GsonDecoder(ModelUtils.GSON);
        private TracingConstants.TracingType tracingType = TracingConstants.TracingType.DAS;

        public Feign.Builder newFeignClientBuilder() {
            return Feign.builder()
                    .encoder(encoder)
                    .decoder(decoder);
        }

        public HystrixFeign.Builder newHystrixFeignClientBuilder() {
            return HystrixFeign.builder()
                    .encoder(encoder)
                    .decoder(decoder);
        }

        /**
         * Adds a single request interceptor to the builder.
         */
        public MSF4JClient.Builder<T> requestInterceptor(RequestInterceptor requestInterceptor) {
            this.requestInterceptors.add(requestInterceptor);
            return this;
        }

        /**
         * Sets the full set of request interceptors for the builder, overwriting any previous
         * interceptors.
         */
        public MSF4JClient.Builder<T> requestInterceptors(Iterable<RequestInterceptor> requestInterceptors) {
            this.requestInterceptors.clear();
            for (RequestInterceptor requestInterceptor : requestInterceptors) {
                this.requestInterceptors.add(requestInterceptor);
            }
            return this;
        }

        /**
         * Sets the fallback factory for HystrixFeign client which supports circuit breaker
         */
        public MSF4JClient.Builder<T> fallbackFactory(FallbackFactory<? extends T> fallbackFactory) {
            this.fallbackFactory = fallbackFactory;
            return this;
        }

        public MSF4JClient.Builder<T> enableCircuitBreaker() {
            this.enableCircuitBreaker = true;
            return this;
        }

        public MSF4JClient.Builder<T> enableTracing() {
            this.enableTracing = true;
            return this;
        }

        public MSF4JClient.Builder<T> instanceName(String instanceName) {
            this.instanceName = instanceName;
            return this;
        }

        public MSF4JClient.Builder<T> analyticsEndpoint(String analyticsEndpoint) {
            this.analyticsEndpoint = analyticsEndpoint;
            return this;
        }

        public MSF4JClient.Builder<T> serviceEndpoint(String serviceEndpoint) {
            this.serviceEndpoint = serviceEndpoint;
            return this;
        }

        public MSF4JClient.Builder<T> apiClass(Class<T> apiClass) {
            this.apiClass = apiClass;
            return this;
        }

        public MSF4JClient.Builder<T> decode404(boolean decode404) {
            this.decode404 = decode404;
            return this;
        }

        public MSF4JClient.Builder<T> encoder(Encoder encoder) {
            this.encoder = encoder;
            return this;
        }

        public MSF4JClient.Builder<T> decoder(Decoder decoder) {
            this.decoder = decoder;
            return this;
        }

        public MSF4JClient.Builder<T> tracingType(TracingConstants.TracingType tracingType) {
            this.tracingType = tracingType;
            return this;
        }

        public MSF4JClient.Builder<T> addErrorResponseMapper(RestErrorResponseMapper... responseMappers) {
            Arrays.stream(responseMappers).forEach(rm -> {
                Arrays.stream(rm.getClass().getMethods()).
                        filter(method -> !StringUtils.isNullOrEmptyAfterTrim(rm.getExceptionKey())).
                        findAny().
                        ifPresent(method -> {
                            if (errorCodeExceptionMap.containsKey(rm.getExceptionKey())) {
                                log.warn("RestErrorResponseMapper has already been added for the given exception key " +
                                        "'{}'", rm.getExceptionKey());
                            }
                            errorCodeExceptionMap.put(rm.getExceptionKey(), rm.getExceptionClass());
                        });
            });
            return this;
        }

        public MSF4JClient.Builder<T> errorDecoder(ErrorDecoder errorDecoder) {
            this.errorDecoder = errorDecoder;
            return this;
        }

        public HostnameVerifier getHostnameVerifier() {
            return hostnameVerifier;
        }

        public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
            this.hostnameVerifier = hostnameVerifier;
        }

        public SSLContext getSslContext() {
            return sslContext;
        }

        public void setSslContext(SSLContext sslContext) {
            this.sslContext = sslContext;
        }

        public MSF4JClient<T> build() {
            MSF4JClient<T> msf4JClient;
            Client client;

            PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
            cm.setMaxTotal(200);
            cm.setDefaultMaxPerRoute(20);

            CloseableHttpClient apacheHttpClient = HttpClients.custom()
                                                              .setSSLContext(sslContext)
                                                              .setSSLHostnameVerifier(hostnameVerifier)
                                                              .setConnectionManager(cm)
                                                              .build();

            if (enableTracing) {
                if (tracingType == TracingConstants.TracingType.ZIPKIN) {
                    client = new FeignClientWrapper(
                            new FeginZipkinTracingClient(new ApacheHttpClient(apacheHttpClient), instanceName,
                                                         analyticsEndpoint));
                } else {
                    client = new FeignClientWrapper(
                            new FeignTracingClient(new ApacheHttpClient(apacheHttpClient), instanceName,
                                                   analyticsEndpoint));
                }
            } else {
                client = new FeignClientWrapper(new ApacheHttpClient(apacheHttpClient));
            }

            if (enableCircuitBreaker) {
                HystrixFeign.Builder builder = newHystrixFeignClientBuilder();
                builder.client(client);
                builder.requestInterceptors(requestInterceptors);
                builder.errorDecoder(errorDecoder);
                if (decode404) {
                    builder.decode404();
                }
                msf4JClient = new MSF4JClient<T>(builder.target(apiClass, serviceEndpoint, fallbackFactory));
            } else {
                Feign.Builder builder = newFeignClientBuilder();
                builder.client(client);
                builder.requestInterceptors(requestInterceptors);
                builder.errorDecoder(errorDecoder);
                if (decode404) {
                    builder.decode404();
                }
                msf4JClient = new MSF4JClient<T>(builder.target(apiClass, serviceEndpoint));
            }
            return msf4JClient;
        }
    }
}
