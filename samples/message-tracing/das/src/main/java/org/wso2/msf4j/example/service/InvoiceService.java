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
import org.wso2.msf4j.example.exception.InvoiceNotFoundException;
import org.wso2.msf4j.example.model.Invoice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * CustomerService resource class.
 */
@Path("/invoice")
public class InvoiceService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceService.class);
    private Map<String, Invoice> invoiceMap = new HashMap<>();

    public InvoiceService() {
        invoiceMap.put("I001", new Invoice("I001", "C001", 250.15, new Date(System.currentTimeMillis())));
    }

    /**
     * Retrieves an invoice record for a given invoice ID.
     * http://localhost:8080/invoice/I001
     *
     * @param id Invoice ID will be taken from the path parameter.
     * @return
     */
    @GET
    @Path("/{id}")
    @Produces({"application/json"})
    public Response getCustomer(@PathParam("id") String id) throws InvoiceNotFoundException {
        Invoice invoice = invoiceMap.get(id);
        if (invoice == null) {
            log.info("Request for non-existing invoice: " + id);
            throw new InvoiceNotFoundException("Invoice ID " + id + " not found");
        }
        return Response.status(Response.Status.OK).entity(invoice).build();
    }
}
