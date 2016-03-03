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

package org.wso2.msf4j.internal.router.beanconversion;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Media type converter for text/plain mime type.
 */
public class TextPlainConverter extends MediaTypeConverter {

    @Override
    public String[] getSupportedMediaTypes() {
        return null;
    }

    @Override
    public ByteBuffer toMedia(Object object) {
        return ByteBuffer.wrap(object.toString().getBytes(Charset.defaultCharset()));
    }

    @Override
    public Object toObject(ByteBuffer content, Type targetType) {
        return Charset.defaultCharset().decode(content).toString();
    }
}
