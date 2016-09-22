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

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class.
 */
public class Utils {

    /**
     * Return the String representation of given object.
     *
     * @param object Object to be reflected.
     * @return String representation of given object.
     */
    public static String toString(Object object) {
        Objects.requireNonNull(object);
        StringBuilder sb = new StringBuilder();
        try {
            for (Field field : object.getClass().getFields()) {
                sb.append(field.getName()).append(":").append(field.get(object)).append("\n");
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error while executing " + object.getClass() + ".toString()", e);
        }
        return sb.toString();
    }

    /**
     * Return the String representation of given object's given fields list.
     *
     * @param object Object to be reflected.
     * @return String representation of given object.
     */
    public static String toString(Object object, String[] fields) {
        Objects.requireNonNull(object);
        StringBuilder sb = new StringBuilder();

        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            @Override
            public Object run() {
                try {
                    for (String field : fields) {
                        Objects.requireNonNull(field);
                        Field declaredField = object.getClass().getDeclaredField(field);
                        declaredField.setAccessible(true);
                        sb.append(field).append(":").append(declaredField.get(object)).append("\n");
                        declaredField.setAccessible(false);
                    }
                    return sb;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error while executing " + object.getClass() + ".toString()", e);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException("Error while executing " + object.getClass() + ".toString()", e);
                }
            }
        });
        return sb.toString();
    }

    /**
     * Split the given sequence with the given delimiter and return list of values.
     *
     * @param sequence  String need to be splitted.
     * @param delimiter String delimiter
     * @param omitEmpty boolean need to skip empty values.
     * @return List of values obtained by splitting.
     */
    public static List<String> split(String sequence, String delimiter, boolean omitEmpty) {
        Objects.requireNonNull(sequence);
        Objects.requireNonNull(delimiter);
        String[] splittedValues = sequence.split(delimiter);
        List<String> values = Arrays.asList(splittedValues);
        return omitEmpty ? values.stream().filter(value -> !value.isEmpty()).collect(Collectors.toList()) : values;
    }

    /**
     * Get the remaining count of intersection between 2 sets.
     *
     * @param set1 First set
     * @param set2 Second set
     * @return remaining count of intersection between 2 sets
     */
    public static int getIntersection(Set set1, Set set2) {
        HashSet cloneSet1 = new HashSet<>(set1);
        HashSet cloneSet2 = new HashSet<>(set2);
        if (cloneSet1.size() > cloneSet2.size()) {
            cloneSet1.retainAll(cloneSet2);
            return cloneSet1.size();
        }
        cloneSet2.retainAll(cloneSet1);
        return cloneSet2.size();
    }
}
