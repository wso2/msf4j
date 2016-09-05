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
package org.wso2.msf4j.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * This class represents default values of primitive data types.
 */
public class Defaults {
    private Defaults() {
    }

    private static final Map<Class<?>, Object> DEFAULTS = new HashMap<>(8);

    static {
        // Only add to this map via put(Map, Class<T>, T)
        DEFAULTS.put(boolean.class, false);
        DEFAULTS.put(char.class, '\0');
        DEFAULTS.put(byte.class, (byte) 0);
        DEFAULTS.put(short.class, (short) 0);
        DEFAULTS.put(int.class, 0);
        DEFAULTS.put(long.class, 0L);
        DEFAULTS.put(float.class, 0f);
        DEFAULTS.put(double.class, 0d);
    }

    /**
     * Returns the default value of {@code type} as defined by JLS --- {@code 0} for numbers, {@code
     * false} for {@code boolean} and {@code '\0'} for {@code char}. For non-primitive types and
     * {@code void}, {@code null} is returned.
     *
     * @param type class of the value.
     * @return default value of given class.
     */
    @Nullable
    public static <T> T defaultValue(Class<T> type) {
        // Primitives.wrap(type).cast(...) would avoid the warning, but we can't use that from here
        @SuppressWarnings("unchecked") // the put method enforces this key-value relationship
                T t = (T) DEFAULTS.get(Objects.requireNonNull(type));
        return t;
    }
}
