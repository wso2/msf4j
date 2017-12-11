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

package org.wso2.msf4j.internal.beanconversion;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.wso2.msf4j.beanconversion.BeanConversionException;
import org.wso2.msf4j.beanconversion.MediaTypeConverter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import javax.ws.rs.core.MediaType;

/**
 * Media type converter for text/json,
 * application/json mime types.
 */
public class JsonConverter extends MediaTypeConverter {

    private static final Gson gson = new Gson();
    private static final String TEXT_JSON = "text/json";

    /**
     * Provides the supported media types for bean conversions.
     */
    @Override
    public String[] getSupportedMediaTypes() {
        return new String[]{MediaType.APPLICATION_JSON, TEXT_JSON};
    }

    /**
     * Convert an Object to a Json encoded byte buffer.
     *
     * @param object object that needs to be converted to a media content
     * @return Json encoded byte buffer
     */
    @Override
    public ByteBuffer toMedia(Object object) {
        String value = (object instanceof String || object instanceof JsonArray || object instanceof JsonObject) ?
                       object.toString() : gson.toJson(object);
        return ByteBuffer.wrap(value.getBytes(Charset.defaultCharset()));
    }

    /**
     * Convert a Json ByteBuffer content to an object.
     *
     * @param content    content that needs to be converted to an object
     * @param targetType media type of the content
     * @return Object that maps the Json data
     * @throws BeanConversionException if error occure while converting the content
     */
    @Override
    public Object toObject(ByteBuffer content, Type targetType) throws BeanConversionException {
        try {
            String str = Charset.defaultCharset().decode(content).toString();
            Object object = gson.fromJson(str, targetType);
            if (object == null) {
                throw new BeanConversionException("Unable to perform json to object conversion");
            }
            return object;
        } catch (JsonSyntaxException ex) {
            throw new BeanConversionException("Unable to perform json to object conversion", ex);
        }
    }

    @Override
    protected Object toObject(InputStream inputStream, Type targetType) throws BeanConversionException {
        try {
            Reader reader = new InputStreamReader(inputStream, UTF_8_CHARSET);
            Object object = gson.fromJson(reader, targetType);
            if (object == null) {
                throw new BeanConversionException("Unable to perform json to object conversion");
            }
            return object;
        } catch (UnsupportedEncodingException ex) {
            throw new BeanConversionException("Unable to perform json to object conversion", ex);
        }
    }

}
