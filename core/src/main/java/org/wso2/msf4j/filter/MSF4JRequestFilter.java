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
package org.wso2.msf4j.filter;

import org.wso2.msf4j.Request;

import java.io.IOException;

/**
 * Interface that needs to be implemented to filter request method calls
 */
public interface MSF4JRequestFilter {

    /**
     * Globally, resource vise or sub-resource vise filter requests
     *
     * @param request MSF4J request.
     * @throws IOException upon error on filtering request
     */
    void filter(Request request) throws IOException;
}
