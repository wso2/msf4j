/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.msf4j.delegates;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.RuntimeDelegate.HeaderDelegate;

/**
 * Provides conversions from MediaTypes to String and String to MediaType.
 */
public class MediaTypeHeaderProvider implements HeaderDelegate<MediaType> {

    /**
     * Convert a string media type to a MediaType object.
     *
     * @param mType media type string
     * @return MediaType object
     */
    public MediaType fromString(String mType) {
        if (mType == null) {
            throw new IllegalArgumentException("Media type value can not be null");
        }

        int i = mType.indexOf('/');
        if (i == -1) {
            throw new UnsupportedOperationException("Media types without subtype is not supported");
        }

        int paramsStart = mType.indexOf(';', i + 1);
        int end = paramsStart == -1 ? mType.length() : paramsStart;

        String type = mType.substring(0, i);
        String subtype = mType.substring(i + 1, end);

        return new MediaType(type.trim().toLowerCase(),
                subtype.trim().toLowerCase());
    }

    /**
     * Convert a media type object to a string.
     *
     * @param type MediaType object
     * @return string media type
     */
    public String toString(MediaType type) {
        if (type == null) {
            throw new IllegalArgumentException("MediaType can not be null");
        }
        return type.getType() + '/' + type.getSubtype();
    }
}
