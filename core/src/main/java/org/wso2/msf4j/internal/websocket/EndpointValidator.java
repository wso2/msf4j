/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.msf4j.internal.websocket;

import org.wso2.msf4j.websocket.exception.WebSocketEndpointAnnotationException;
import org.wso2.msf4j.websocket.exception.WebSocketEndpointMethodReturnTypeException;
import org.wso2.msf4j.websocket.exception.WebSocketMethodParameterException;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.ByteBuffer;
import javax.websocket.CloseReason;
import javax.websocket.PongMessage;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

/**
 * This validates all the methods which are relevant to WebSocket Server endpoint using JSR-356 specification.
 */
public class EndpointValidator {

    /**
     * Validate the whole WebSocket endpoint.
     *
     * @return true if validation is completed without any error.
     * @throws WebSocketEndpointAnnotationException if error on an annotation declaration occurred.
     * @throws WebSocketMethodParameterException if the method parameters are invalid for a given method according
     * to JSR-356 specification.
     */
    public boolean validate(Object webSocketEndpoint) throws WebSocketEndpointAnnotationException,
                                                             WebSocketMethodParameterException,
                                                             WebSocketEndpointMethodReturnTypeException {
        if (webSocketEndpoint == null) {
            return false;
        }
        return validateURI(webSocketEndpoint) && validateOnStringMethod(webSocketEndpoint) &&
                validateOnBinaryMethod(webSocketEndpoint) && validateOnPongMethod(webSocketEndpoint) &&
                validateOnOpenMethod(webSocketEndpoint) && validateOnCloseMethod(webSocketEndpoint) &&
                validateOnErrorMethod(webSocketEndpoint);
    }

    private boolean validateURI(Object webSocketEndpoint) throws WebSocketEndpointAnnotationException {
        if (webSocketEndpoint.getClass().isAnnotationPresent(ServerEndpoint.class)) {
            return true;
        }
        throw new WebSocketEndpointAnnotationException("Server Endpoint is not defined.");
    }

    private boolean validateOnStringMethod(Object webSocketEndpoint)
            throws WebSocketMethodParameterException, WebSocketEndpointMethodReturnTypeException {
        EndpointDispatcher dispatcher = new EndpointDispatcher();
        Method method;
        if (dispatcher.getOnStringMessageMethod(webSocketEndpoint).isPresent()) {
            method = dispatcher.getOnStringMessageMethod(webSocketEndpoint).get();
        } else {
            return true;
        }
        validateReturnType(method);
        boolean foundPrimaryString = false;
        for (Parameter parameter: method.getParameters()) {
            Class<?> paraType = parameter.getType();
            if (paraType == String.class) {
                if (parameter.getAnnotation(PathParam.class) == null) {
                    if (foundPrimaryString) {
                        throw new WebSocketMethodParameterException("Invalid parameter found on text message method: " +
                                                                            "More than one string parameter without " +
                                                                            "@PathParam annotation.");
                    }
                    foundPrimaryString = true;
                }
            } else if (paraType != WebSocketConnection.class) {
                throw new WebSocketMethodParameterException("Invalid parameter found on text message method: " +
                                                                    paraType);
            }
        }
        return foundPrimaryString;
    }

    private boolean validateOnBinaryMethod(Object webSocketEndpoint)
            throws WebSocketMethodParameterException, WebSocketEndpointMethodReturnTypeException {
        EndpointDispatcher dispatcher = new EndpointDispatcher();
        Method method;
        if (dispatcher.getOnBinaryMessageMethod(webSocketEndpoint).isPresent()) {
            method = dispatcher.getOnBinaryMessageMethod(webSocketEndpoint).get();
        } else {
            return true;
        }
        validateReturnType(method);
        boolean foundPrimaryBuffer = false;
        boolean foundIsFinal = false;
        for (Parameter parameter: method.getParameters()) {
            Class<?> paraType = parameter.getType();
            if (paraType == String.class) {
                if (parameter.getAnnotation(PathParam.class) == null) {
                    throw new WebSocketMethodParameterException("Invalid parameter found on binary message method: " +
                                                                        "string parameter without " +
                                                                        "@PathParam annotation.");
                }
            } else if (paraType == ByteBuffer.class || paraType == byte[].class) {
                if (foundPrimaryBuffer) {
                    throw new WebSocketMethodParameterException("Invalid parameter found on binary message method: " +
                                                                        "only one ByteBuffer/byte[] " +
                                                                        "should be declared.");
                }
                foundPrimaryBuffer = true;

            } else if (paraType == boolean.class) {
                if (foundIsFinal) {
                    throw new WebSocketMethodParameterException("Invalid parameter found on binary message method: " +
                                                                        "only one boolean should be declared and " +
                                                                        "found more than one.");
                }
                foundIsFinal = true;
            } else if (paraType != WebSocketConnection.class) {
                throw new WebSocketMethodParameterException("Invalid parameter found on binary message method: " +
                                                                    paraType);
            }
        }
        return foundPrimaryBuffer;
    }

