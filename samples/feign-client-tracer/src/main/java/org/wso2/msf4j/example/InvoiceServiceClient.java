/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.msf4j.example;

import feign.Feign;
import feign.RequestInterceptor;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.slf4j.Slf4jLogger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static java.util.Arrays.asList;

class InvoiceServiceClient {
    private static final Log log = LogFactory.getLog(InvoiceServiceClient.class);

    /**
     * The generalized version of the method that allows more in-depth customizations via
     * {@link RequestInterceptor}s.
     *
     * @param endpoint URL of service
     */
    public static InvoiceServiceAPI getInstance(String endpoint, RequestInterceptor... interceptors) {
        Feign.Builder b = Feign.builder()
                .encoder(new GsonEncoder(ModelUtils.GSON))
                .logger(new Slf4jLogger())
                .decoder(new GsonDecoder(ModelUtils.GSON))
                .errorDecoder(new ClientErrorDecoder());
        if (interceptors != null) {
            b.requestInterceptors(asList(interceptors));
        }
        return b.target(InvoiceServiceAPI.class, endpoint);
    }

    public static InvoiceServiceAPI getInstanceWithAnalytics(String serviceEndpoint) {
        Feign.Builder b = Feign.builder()
                .client(new FeignTracingClient("Invoice-Service-Client"))
                .encoder(new GsonEncoder(ModelUtils.GSON))
                .logger(new Slf4jLogger())
                .decoder(new GsonDecoder(ModelUtils.GSON))
                .errorDecoder(new ClientErrorDecoder());
        return b.target(InvoiceServiceAPI.class, serviceEndpoint);
    }

    public static InvoiceServiceAPI getInstanceWithAnalytics(String serviceEndpoint, String analyticsEndpoint) {
        Feign.Builder b = Feign.builder()
                .client(new FeignTracingClient("Invoice-Service-Client", analyticsEndpoint))
                .encoder(new GsonEncoder(ModelUtils.GSON))
                .logger(new Slf4jLogger())
                .decoder(new GsonDecoder(ModelUtils.GSON))
                .errorDecoder(new ClientErrorDecoder());
        return b.target(InvoiceServiceAPI.class, serviceEndpoint);
    }
}
