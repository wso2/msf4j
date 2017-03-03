/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.msf4j.util;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

/**
 * Util class to do java reflection related activities.
 */
public class ReflectionUtils {

    private static final Logger log = LoggerFactory.getLogger(ReflectionUtils.class);

    private ReflectionUtils() {
    }

    /**
     * Create a new instance for a given type.
     *
     * @param clazz                     type of the expected instance
     * @param constructorParameterTypes the parameter array
     * @param constructorArguments      array of objects to be passed as arguments to the constructor call
     * @param <T>                       Type of the object to be created
     * @return Object of type T
     * @throws NoSuchMethodException     when the specified constructor is not available
     * @throws IllegalAccessException    when the constructor is not visible
     * @throws InvocationTargetException when invocation target exception
     * @throws InstantiationException    when error on object creation
     */
    public static <T> T createInstanceFromClass(Class<T> clazz, Class<?>[] constructorParameterTypes,
                                                Object... constructorArguments)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<T> constructor = clazz.getConstructor(constructorParameterTypes);
        return constructor.newInstance(constructorArguments);
    }

    /**
     * Load class from bundle (find class from the bundles).
     * If there is no bundle with the specific class or loading the class from bundle fails, this method
     * will return Optional.empty()
     * This method is only sensible to be used in the OSGi environment
     *
     * @param clazz class to load
     * @param <T>   type of the class
     * @return Optional loaded class
     */
    @SuppressWarnings("unchecked")
    public static <T> Optional<Class<? extends T>> loadClassFromBundle(Class<? extends T> clazz) {
        String className = clazz.getName();
        Bundle bundle = FrameworkUtil.getBundle(clazz);
        if (bundle != null) {
            try {
                return Optional.of((Class<? extends T>) bundle.loadClass(className));
            } catch (ClassNotFoundException e) {
                log.error("Class " + className + " do not exist in any bundle", e);
            }
        }
        return Optional.empty();
    }

    /**
     * Checks if the class is available.
     *
     * @param className String class name
     * @return true if the class exists
     */
    public static boolean isClassAvailable(String className) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException ignore) {
            return false;
        }
        return true;
    }
}