    private boolean validateOnPongMethod(Object webSocketEndpoint)
            throws WebSocketMethodParameterException, WebSocketEndpointMethodReturnTypeException {
        EndpointDispatcher dispatcher = new EndpointDispatcher();
        Method method;
        if (dispatcher.getOnPongMessageMethod(webSocketEndpoint).isPresent()) {
            method = dispatcher.getOnPongMessageMethod(webSocketEndpoint).get();
        } else {
            return true;
        }
        validateReturnType(method);
        boolean foundPrimaryPong = false;
        for (Parameter parameter: method.getParameters()) {
            Class<?> paraType = parameter.getType();
            if (paraType == String.class) {
                if (parameter.getAnnotation(PathParam.class) == null) {
                    throw new WebSocketMethodParameterException("Invalid parameter found on pong message method: " +
                                                                        "string parameter without " +
                                                                        "@PathParam annotation.");
                }
            } else if (paraType == PongMessage.class) {
                if (foundPrimaryPong) {
                    throw new WebSocketMethodParameterException("Invalid parameter found on pong message method: " +
                                                                        "only one PongMessage should be declared.");
                }
                foundPrimaryPong = true;
            } else if (paraType != WebSocketConnection.class) {
                throw new WebSocketMethodParameterException("Invalid parameter found on pong message method: " +
                                                                    paraType);
            }
        }
        return foundPrimaryPong;
    }

    private boolean validateOnOpenMethod(Object webSocketEndpoint)
            throws WebSocketMethodParameterException, WebSocketEndpointMethodReturnTypeException {
        EndpointDispatcher dispatcher = new EndpointDispatcher();
        Method method;
        if (dispatcher.getOnOpenMethod(webSocketEndpoint).isPresent()) {
            method = dispatcher.getOnOpenMethod(webSocketEndpoint).get();
        } else {
            return true;
        }
        validateReturnType(method);
        for (Parameter parameter: method.getParameters()) {
            Class<?> paraType = parameter.getType();
            if (paraType == String.class) {
                if (parameter.getAnnotation(PathParam.class) == null) {
                    throw new WebSocketMethodParameterException("Invalid parameter found on open message method: " +
                                                                        "string parameter without " +
                                                                        "@PathParam annotation.");
                }
            } else if (paraType != WebSocketConnection.class) {
                throw new WebSocketMethodParameterException("Invalid parameter found on open message method: " +
                                                                    paraType);
            }
        }
        return true;
    }

    private boolean validateOnCloseMethod(Object webSocketEndpoint)
            throws WebSocketMethodParameterException, WebSocketEndpointMethodReturnTypeException {
        EndpointDispatcher dispatcher = new EndpointDispatcher();
        Method method;
        if (dispatcher.getOnCloseMethod(webSocketEndpoint).isPresent()) {
            method = dispatcher.getOnCloseMethod(webSocketEndpoint).get();
        } else {
            return true;
        }
        validateReturnType(method);
        for (Parameter parameter: method.getParameters()) {
            Class<?> paraType = parameter.getType();
            if (paraType == String.class) {
                if (parameter.getAnnotation(PathParam.class) == null) {
                    throw new WebSocketMethodParameterException("Invalid parameter found on close message method: " +
                                                                        "string parameter without " +
                                                                        "@PathParam annotation.");
                }
            } else if (paraType != CloseReason.class && paraType != WebSocketConnection.class) {
                throw new WebSocketMethodParameterException("Invalid parameter found on close message method: " +
                                                                    paraType);
            }
        }
        return true;
    }

    private boolean validateOnErrorMethod(Object webSocketEndpoint)
            throws WebSocketMethodParameterException, WebSocketEndpointMethodReturnTypeException {
        EndpointDispatcher dispatcher = new EndpointDispatcher();
        Method method;
        if (dispatcher.getOnErrorMethod(webSocketEndpoint).isPresent()) {
            method = dispatcher.getOnErrorMethod(webSocketEndpoint).get();
        } else {
            return true;
        }
        validateReturnType(method);
        boolean foundPrimaryThrowable = false;
        for (Parameter parameter: method.getParameters()) {
            Class<?> paraType = parameter.getType();
            if (paraType == String.class) {
                if (parameter.getAnnotation(PathParam.class) == null) {
                    throw new WebSocketMethodParameterException("Invalid parameter found on error message method: " +
                                                                        "string parameter without " +
                                                                        "@PathParam annotation.");
                }
            } else if (paraType == Throwable.class) {
                if (foundPrimaryThrowable) {
                    throw new WebSocketMethodParameterException("Invalid parameter found on pong message method: " +
                                                                        "only one Throwable should be declared.");
                }
                foundPrimaryThrowable = true;
            } else if (paraType != WebSocketConnection.class) {
                throw new WebSocketMethodParameterException("Invalid parameter found on error message method: " +
                                                                    paraType);
            }
        }

        if (!foundPrimaryThrowable) {
            throw new WebSocketMethodParameterException("Mandatory parameter for on error method " + Throwable.class +
                                                                " not found.");
        }
        return foundPrimaryThrowable;
    }

    private boolean validateReturnType(Method method) throws WebSocketEndpointMethodReturnTypeException {
        Class<?> returnType = method.getReturnType();
        boolean foundCorrectReturnType = returnType == String.class || returnType == ByteBuffer.class ||
                returnType == byte[].class || returnType == PongMessage.class || returnType == void.class;
        if (!foundCorrectReturnType) {
            throw new WebSocketEndpointMethodReturnTypeException("Unexpected method return type: " + returnType);
        }
        return foundCorrectReturnType;
    }
}
