/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.mss.internal.router.beanconversion;

import java.lang.reflect.Type;

/**
 * Media type converter for text/xml mime type
 */
//TODO: implement methods
public class XmlConverter implements MediaTypeConverter {

    @Override
    public Object toMedia(Object object) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object toObject(String content, Type targetType) {
        throw new UnsupportedOperationException();
    }
}
