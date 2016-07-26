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

import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.Locale;


/**
 * Implementation class of JAX-RS request invocation builder.
 */
public class MSF4JInvocationBuilder implements Invocation.Builder {

    @Override
    public Invocation build(String method) {
        return null;
    }

    @Override
    public Invocation build(String method, Entity<?> entity) {
        return null;
    }

    @Override
    public Invocation buildGet() {
        return null;
    }

    @Override
    public Invocation buildDelete() {
        return null;
    }

    @Override
    public Invocation buildPost(Entity<?> entity) {
        return null;
    }

    @Override
    public Invocation buildPut(Entity<?> entity) {
        return null;
    }

    @Override
    public AsyncInvoker async() {
        return null;
    }

    @Override
    public Invocation.Builder accept(String... mediaTypes) {
        return null;
    }

    @Override
    public Invocation.Builder accept(MediaType... mediaTypes) {
        return null;
    }

    @Override
    public Invocation.Builder acceptLanguage(Locale... locales) {
        return null;
    }

    @Override
    public Invocation.Builder acceptLanguage(String... locales) {
        return null;
    }

    @Override
    public Invocation.Builder acceptEncoding(String... encodings) {
        return null;
    }

    @Override
    public Invocation.Builder cookie(Cookie cookie) {
        return null;
    }

    @Override
    public Invocation.Builder cookie(String name, String value) {
        return null;
    }

    @Override
    public Invocation.Builder cacheControl(CacheControl cacheControl) {
        return null;
    }

    @Override
    public Invocation.Builder header(String name, Object value) {
        return null;
    }

    @Override
    public Invocation.Builder headers(MultivaluedMap<String, Object> headers) {
        return null;
    }

    @Override
    public Invocation.Builder property(String name, Object value) {
        return null;
    }

    @Override
    public Response get() {
        return null;
    }

    @Override
    public <T> T get(Class<T> responseType) {
        return null;
    }

    @Override
    public <T> T get(GenericType<T> responseType) {
        return null;
    }

    @Override
    public Response put(Entity<?> entity) {
        return null;
    }

    @Override
    public <T> T put(Entity<?> entity, Class<T> responseType) {
        return null;
    }

    @Override
    public <T> T put(Entity<?> entity, GenericType<T> responseType) {
        return null;
    }

    @Override
    public Response post(Entity<?> entity) {
        return null;
    }

    @Override
    public <T> T post(Entity<?> entity, Class<T> responseType) {
        return null;
    }

    @Override
    public <T> T post(Entity<?> entity, GenericType<T> responseType) {
        return null;
    }

    @Override
    public Response delete() {
        return null;
    }

    @Override
    public <T> T delete(Class<T> responseType) {
        return null;
    }

    @Override
    public <T> T delete(GenericType<T> responseType) {
        return null;
    }

    @Override
    public Response head() {
        return null;
    }

    @Override
    public Response options() {
        return null;
    }

    @Override
    public <T> T options(Class<T> responseType) {
        return null;
    }

    @Override
    public <T> T options(GenericType<T> responseType) {
        return null;
    }

    @Override
    public Response trace() {
        return null;
    }

    @Override
    public <T> T trace(Class<T> responseType) {
        return null;
    }

    @Override
    public <T> T trace(GenericType<T> responseType) {
        return null;
    }

    @Override
    public Response method(String name) {
        return null;
    }

    @Override
    public <T> T method(String name, Class<T> responseType) {
        return null;
    }

    @Override
    public <T> T method(String name, GenericType<T> responseType) {
        return null;
    }

    @Override
    public Response method(String name, Entity<?> entity) {
        return null;
    }

    @Override
    public <T> T method(String name, Entity<?> entity, Class<T> responseType) {
        return null;
    }

    @Override
    public <T> T method(String name, Entity<?> entity, GenericType<T> responseType) {
        return null;
    }
}
