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
package org.wso2.msf4j.example.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * Represents a stock item. @XmlRootElement is used to support xml conversion.
 */
@SuppressWarnings("unused")
@XmlRootElement
public class InvoiceReport {

    private String id;
    private Customer customer;
    private double amount;
    private Date date;

    /**
     * No arg constructor is required for xml marshalling
     */
    public InvoiceReport() {
    }

    public InvoiceReport(Invoice invoice, Customer customer) {
        this.id = invoice.getId();
        this.customer = customer;
        this.amount = invoice.getAmount();
        this.date = invoice.getDate();
    }

    public InvoiceReport(String id, Customer customer, double amount, Date date) {
        this.id = id;
        this.customer = customer;
        this.amount = amount;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
