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
package org.wso2.msf4j.service;

import org.wso2.msf4j.Request;
import org.wso2.msf4j.Session;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

/**
 * Simple RESTful web service.
 */
@Path("/SecondService")
public class SecondService {

    private static final String COUNTER = "counter";
    private static final String COUNTER2 = "counter2";

    @GET
    @Path("/addNumbers/{no1}/{no2}")
    public int add(@PathParam("no1") int no1, @PathParam("no2") int no2) {
        return no1 + no2;
    }

    @GET
    @Path("/session")
    public int count(@Context Request request) {
        Session session = request.getSession();

        // Create & set the counter in the session
        Object attribute = session.getAttribute(COUNTER);
        if (attribute == null) {
            attribute = 0;
        }
        int counter = (int) attribute;
        counter++;
        session.setAttribute(COUNTER, counter);

        // Invalidate this session if the count goes beyond 100
        if (counter >= 2) {
            session.invalidate();
        }

        return counter;
    }

    @GET
    @Path("/removeSessionAttribute")
    public int removeSessionAttribute(@Context Request request) {
        Session session = request.getSession();
        Object attribute = session.getAttribute(COUNTER);
        if (attribute == null) {
            attribute = session.getAttribute(COUNTER2);
        }
        if (attribute == null) {
            attribute = 0;
        }

        int counter = (int) attribute;
        counter++;
        session.setAttribute(COUNTER, counter);

        if (counter >= 2) {
            session.removeAttribute(COUNTER);
            session.setAttribute(COUNTER2, counter);
        }

        return counter;
    }
}
