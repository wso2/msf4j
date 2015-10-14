/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mss.internal.router;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.wso2.carbon.mss.HttpResponder;
import org.wso2.carbon.mss.internal.router.beanconversion.BeanConversionException;
import org.wso2.carbon.mss.internal.router.beanconversion.BeanConverter;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

/**
 * Handles the return values of the resource methods
 * of JAX-RS resource classes
 */
public class HttpMethodResponseHandler {

    private HttpResponder responder;
    private HttpResponseStatus status = null;
    private String mediaType = null;
    private Object entity;
    private Multimap<String, String> headers = LinkedListMultimap.create();

    /**
     * Set netty-http responder object
     *
     * @param responder
     */
    public HttpMethodResponseHandler setResponder(HttpResponder responder) {
        this.responder = responder;
        return this;
    }

    /**
     * Set response http status code
     *
     * @param status http status code
     */
    public HttpMethodResponseHandler setStatus(int status) {
        this.status = HttpResponseStatus.valueOf(status);
        return this;
    }

    /**
     * Set media type of the entity
     *
     * @param mediaType entity media type
     */
    public HttpMethodResponseHandler setMediaType(String mediaType) {
        this.mediaType = mediaType;
        return this;
    }

    /**
     * Set entity body fro the response. If the entity is
     * type of javax.ws.rs.core.Response extract entity,
     * status code etc from it
     *
     * @param entity
     */
    public HttpMethodResponseHandler setEntity(Object entity) {
        if (entity instanceof Response) {
            Response response = (Response) entity;
            this.entity = response.getEntity();
            MultivaluedMap<String, String> multivaluedMap = response.getStringHeaders();
            if (multivaluedMap != null) {
                multivaluedMap.forEach((key, strings) -> {
                    headers.putAll(key, strings);
                });
            }
            setStatus(response.getStatus());
            if (response.getMediaType() != null) {
                setMediaType(response.getMediaType().toString());
            }
        } else {
            this.entity = entity;
        }
        return this;
    }

    /**
     * send response using netty-http provided responder
     */
    public void send() throws BeanConversionException {
        HttpResponseStatus status;
        if (this.status != null) {
            status = this.status;
        } else if (entity != null) {
            status = HttpResponseStatus.OK;
        } else {
            status = HttpResponseStatus.NO_CONTENT;
        }
        Object entityToSend;
        if (entity != null) {
            if (mediaType != null) {
                headers.put(HttpHeaders.Names.CONTENT_TYPE, mediaType);
                entityToSend = BeanConverter.instance(mediaType)
                        .toMedia(entity);
            } else {
                entityToSend = entity;
            }
            responder.sendString(status, String.valueOf(entityToSend), headers);
        } else {
            responder.sendStatus(status, headers);
        }
    }
}
