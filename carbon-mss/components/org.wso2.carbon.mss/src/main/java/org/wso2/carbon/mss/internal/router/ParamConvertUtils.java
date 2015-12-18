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

package org.wso2.carbon.mss.internal.router;

import com.google.common.base.Defaults;
import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import com.google.common.primitives.Primitives;
import com.google.common.reflect.TypeToken;
import org.apache.commons.beanutils.ConvertUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * Util class to convert request parameters.
 */
public final class ParamConvertUtils {

    private static final Map<Class<?>, Method> PRIMITIVES_PARSE_METHODS;

    // Setup methods for converting string into primitive/boxed types
    static {
        Map<Class<?>, Method> methods = Maps.newIdentityHashMap();
        for (Class<?> wrappedType : Primitives.allWrapperTypes()) {
            try {
                methods.put(wrappedType, wrappedType.getMethod("valueOf", String.class));
            } catch (NoSuchMethodException e) {
                // Void and Character has no valueOf. It's ok to ignore them
            }
        }

        PRIMITIVES_PARSE_METHODS = methods;
    }

    private ParamConvertUtils() {
    }

    /**
     * Creates a converter function that converts a path segment into the given result type.
     * Current implementation doesn't follow the {@link javax.ws.rs.PathParam} specification to maintain backward
     * compatibility.
     *
     * @param resultType Result type
     * @return Function the function
     */
    public static Function<String, Object> createPathParamConverter(final Type resultType) {
        if (!(resultType instanceof Class)) {
            throw new IllegalArgumentException("Unsupported @PathParam type " + resultType);
        }
        return new Function<String, Object>() {
            @Override
            public Object apply(String value) {
                return ConvertUtils.convert(value, (Class<?>) resultType);
            }
        };
    }

    /**
     * Creates a converter function that converts header value into an object of the given result type.
     * It follows the supported types of {@link javax.ws.rs.HeaderParam} with the following exceptions:
     * <ol>
     * <li>Does not support types registered with {@link javax.ws.rs.ext.ParamConverterProvider}</li>
     * </ol>
     *
     * @param resultType Result type
     * @return Function the function
     */
    public static Function<List<String>, Object> createHeaderParamConverter(Type resultType) {
        return createListConverter(resultType);
    }

    /**
     * Creates a converter function that converts query parameter into an object of the given result type.
     * It follows the supported types of {@link javax.ws.rs.QueryParam} with the following exceptions:
     * <ol>
     * <li>Does not support types registered with {@link javax.ws.rs.ext.ParamConverterProvider}</li>
     * </ol>
     *
     * @param resultType Result type
     * @return Function the function
     */
    public static Function<List<String>, Object> createQueryParamConverter(Type resultType) {
        return createListConverter(resultType);
    }

    /**
     * Common helper method to convert value for {@link javax.ws.rs.HeaderParam} and {@link javax.ws.rs.QueryParam}.
     *
     * @param resultType Result type
     * @return Function the function
     * @see #createHeaderParamConverter(Type)
     * @see #createQueryParamConverter(Type)
     */
    private static Function<List<String>, Object> createListConverter(Type resultType) {
        TypeToken<?> typeToken = TypeToken.of(resultType);

        // Use boxed type if raw type is primitive type. Otherwise the type won't change.
        Class<?> resultClass = typeToken.getRawType();

        // For string, just return the first value
        if (resultClass == String.class) {
            return new BasicConverter(Defaults.defaultValue(resultClass)) {
                @Override
                protected Object convert(String value) throws Exception {
                    return value;
                }
            };
        }

        // Creates converter based on the type

        // Primitive
        Function<List<String>, Object> converter = createPrimitiveTypeConverter(resultClass);
        if (converter != null) {
            return converter;
        }

        // String constructor
        converter = createStringConstructorConverter(resultClass);
        if (converter != null) {
            return converter;
        }

        // Static string argument methods
        converter = createStringMethodConverter(resultClass);
        if (converter != null) {
            return converter;
        }

        // Collection
        converter = createCollectionConverter(typeToken);
        if (converter != null) {
            return converter;
        }

        throw new IllegalArgumentException("Unsupported type " + typeToken);
    }

    /**
     * Creates a converter function that converts value into primitive type.
     *
     * @param resultClass The result class
     * @return A converter function or {@code null} if the given type is not primitive type
     */
    private static Function<List<String>, Object> createPrimitiveTypeConverter(Class<?> resultClass) {
        Object defaultValue = Defaults.defaultValue(resultClass);
        final Class<?> boxedType = Primitives.wrap(resultClass);

        if (!Primitives.isWrapperType(boxedType)) {
            return null;
        }

        return new BasicConverter(defaultValue) {
            @Override
            protected Object convert(String value) throws Exception {
                Method method = PRIMITIVES_PARSE_METHODS.get(boxedType);
                if (method != null) {
                    // It's primitive/wrapper type (except char)
                    return method.invoke(null, value);
                }
                // One exception is char type
                if (boxedType == Character.class) {
                    return value.charAt(0);
                }

                // Should not happen.
                return null;
            }
        };
    }

