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

package org.wso2.msf4j.entitywriter;

import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Interface that provides the capability of writing an entity type to
 * a carbon message.
 *
 * @param <T> type of the entity
 */
public interface EntityWriter<T> {

    String CHUNKED = "chunked";

    /**
     * Provide supported entity type.
     *
     * @return entity type
     */
    Class<T> getType();

    /**
     * Write the entity object to the carbon message by considering the
     * provided chunk size and media type.
     *
     * @param entity    object
     * @param mediaType user defined media type
     * @param chunkSize user defined chunk size
     *                  0 to signify none chunked response
     *                  -1 to signify default chunk size of the EntityWriter
     */
    void writeData(CarbonMessage carbonMessage, T entity, String mediaType, int chunkSize);

}
