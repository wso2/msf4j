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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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

    @Override
    public String[] getSupportedMediaTypes() {
        return new String[]{MediaType.APPLICATION_JSON, TEXT_JSON};
    }

    @Override
    public ByteBuffer toMedia(Object object) {
        return ByteBuffer.wrap(gson.toJson(object).getBytes(Charset.defaultCharset()));
    }

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
}
