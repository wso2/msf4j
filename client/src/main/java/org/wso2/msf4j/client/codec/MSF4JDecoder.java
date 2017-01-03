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
package org.wso2.msf4j.client.codec;

import feign.FeignException;
import feign.Response;
import feign.codec.Decoder;
import feign.codec.StringDecoder;
import feign.gson.GsonDecoder;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import javax.ws.rs.core.MediaType;

/**
 * Decoder for fegin client.
 */
public class MSF4JDecoder implements Decoder {

    private GsonDecoder gsonDecoder = new GsonDecoder();
    private StringDecoder stringDecoder = new StringDecoder();
    private static final String CONTENT_TYPE = "Content-Type";

    @Override
    public Object decode(Response response, Type type) throws IOException, FeignException {
        Collection<String> contentTypeHeaders = response.headers().get(CONTENT_TYPE);
        String responseContentType =
                contentTypeHeaders != null ? contentTypeHeaders.iterator().next() : MediaType.TEXT_PLAIN;

        if (responseContentType.equals(MediaType.APPLICATION_JSON)) {
            return gsonDecoder.decode(response, type);
        } else if (responseContentType.equals(MediaType.TEXT_PLAIN) || responseContentType.equals(MediaType.WILDCARD)) {
            return stringDecoder.decode(response, type);
        }
        throw new RuntimeException("Unsupported Content Type " + responseContentType);
    }
}
