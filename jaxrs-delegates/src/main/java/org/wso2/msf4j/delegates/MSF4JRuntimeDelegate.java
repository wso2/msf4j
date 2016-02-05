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

package org.wso2.msf4j.delegates;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Variant.VariantListBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 * <p>
 * Implementation of the RuntimeDelegate which is
 * required for the javax.ws.rs.core.Response.
 * <p>
 * This class will be loaded by javax.ws.rs.ext.RuntimeDelegate.
 */
public class MSF4JRuntimeDelegate extends RuntimeDelegate {

    protected Map<Class<?>, HeaderDelegate<?>> headerProviders = new HashMap<>();

    public MSF4JRuntimeDelegate() {
        headerProviders.put(MediaType.class, new MediaTypeHeaderProvider());
    }

    @Override
    public UriBuilder createUriBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ResponseBuilder createResponseBuilder() {
        return new MSF4JResponse.Builder();
    }

    @Override
    public VariantListBuilder createVariantListBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T createEndpoint(Application application, Class<T> endpointType)
            throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type) throws IllegalArgumentException {
        if (type == null) {
            throw new IllegalArgumentException("HeaderDelegate type is null");
        }
        return (HeaderDelegate<T>) headerProviders.get(type);
    }

    @Override
    public Link.Builder createLinkBuilder() {
        throw new UnsupportedOperationException();
    }
}
