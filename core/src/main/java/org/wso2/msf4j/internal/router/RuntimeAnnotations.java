package org.wso2.msf4j.internal.router;
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Utility class used to change the class annotation at runtime.
 */
public final class RuntimeAnnotations {

    private static final Constructor<?> annotationInvocationHandlerConstructor;
    private static final Constructor<?> annotationDataConstructor;
    private static final Method classAnnotationData;
    private static final Field classClassRedefinedCount;
    private static final Field annotationDataAnnotations;
    private static final Field annotationDataDeclaredAnotations;
    private static final Method atomicClassAnnotationData;
    private static final Class<?> atomicClass;

    static {
        // static initialization of necessary reflection Objects
        try {
            Class<?> annotationInvocationHandlerClass =
                    Class.forName("sun.reflect.annotation.AnnotationInvocationHandler");
            annotationInvocationHandlerConstructor =
                    annotationInvocationHandlerClass.getDeclaredConstructor(new Class[] { Class.class, Map.class });
            annotationInvocationHandlerConstructor.setAccessible(true);

            atomicClass = Class.forName("java.lang.Class$Atomic");
            Class<?> annotationDataClass = Class.forName("java.lang.Class$AnnotationData");

            annotationDataConstructor =
                    annotationDataClass.getDeclaredConstructor(new Class[] { Map.class, Map.class, int.class });
            annotationDataConstructor.setAccessible(true);
            classAnnotationData = Class.class.getDeclaredMethod("annotationData");
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    classAnnotationData.setAccessible(true);
                    return null;
                }
            });

            classClassRedefinedCount = Class.class.getDeclaredField("classRedefinedCount");

            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Void run() {
                    classClassRedefinedCount.setAccessible(true);
                    return null;
                }
            });

            annotationDataAnnotations = annotationDataClass.getDeclaredField("annotations");
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    annotationDataAnnotations.setAccessible(true);
                    return null;
                }
            });
            annotationDataDeclaredAnotations = annotationDataClass.getDeclaredField("declaredAnnotations");
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    annotationDataDeclaredAnotations.setAccessible(true);
                    return null;
                }
            });
            atomicClassAnnotationData = atomicClass
                    .getDeclaredMethod("casAnnotationData", Class.class, annotationDataClass, annotationDataClass);
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                @Override
                public Object run() {
                    atomicClassAnnotationData.setAccessible(true);
                    return null;
                }
            });

        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Change the given annotation of the given class.
     *
     * @param clazz Class need to be change
     * @param annotationClass Annotation class that need to change
     * @param valuesMap value map to set
     * @param <T> Annotation that get change
     */
    public static <T extends Annotation> void putAnnotation(Class<?> clazz, Class<T> annotationClass,
                                                            Map<String, Object> valuesMap) {
        putAnnotation(clazz, annotationClass, annotationForMap(annotationClass, valuesMap));
    }

    @SuppressWarnings("unchecked")
    private static <T extends Annotation> void putAnnotation(Class<?> c, Class<T> annotationClass, T annotation) {
        try {
            while (true) { // retry loop
                int classRedefinedCount = classClassRedefinedCount.getInt(c);
                Object annotationData = classAnnotationData.invoke(c);
                // null or stale annotationData -> optimistically create new instance
                Object newAnnotationData =
                        createAnnotationData(c, annotationData, annotationClass, annotation, classRedefinedCount);
                // try to install it
                if ((boolean) atomicClassAnnotationData.invoke(atomicClass, c, annotationData, newAnnotationData)) {
                    // successfully installed new AnnotationData
                    break;
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException
                | InstantiationException e) {
            throw new IllegalStateException(e);
        }

    }

    @SuppressWarnings("unchecked")
    private static <T extends Annotation> Object createAnnotationData(Class<?> c, Object annotationData,
                                                                      Class<T> annotationClass, T annotation,
                                                                      int classRedefinedCount)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        Map<Class<? extends Annotation>, Annotation> annotations =
                (Map<Class<? extends Annotation>, Annotation>) annotationDataAnnotations.get(annotationData);
        Map<Class<? extends Annotation>, Annotation> declaredAnnotations =
                (Map<Class<? extends Annotation>, Annotation>) annotationDataDeclaredAnotations.get(annotationData);

        Map<Class<? extends Annotation>, Annotation> newDeclaredAnnotations = new LinkedHashMap<>(annotations);
        newDeclaredAnnotations.put(annotationClass, annotation);
        Map<Class<? extends Annotation>, Annotation> newAnnotations;
        if (declaredAnnotations == annotations) {
            newAnnotations = newDeclaredAnnotations;
        } else {
            newAnnotations = new LinkedHashMap<>(annotations);
            newAnnotations.put(annotationClass, annotation);
        }
        return annotationDataConstructor.newInstance(newAnnotations, newDeclaredAnnotations, classRedefinedCount);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Annotation> T annotationForMap(final Class<T> annotationClass,
                                                            final Map<String, Object> valuesMap) {
        return (T) AccessController.doPrivileged(new PrivilegedAction<Annotation>() {
            public Annotation run() {
                InvocationHandler handler;
                try {
                    handler = (InvocationHandler) annotationInvocationHandlerConstructor
                            .newInstance(annotationClass, new HashMap<>(valuesMap));
                } catch (InstantiationException | IllegalAccessException
                        | IllegalArgumentException | InvocationTargetException e) {
                    throw new IllegalStateException(e);
                }
                return (Annotation) Proxy
                        .newProxyInstance(annotationClass.getClassLoader(), new Class[] { annotationClass }, handler);
            }
        });
    }
}
