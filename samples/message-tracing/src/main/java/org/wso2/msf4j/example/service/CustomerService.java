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

package org.wso2.msf4j.example.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.example.exception.CustomerNotFoundException;
import org.wso2.msf4j.example.model.Customer;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * CustomerService resource class.
 */
@Path("/customer")
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);
    private Map<String, Customer> customerMap = new HashMap<>();

    public CustomerService() {
        customerMap.put("C001", new Customer("C001", "Akila", "Perera", "DreamWorld!"));
    }

    /**
     * Retrieves a customer record for a given customer ID.
     * http://localhost:8080/customer/C001
     *
     * @param id Customer ID will be taken from the path parameter.
     * @return
     */
    @GET
    @Path("/{id}")
    @Produces({"application/json"})
    public Response getCustomer(@PathParam("id") String id) throws CustomerNotFoundException {
        Customer customer = customerMap.get(id);
        if (customer == null) {
            log.info("Request for non-existing customer: " + id);
            throw new CustomerNotFoundException("Customer ID " + id + " not found");
        }
        return Response.status(Response.Status.OK).entity(customer).build();
    }
}