    /**
     * Creates a converter function that converts value using a constructor that accepts a single String argument.
     *
     * @param resultClass Result class
     * @return A converter function or {@code null} if the given type doesn't have a public constructor that accepts
     * a single String argument.
     */
    private static Function<List<String>, Object> createStringConstructorConverter(Class<?> resultClass) {
        try {
            final Constructor<?> constructor = resultClass.getConstructor(String.class);
            return new BasicConverter(Defaults.defaultValue(resultClass)) {
                @Override
                protected Object convert(String value) throws Exception {
                    return constructor.newInstance(value);
                }
            };
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    /**
     * Creates a converter function that converts value using a public static method named
     * {@code valueOf} or {@code fromString} that accepts a single String argument.
     *
     * @param resultClass Result class
     * @return A converter function or {@code null} if the given type doesn't have a public static method
     * named {@code valueOf} or {@code fromString} that accepts a single String argument.
     */
    private static Function<List<String>, Object> createStringMethodConverter(Class<?> resultClass) {
        Method method;
        try {
            method = resultClass.getMethod("valueOf", String.class);
        } catch (NoSuchMethodException e) {
            try {
                method = resultClass.getMethod("fromString", String.class);
            } catch (NoSuchMethodException ex) {
                return null;
            }
        }

        final Method convertMethod = method;
        return new BasicConverter(Defaults.defaultValue(resultClass)) {
            @Override
            protected Object convert(String value) throws Exception {
                return convertMethod.invoke(null, value);
            }
        };
    }

    /**
     * Creates a converter function that converts value into a {@link List}, {@link Set} or {@link SortedSet}.
     *
     * @param resultType Result type
     * @return A converter function or {@code null} if the given type is not a {@link ParameterizedType} with raw type
     * as {@link List}, {@link Set} or {@link SortedSet}. Also, for {@link SortedSet} type, if the element type
     * doesn't implements {@link Comparable}, {@code null} is returned.
     */
    private static Function<List<String>, Object> createCollectionConverter(TypeToken<?> resultType) {
        final Class<?> rawType = resultType.getRawType();

        // Collection. It must be List or Set
        if (rawType != List.class && rawType != Set.class && rawType != SortedSet.class) {
            return null;
        }

        // Must be ParameterizedType
        if (!(resultType.getType() instanceof ParameterizedType)) {
            return null;
        }

        // Must have 1 type parameter
        ParameterizedType type = (ParameterizedType) resultType.getType();
        if (type.getActualTypeArguments().length != 1) {
            return null;
        }

        // For SortedSet, the entry type must be Comparable.
        Type elementType = type.getActualTypeArguments()[0];
        if (rawType == SortedSet.class && !Comparable.class.isAssignableFrom(TypeToken.of(elementType).getRawType())) {
            return null;
        }

        // Get the converter for the collection element.
        final Function<List<String>, Object> elementConverter = createQueryParamConverter(elementType);

        return new Function<List<String>, Object>() {
            @Override
            public Object apply(List<String> values) {
                ImmutableCollection.Builder<? extends Comparable> builder;
                if (rawType == List.class) {
                    builder = ImmutableList.builder();
                } else if (rawType == Set.class) {
                    builder = ImmutableSet.builder();
                } else {
                    builder = ImmutableSortedSet.naturalOrder();
                }

                if (values != null) {
                    for (String value : values) {
                        add(builder, elementConverter.apply(ImmutableList.of(value)));
                    }
                }
                return builder.build();
            }

            @SuppressWarnings("unchecked")
            private <T> void add(ImmutableCollection.Builder<T> builder, Object element) {
                builder.add((T) element);
            }
        };
    }

    /**
     * A converter that converts first String value from a List of String.
     */
    private abstract static class BasicConverter implements Function<List<String>, Object> {

        private final Object defaultValue;

        protected BasicConverter(Object defaultValue) {
            this.defaultValue = defaultValue;
        }

        @Override
        public final Object apply(List<String> values) {
            if (values == null || values.isEmpty()) {
                return getDefaultValue();
            }
            try {
                return convert(values.get(0));
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }

        protected Object getDefaultValue() {
            return defaultValue;
        }

        protected abstract Object convert(String value) throws Exception;
    }
}
