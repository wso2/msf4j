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

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for getting correct media type conversion
 * instance for a given mime type.
 */
public class BeanConverter {

    private static final MediaTypeConverter DEFAULT_CONVERTER = new TextPlainConverter();
    private static final BeanConverter INSTANCE = new BeanConverter();

    private final Map<String, MediaTypeConverter> converterMap = new HashMap<>();

    public BeanConverter() {
        addMediaTypeConverter(new JsonConverter());
        addMediaTypeConverter(new XmlConverter());
    }

    /**
     * Get a media type converter for a given media type string.
     *
     * @param mediaType media type String
     * @return MediaTypeConverter
     */
    public MediaTypeConverter getConverter(String mediaType) {
        MediaTypeConverter mediaTypeConverter = converterMap.get(mediaType.toLowerCase());
        if (mediaTypeConverter == null) {
            mediaTypeConverter = DEFAULT_CONVERTER;
        }
        return mediaTypeConverter;
    }

    /**
     * Register a media type converter.
     */
    public void addMediaTypeConverter(MediaTypeConverter mediaTypeConverter) {
        for (String mediaType : mediaTypeConverter.getSupportedMediaTypes()) {
            converterMap.put(mediaType.toLowerCase(), mediaTypeConverter);
        }
    }

    /**
     * Get BeanConverter singleton.
     */
    public static BeanConverter getInstance() {
        return INSTANCE;
    }
}
