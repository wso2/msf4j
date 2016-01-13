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

package org.wso2.carbon.mss.internal.router.beanconversion;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

/**
 * Media type converter for text/json,
 * application/json mime types.
 */
public class JsonConverter implements MediaTypeConverter {

    private static final Gson gson = new Gson();

    @Override
    public Object toMedia(Object object) {
        return gson.toJson(object);
    }

    @Override
    public Object toObject(String content, Type targetType) throws BeanConversionException {
        Object object;
        try {
            object = gson.fromJson(content, targetType);
            if (object == null) {
                throw new BeanConversionException("Unable to perform json to object conversion");
            }
        } catch (JsonSyntaxException ex) {
            throw new BeanConversionException("Unable to perform json to object conversion", ex);
        }
        return gson.fromJson(content, targetType);
    }
}
