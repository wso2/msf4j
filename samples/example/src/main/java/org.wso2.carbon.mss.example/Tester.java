/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.mss.example;

import co.cask.http.AbstractHttpHandler;
import co.cask.http.NettyHttpService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * TODO: class level comment
 */
public class Tester {
    public static void main(String[] args) {
        List<AbstractHttpHandler> httpHandlers = new ArrayList<AbstractHttpHandler>();
        httpHandlers.add(new StockQuoteService());
        NettyHttpService service =
                NettyHttpService.builder().setPort(7778).addHttpHandlers(httpHandlers).build();

        // Start the HTTP service
        service.startAndWait();


        while (true) {
            try {
                TimeUnit.SECONDS.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
