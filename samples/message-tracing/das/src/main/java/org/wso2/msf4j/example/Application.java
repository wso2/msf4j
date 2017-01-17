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

import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.analytics.tracing.MSF4JTracingInterceptor;
import org.wso2.msf4j.example.exception.CustomerNotFoundMapper;
import org.wso2.msf4j.example.exception.EntityNotFoundMapper;
import org.wso2.msf4j.example.exception.GenericServerErrorMapper;
import org.wso2.msf4j.example.exception.InvoiceNotFoundMapper;
import org.wso2.msf4j.example.service.CustomerService;
import org.wso2.msf4j.example.service.InvoiceService;
import org.wso2.msf4j.example.service.ReportService;

/**
 * Application entry point.
 */
public class Application {

    private Application() {
    }

    public static void main(String[] args) {
        MSF4JTracingInterceptor customerServiceTracingInterceptor = new MSF4JTracingInterceptor("Customer-Service");
        MSF4JTracingInterceptor invoiceServiceTracingInterceptor = new MSF4JTracingInterceptor("Invoice-Service");
        MSF4JTracingInterceptor reportServiceTracingInterceptor = new MSF4JTracingInterceptor("Report-Service");

        new MicroservicesRunner(8081)
                .addExceptionMapper(new EntityNotFoundMapper(), new CustomerNotFoundMapper(), new
                        GenericServerErrorMapper())
                .registerGlobalRequestInterceptor(customerServiceTracingInterceptor)
                .registerGlobalResponseInterceptor(customerServiceTracingInterceptor)
                .deploy(new CustomerService())
                .start();

        new MicroservicesRunner(8082)
                .addExceptionMapper(new EntityNotFoundMapper(), new InvoiceNotFoundMapper(), new
                        GenericServerErrorMapper())
                .registerGlobalRequestInterceptor(invoiceServiceTracingInterceptor)
                .registerGlobalResponseInterceptor(invoiceServiceTracingInterceptor)
                .deploy(new InvoiceService())
                .start();
        new MicroservicesRunner()
                .addExceptionMapper(new EntityNotFoundMapper(), new CustomerNotFoundMapper(), new
                        InvoiceNotFoundMapper(), new GenericServerErrorMapper())
                .registerGlobalRequestInterceptor(reportServiceTracingInterceptor)
                .registerGlobalResponseInterceptor(reportServiceTracingInterceptor)
                .deploy(new ReportService())
                .start();
    }
}
