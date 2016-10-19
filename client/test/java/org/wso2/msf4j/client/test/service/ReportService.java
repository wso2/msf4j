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

package org.wso2.msf4j.client.test.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.client.MSF4JClient;
import org.wso2.msf4j.client.test.client.api.CustomerServiceAPI;
import org.wso2.msf4j.client.test.client.api.InvoiceServiceAPI;
import org.wso2.msf4j.client.test.client.exception.CustomerNotFoundRestServiceException;
import org.wso2.msf4j.client.test.client.exception.InvoiceNotFoundRestServiceException;
import org.wso2.msf4j.client.test.exception.InvoiceNotFoundException;
import org.wso2.msf4j.client.test.model.Customer;
import org.wso2.msf4j.client.test.client.exception.CustomerNotFoundResponseMapper;
import org.wso2.msf4j.client.test.client.exception.InvoiceNotFoundResponseMapper;
import org.wso2.msf4j.client.test.exception.CustomerNotFoundException;
import org.wso2.msf4j.client.test.exception.GenericServerErrorException;
import org.wso2.msf4j.client.test.model.Invoice;
import org.wso2.msf4j.client.test.model.InvoiceReport;

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
    private static final String CUSTOMER_SERVICE_URL = "http://localhost:8088";
    private static final String INVOICE_SERVICE_URL = "http://localhost:8089";
    private final MSF4JClient<CustomerServiceAPI> customerServiceClient;
    private final MSF4JClient<InvoiceServiceAPI> invoiceServiceClient;

    public ReportService() {
        customerServiceClient = new MSF4JClient.Builder<CustomerServiceAPI>()
                .apiClass(CustomerServiceAPI.class)
                .instanceName("CustomerServiceClient")
                .serviceEndpoint(CUSTOMER_SERVICE_URL)
                .addErrorResponseMapper(new CustomerNotFoundResponseMapper())
                .build();

        invoiceServiceClient = new MSF4JClient.Builder<InvoiceServiceAPI>()
                .apiClass(InvoiceServiceAPI.class)
                .instanceName("InvoiceServiceClient")
                .serviceEndpoint(INVOICE_SERVICE_URL)
               // .addErrorResponseMapper(new InvoiceNotFoundResponseMapper())
                .build();
    }

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
                                                                        CustomerNotFoundException, GenericServerErrorException {
        InvoiceReport invoiceReport;
        Invoice invoice;
        try {
            invoice = invoiceServiceClient.api().getInvoice(id);
            if (log.isDebugEnabled()) {
                log.info("Invoice retrieved: " + invoice);
            }
        } catch (InvoiceNotFoundRestServiceException e) {
            throw new InvoiceNotFoundException(e);
        } catch (Exception e) {
            log.error("Generic exception encountered", e);
            throw new GenericServerErrorException("Server Error: Something went wrong!");
        }

        try {
            String customerId = invoice.getCustomerId();
            Customer customer = customerServiceClient.api().getCustomer(customerId);
            if (log.isDebugEnabled()) {
                log.debug("Customer retrieved: " + customer);
            }
            invoiceReport = new InvoiceReport(invoice, customer);
        } catch (CustomerNotFoundRestServiceException e) {
            throw new CustomerNotFoundException(e);
        } catch (Exception e) {
            log.error("Generic exception encountered", e);
            throw new GenericServerErrorException("Server Error: Something went wrong!");
        }

        return Response.status(Response.Status.OK).entity(invoiceReport).build();
    }
}
