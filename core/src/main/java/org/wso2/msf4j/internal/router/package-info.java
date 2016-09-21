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

/**
 * Service and components to build Netty based Http web service.
 * {@code NettyHttpService} sets up the necessary pipeline and manages starting, stopping,
 * state-management of the web service.
 *
 *
 * In-order to handle http requests, {@code HttpHandler} must be implemented. The methods
 * in the classes implemented from {@code HttpHandler} must be annotated with Jersey annotations to
 * specify http uri paths and http methods.
 * Note: Only supports the following annotations:
 * {@link javax.ws.rs.Path Path},
 * {@link javax.ws.rs.PathParam PathParam},
 * {@link javax.ws.rs.GET GET},
 * {@link javax.ws.rs.PUT PUT},
 * {@link javax.ws.rs.POST POST},
 * {@link javax.ws.rs.DELETE DELETE},
 * {@link javax.ws.rs.HEAD HEAD},
 * {@link javax.ws.rs.OPTIONS OPTIONS}.
 *
 * Note: Doesn't support getting Annotations from base class if the HttpHandler implements also extends
 * a class with annotation.
 *
 * Sample usage Handlers and Netty service setup:
 *
 * <pre>
 * //Setup Handlers
 *
 * {@literal @}Path("/common/v1/")
 * public class ApiHandler implements HttpHandler {
 *
 *   {@literal @}Path("widgets")
 *   {@literal @}GET
 *   public void widgetHandler(HttpRequest request, HttpResponder responder) {
 *     responder.sendJson(HttpResponseStatus.OK, "{\"key\": \"value\"}");
 *   }
 *
 *   {@literal @}Override
 *   public void init(HandlerContext context) {
 *     //Perform bootstrap operations before any of the handlers in this class gets called.
 *   }
 *
 *   {@literal @}Override
 *   public void destroy(HandlerContext context) {
 *    //Perform teardown operations the server shuts down.
 *   }
 * }
 *
 * //Set up and start the http service
 * NettyHttpService service = NettyHttpService.builder()
 *                                            .addHttpHandlers(ImmutableList.of(new Handler())
 *                                            .setPort(8989)
 *                                            .build();
 * service.startAndWait();
 *
 * // ....
 *
 * //Stop the web-service
 * service.shutdown();
 *
 * </pre>
 */
package org.wso2.msf4j.internal.router;

