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

package org.wso2.msf4j.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.example.exception.ClientException;
import org.wso2.msf4j.example.exception.CustomerNotFoundException;
import org.wso2.msf4j.example.exception.InvoiceNotFoundException;
import org.wso2.msf4j.example.model.Customer;
import org.wso2.msf4j.example.model.Invoice;
import org.wso2.msf4j.example.model.InvoiceReport;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * ReportService resource class.
 */
@Path("/report")
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);
    private static final String SERVICE_URL = "http://localhost:8080/";
    private static final String DAS_RECEIVER_URL = "http://localhost:9763/endpoints/msf4jtracereceiver";
    private final InvoiceServiceAPI invoiceServiceClient = InvoiceServiceClient.getInstanceWithAnalytics(SERVICE_URL,
            DAS_RECEIVER_URL);
    private final CustomerServiceAPI customerServiceClient = CustomerServiceClient.getInstanceWithAnalytics
            (SERVICE_URL, DAS_RECEIVER_URL);

    /**
     * Retrieves the invoice report for a given invoice ID.
     * http://localhost:8080/report/invoice/I001
     *
     * @param id Invoice ID will be taken from the path parameter.
     * @return
     */
    @GET
    @Path("/invoice/{id}")
    @Produces({"application/json"})
    public Response getInvoiceReport(@PathParam("id") String id) throws InvoiceNotFoundException,
            CustomerNotFoundException {
        InvoiceReport invoiceReport;
        try {
            Invoice invoice = invoiceServiceClient.getInvoice(id);
            if (invoice == null) {
                throw new InvoiceNotFoundException();
            }
            if (log.isDebugEnabled()) {
                log.debug("Invoice retrieved: " + invoice.toString());
            }
            Customer customer = customerServiceClient.getCustomer(invoice.getCustomerId());
            if (customer == null) {
                throw new CustomerNotFoundException();
            }
            if (log.isDebugEnabled()) {
                log.debug("Customer retrieved: " + customer.toString());
            }
            invoiceReport = new InvoiceReport(invoice, customer);
        } catch (ClientException e) {
            throw new InvoiceNotFoundException(e);
        }

        return Response.status(Response.Status.OK).entity(invoiceReport).build();
    }
}
