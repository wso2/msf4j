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
package org.wso2.msf4j.spring.property;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * ApplicationContextInitializer class used to load environment properties from YAML files.
 */
public class YamlFileApplicationContextInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String YAML_CONFIG_FILE_NAME = "application.yml";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Resource resource = applicationContext.getResource("classpath:" + YAML_CONFIG_FILE_NAME);
        if (!resource.exists()) {
            resource = applicationContext.getResource("file:" + YAML_CONFIG_FILE_NAME);
        }

        if (resource.exists()) {
            List<Properties> applicationYmlProperties = new ArrayList<>();
            String[] activeProfileNames = null;
            try (InputStream input = resource.getInputStream()) {
                Yaml yml = new Yaml(new SafeConstructor());
                Iterable<Object> objects = yml.loadAll(input);
                for (Object obj : objects) {
                    Map<String, Object> flattenedMap = getFlattenedMap(asMap(obj));
                    Properties properties = new Properties();
                    properties.putAll(flattenedMap);
                    Object activeProfile = properties.get("spring.profiles.active");
                    if (activeProfile != null) {
                        activeProfileNames = activeProfile.toString().split(",");
                    }
                    applicationYmlProperties.add(properties);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Couldn't find " + YAML_CONFIG_FILE_NAME, e);
            } catch (IOException e) {
                throw new RuntimeException("Error while reading " + YAML_CONFIG_FILE_NAME, e);
            }

            if (activeProfileNames == null) {
                activeProfileNames = applicationContext.getEnvironment().getActiveProfiles();
            }

            for (Properties properties : applicationYmlProperties) {
                String profile = properties.getProperty("spring.profiles");
                PropertySource<?> propertySource;
                if (profile == null) {
                    propertySource = new MapPropertySource(YAML_CONFIG_FILE_NAME, new HashMap(properties));
                    applicationContext.getEnvironment().getPropertySources().addLast(propertySource);
                } else if (activeProfileNames != null && ("default".equals(profile) ||
                                                          (activeProfileNames.length == 1 &&
                                                           activeProfileNames[0].equals(profile)))) {
                    propertySource =
                            new MapPropertySource(YAML_CONFIG_FILE_NAME + "[" + profile + "]", new HashMap(properties));
                    applicationContext.getEnvironment().getPropertySources()
                                      .addAfter("systemEnvironment", propertySource);
                }
                activeProfileNames = applicationContext.getEnvironment().getActiveProfiles();
            }
        }
        applicationContext.getEnvironment().getActiveProfiles();
    }

    private Map<String, Object> asMap(Object object) {
        // YAML can have numbers as keys
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        if (!(object instanceof Map)) {
            // A document can be a text literal
            result.put("document", object);
            return result;
        }

        Map<Object, Object> map = (Map<Object, Object>) object;
        for (Map.Entry<Object, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Map) {
                value = asMap(value);
            }
            Object key = entry.getKey();
            if (key instanceof CharSequence) {
                result.put(key.toString(), value);
            } else {
                // It has to be a map key in this case
                result.put("[" + key.toString() + "]", value);
            }
        }
        return result;
    }

    private final Map<String, Object> getFlattenedMap(Map<String, Object> source) {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        buildFlattenedMap(result, source, null);
        return result;
    }

    private void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, String path) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            if (StringUtils.hasText(path)) {
                if (key.startsWith("[")) {
                    key = path + key;
                } else {
                    key = path + "." + key;
                }
            }
            Object value = entry.getValue();
            if (value instanceof String) {
                result.put(key, value);
            } else if (value instanceof Map) {
                // Need a compound key
                @SuppressWarnings("unchecked") Map<String, Object> map = (Map<String, Object>) value;
                buildFlattenedMap(result, map, key);
            } else if (value instanceof Collection) {
                // Need a compound key
                @SuppressWarnings("unchecked") Collection<Object> collection = (Collection<Object>) value;
                int count = 0;
                for (Object object : collection) {
                    buildFlattenedMap(result, Collections.singletonMap("[" + (count++) + "]", object), key);
                }
            } else {
                result.put(key, value == null ? "" : value);
            }
        }
    }
}
