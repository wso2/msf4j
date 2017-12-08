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

import org.apache.commons.io.IOUtils;
import org.wso2.msf4j.beanconversion.BeanConversionException;
import org.wso2.msf4j.beanconversion.MediaTypeConverter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Media type converter for text/plain mime type. This will be
 * applied by default for unsupported media types.
 */
public class TextPlainConverter extends MediaTypeConverter {

    /**
     * Provides the supported media types for bean conversions.
     */
    @Override
    public String[] getSupportedMediaTypes() {
        return new String[0];
    }

    /**
     * Convert an Object to a text ByteBuffer.
     *
     * @param object object that needs to be converted to a media content
     * @return Byte buffer that contains the text
     */
    @Override
    public ByteBuffer toMedia(Object object) {
        return ByteBuffer.wrap(object.toString().getBytes(Charset.defaultCharset()));
    }

    /**
     * Convert a text ByteBuffer content to an object.
     *
     * @param content    content that needs to be converted to an object
     * @param targetType media type of the content
     * @return String object that contains the text data
     */
    @Override
    public Object toObject(ByteBuffer content, Type targetType) {
        return Charset.defaultCharset().decode(content).toString();
    }

    @Override
    protected Object toObject(InputStream inputStream, Type targetType) throws BeanConversionException {
        try {
            return IOUtils.toString(inputStream, UTF_8_CHARSET);
        } catch (IOException e) {
            throw new BeanConversionException("Unable to perform string conversion", e);
        }
    }
}
