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

package org.wso2.msf4j.internal.entitywriter;

import org.wso2.msf4j.internal.ClassComparator;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Registry that stores entity writers for different entity types.
 */
public class EntityWriterRegistry {

    private static final EntityWriter DEFAULT_ENTITY_WRITER = new ObjectEntityWriter();
    private static final Map<Class, EntityWriter> writers = new TreeMap<>(new ClassComparator());

    static {
        registerEntityWriter(new FileEntityWriter());
        registerEntityWriter(new InputStreamEntityWriter());
        registerEntityWriter(new StreamingOutputEntityWriter());
    }

    private EntityWriterRegistry() {
    }

    /**
     * Register an entity writer.
     *
     * @param entityWriter entity writer for a specific entity type
     */
    private static void registerEntityWriter(EntityWriter entityWriter) {
        writers.put(entityWriter.getType(), entityWriter);
    }

    /**
     * Get entity writer for a given type.
     *
     * @param type type of the entity to be written to a carbon message
     * @return entity writer
     */
    public static EntityWriter getEntityWriter(Class type) {
        return writers.entrySet().
                stream().
                filter(entry -> entry.getKey().isAssignableFrom(type)).
                findFirst().
                flatMap(entry -> Optional.ofNullable(entry.getValue())).orElse(DEFAULT_ENTITY_WRITER);
    }
}
