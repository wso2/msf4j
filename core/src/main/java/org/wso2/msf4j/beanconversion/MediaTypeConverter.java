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

package org.wso2.msf4j.beanconversion;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;

/**
 * Interface of media type conversion classes.
 */
public abstract class MediaTypeConverter {

    /**
     * Convert an object to a specific media type.
     *
     * @param object object that needs to be converted to a media content
     * @return converted media content
     * @throws BeanConversionException throws if conversion is failed
     */
    public ByteBuffer convertToMedia(Object object) throws BeanConversionException {
        if (object == null) {
            throw new BeanConversionException("Object cannot be null");
        }
        return toMedia(object);
    }

    /**
     * Create an object from a specific content.
     *
     * @param content    content that needs to be converted to an object
     * @param targetType media type of the content
     * @return created object
     * @throws BeanConversionException throws if object creation is failed
     */
    public Object convertToObject(ByteBuffer content, Type targetType) throws BeanConversionException {
        if (content == null || targetType == null) {
            throw new BeanConversionException("Content or target type cannot be null");
        }
        return toObject(content, targetType);
    }

    /**
     * Return an array of supported media types.
     *
     * @return String array of media types
     */
    public abstract String[] getSupportedMediaTypes();

    /**
     * Convert an object to a specific media type.
     *
     * @param object object that needs to be converted to a media content
     * @return converted media content
     * @throws BeanConversionException throws if conversion is failed
     */
    protected abstract ByteBuffer toMedia(Object object) throws BeanConversionException;

    /**
     * Create an object from a specific content.
     *
     * @param content    content that needs to be converted to an object
     * @param targetType media type of the content
     * @return created object
     * @throws BeanConversionException throws if object creation is failed
     */
    protected abstract Object toObject(ByteBuffer content, Type targetType) throws BeanConversionException;

}
