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

/**
 * Factory class for getting correct media type conversion
 * instance for a given mime type.
 */
public class BeanConverter {

    public static MediaTypeConverter instance(String mediaType) throws BeanConversionException {
        if (mediaType.equalsIgnoreCase("text/json")
                || mediaType.equalsIgnoreCase("application/json")) {
            return new JsonConverter();
        } else if (mediaType.equalsIgnoreCase("text/xml")) {
            return new XmlConverter();
        } else {
            return new TextPlainConverter();
        }
    }
}
