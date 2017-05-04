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

package org.wso2.msf4j.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.msf4j.internal.websocket.EndpointValidator;
import org.wso2.msf4j.websocket.endpoint.TestEndpointWithAllCorrect;
import org.wso2.msf4j.websocket.endpoint.TestEndpointWithMandatoryParameters;
import org.wso2.msf4j.websocket.endpoint.error.TestEndpoinWithOnTextError;
import org.wso2.msf4j.websocket.endpoint.error.TestEndpointWithMandatoryParametersMissing;
import org.wso2.msf4j.websocket.endpoint.error.TestEndpointWithOnBinaryError;
import org.wso2.msf4j.websocket.endpoint.error.TestEndpointWithOnCloseError;
import org.wso2.msf4j.websocket.endpoint.error.TestEndpointWithOnError;
import org.wso2.msf4j.websocket.endpoint.error.TestEndpointWithOnOpenError;
import org.wso2.msf4j.websocket.endpoint.error.TestEndpointWithOnPongError;
import org.wso2.msf4j.websocket.endpoint.error.TestEndpointWithReturnTypeError;
import org.wso2.msf4j.websocket.endpoint.error.TestEndpointWithServerEndpointError;
import org.wso2.msf4j.websocket.exception.WebSocketEndpointAnnotationException;
import org.wso2.msf4j.websocket.exception.WebSocketEndpointMethodReturnTypeException;
import org.wso2.msf4j.websocket.exception.WebSocketMethodParameterException;

/**
 * Test the Exceptions which can be occurred when deploying and running WebSocket.
 */
public class ValidatorTest {

    private static final Logger log = LoggerFactory.getLogger(ValidatorTest.class);
    private EndpointValidator validator = new EndpointValidator();

    @BeforeClass
    public void setup() {
        log.info(System.lineSeparator() +
                         "--------------------------------WebSocket Validator Test--------------------------------");
    }

    @Test(description = "Test endpoint with all necessary methods and all correct parameters")
    public void testCorretEndpoint() throws WebSocketMethodParameterException, WebSocketEndpointAnnotationException,
                                            WebSocketEndpointMethodReturnTypeException {
        log.info("Test endpoint with all necessary methods and all correct parameters");
        Assert.assertTrue(validator.validate(new TestEndpointWithAllCorrect()));
    }

    @Test(description = "Test endpoint with all necessary methods with mandatory parameters")
    public void testMandatoryParametersEndpoint() throws WebSocketMethodParameterException,
                                                         WebSocketEndpointAnnotationException,
                                                         WebSocketEndpointMethodReturnTypeException {
        log.info("Test endpoint with all necessary methods with mandatory parameters");
        Assert.assertTrue(validator.validate(new TestEndpointWithMandatoryParameters()));
    }

    @Test(description = "Check the mandatory parameters missing",
          expectedExceptions = WebSocketMethodParameterException.class)
    public void testMandatoryParameterMissing()
            throws WebSocketMethodParameterException, WebSocketEndpointAnnotationException,
                   WebSocketEndpointMethodReturnTypeException {
        log.info("Check the mandatory parameters missing");
        validator.validate(new TestEndpointWithMandatoryParametersMissing());
    }

    @Test(description = "Test the expected exceptions for not defining server endpoint",
          expectedExceptions = WebSocketEndpointAnnotationException.class)
    public void testerverEndpoint() throws WebSocketMethodParameterException, WebSocketEndpointAnnotationException,
                                           WebSocketEndpointMethodReturnTypeException {
        log.info("Test the expected exceptions for not defining server endpoint");
        validator.validate(new TestEndpointWithServerEndpointError());

    }

    @Test(description = "Test the expected exceptions for onOpen",
          expectedExceptions = WebSocketMethodParameterException.class)
    public void testOnOpen() throws WebSocketMethodParameterException, WebSocketEndpointAnnotationException,
                                    WebSocketEndpointMethodReturnTypeException {
        log.info("Test the expected exceptions for onOpen");
        validator.validate(new TestEndpointWithOnOpenError());
    }

    @Test(description = "Test the expected exceptions for onClose",
          expectedExceptions = WebSocketMethodParameterException.class)
    public void testOnClose() throws WebSocketMethodParameterException, WebSocketEndpointAnnotationException,
                                     WebSocketEndpointMethodReturnTypeException {
        log.info("Test the expected exceptions for onClose");
        validator.validate(new TestEndpointWithOnCloseError());
    }

    @Test(description = "Test the expected exceptions for onTextMessage",
          expectedExceptions = WebSocketMethodParameterException.class)
    public void testOnTextMessage() throws WebSocketMethodParameterException, WebSocketEndpointAnnotationException,
                                           WebSocketEndpointMethodReturnTypeException {
        log.info("Test the expected exceptions for onTextMessage");
        validator.validate(new TestEndpoinWithOnTextError());
    }

    @Test(description = "Test the expected exceptions for onBinaryMessage",
          expectedExceptions = WebSocketMethodParameterException.class)
    public void testOnBinaryMessage() throws WebSocketMethodParameterException, WebSocketEndpointAnnotationException,
                                             WebSocketEndpointMethodReturnTypeException {
        log.info("Test the expected exceptions for onBinaryMessage");
        validator.validate(new TestEndpointWithOnBinaryError());
    }

    @Test(description = "Test the expected exceptions for onPongMessage",
          expectedExceptions = WebSocketMethodParameterException.class)
    public void testOnPongMessage() throws WebSocketMethodParameterException, WebSocketEndpointAnnotationException,
                                           WebSocketEndpointMethodReturnTypeException {
        log.info("Test the expected exceptions for onPongMessage");
        validator.validate(new TestEndpointWithOnPongError());
    }

    @Test(description = "Test the expected exceptions for onError",
          expectedExceptions = WebSocketMethodParameterException.class)
    public void testOnError() throws WebSocketMethodParameterException, WebSocketEndpointAnnotationException,
                                     WebSocketEndpointMethodReturnTypeException {
        log.info("Test the expected exceptions for onError");
        validator.validate(new TestEndpointWithOnError());
    }

    @Test(description = "Test the expected exceptions for invalid return type",
          expectedExceptions = WebSocketEndpointMethodReturnTypeException.class)
    public void testReturnTypeError() throws WebSocketMethodParameterException, WebSocketEndpointAnnotationException,
                                     WebSocketEndpointMethodReturnTypeException {
        log.info("Test the expected exceptions for invalid return type");
        validator.validate(new TestEndpointWithReturnTypeError());
    }
}
