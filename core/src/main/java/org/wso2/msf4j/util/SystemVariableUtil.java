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
package org.wso2.msf4j.util;


import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * A utility which allows reading variables from the environment or System properties.
 * If the variable in available in the environment as well as a System property, the System property takes
 * precedence.
 */
public class SystemVariableUtil {

    private static final String VARIABLE_PREFIX = "CUSTOM_";

    public static String getValue(String variableName, String defaultValue) {
        String value;
        if (System.getProperty(variableName) != null) {
            value = System.getProperty(variableName);
        } else if (System.getenv(variableName) != null) {
            value = System.getenv(variableName);
        } else {
            value = defaultValue;
        }
        return value;
    }

    public static Map<String, String> getArbitraryAttributes() {

        Map<String, String> arbitraryAttributes = new HashMap<>();

        Map<String, String> environmentVariables = System.getenv();
        environmentVariables.keySet().stream().filter(key -> key.startsWith(VARIABLE_PREFIX)).forEach(key -> {
            arbitraryAttributes.put(key, environmentVariables.get(key));
        });

        Properties properties = System.getProperties();
        properties.keySet().stream().filter(key -> ((String) key).startsWith(VARIABLE_PREFIX)).forEach((Object key) -> {
            arbitraryAttributes.put((String) key, properties.getProperty((String) key));
        });

        return arbitraryAttributes;
    }
}
