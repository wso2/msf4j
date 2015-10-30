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

package org.wso2.carbon.mss;

/**
 * Interface that needs to be implemented for handling HTTP methods. init and destroy methods can be used to manage
 * the lifecycle of the object. The framework will call init and destroy during startup and shutdown respectively.
 * No handles will be called before init method of the class is called or after the destroy method is called.
 * The handlers should be annotated with Jax-RS annotations to handle appropriate path and HTTP Methods.
 * Note: Only the annotations in the given handler object will be inspected and be available for routing. The
 * annotations from the base class (if extended) will not be applied to the given handler object.
 * Note: The framework that calls the handler assumes that the implementation is threadsafe.
 * Note: If the HttpHandler implementation is extended, the annotations are not inherited from the base class.
 *
 * Example:
 * public class ApiHandler implements HttpHandler{
 * {@literal @}Override
 * public void init(HandlerContext context){
 * //Perform bootstrap operations before any of the handlers in this class gets called.
 * }
 * {@literal @}Override
 * public void destroy(HandlerContext context){
 * //Perform teardown operations before the server shuts down.
 * }
 *
 * {@literal @}Path("/common/v1/widgets")
 * {@literal @}GET
 * public void handleGet(HttpRequest request, HttpResponder responder){
 * //Handle Http request
 * }
 * }
 */
public interface HttpHandler {

    /**
     * init method will be called before the netty pipeline is setup. Any initialization operation can be performed
     * in this method.
     *
     * @param context instance of HandlerContext.
     */
    void init(HandlerContext context);

    /**
     * destroy method will be called before shutdown. Any teardown task can be performed in this method.
     * At this point the server will not accept any more new requests.
     *
     * @param context instance of HandlerContext.
     */
    void destroy(HandlerContext context);
}
