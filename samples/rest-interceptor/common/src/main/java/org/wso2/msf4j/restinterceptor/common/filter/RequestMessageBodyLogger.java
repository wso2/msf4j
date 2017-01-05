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
package org.wso2.msf4j.restinterceptor.common.filter;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.filter.MSF4JRequestFilter;

import java.io.IOException;

/**
 * Class for logging the message body of the http request.
 */
@Component(
        name = "org.wso2.msf4j.restinterceptor.common.filter.RequestMessageBodyLogger",
        service = MSF4JRequestFilter.class,
        immediate = true
)
public class RequestMessageBodyLogger implements MSF4JRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(HTTPRequestLogger.class);

    @Override
    public void filter(Request request) throws IOException {
        log.info(request.getMessageBody().toString());
    }
}
