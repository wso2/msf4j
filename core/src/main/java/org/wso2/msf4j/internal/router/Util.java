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

import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Pattern;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.MediaType;

/**
 * Util class.
 */
public class Util {

    public static final String GROUP_PATTERN = "\\{(.*?)\\}";
    public static final String GROUP_PATTERN_REGEX = "([^/]+?)";
    public static final Pattern WILD_CARD_PATTERN = Pattern.compile("\\*\\*");

    /**
     * Check if http verb is available for the method.
     *
     * @param method
     * @return
     */
    public static boolean isHttpMethodAvailable(Method method) {
        return method.isAnnotationPresent(GET.class) ||
               method.isAnnotationPresent(PUT.class) ||
               method.isAnnotationPresent(POST.class) ||
               method.isAnnotationPresent(DELETE.class) ||
               method.isAnnotationPresent(HEAD.class) ||
               method.isAnnotationPresent(OPTIONS.class);
    }

    /**
     * Remove the curly braces if the token is wrapped with curly braces.
     *
     * @param token
     * @return
     */
    public static String stripBraces(String token) {
        return token.charAt(0) == '{' && token.charAt(token.length() - 1) == '}' ?
               token.substring(1, token.length() - 1) : token;
    }

    /**
     * Process accept type considering the produce type and the
     * accept types of the request header.
     *
     * @param acceptTypes accept types of the request.
     * @return processed accept type
     */
    public static String getResponseType(List<String> acceptTypes, List<String> producesMediaTypes) {
        String responseType = MediaType.WILDCARD;
        if (!producesMediaTypes.contains(MediaType.WILDCARD) && acceptTypes != null) {
            responseType =
                    (acceptTypes.contains(MediaType.WILDCARD)) ? producesMediaTypes.get(0) :
                    producesMediaTypes.stream().filter(acceptTypes::contains).findFirst().get();
        } else if (acceptTypes == null && !producesMediaTypes.isEmpty()) {
            responseType = producesMediaTypes.get(0);
        }
        return responseType;
    }
}
