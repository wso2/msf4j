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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;

/**
 * Represents a list of Invoice objects. This wrapper is
 * required for proper xml list marshaling.
 */
@XmlRootElement
public class Invoices {

    @XmlElement(name = "invoice")
    private Collection<Invoice> invoices;

    /**
     * No arg constructor is required for xml marshalling
     */
    public Invoices() {
    }

    public Invoices(Collection<Invoice> invoices) {
        this.invoices = invoices;
    }

    public Collection<Invoice> getInvoices() {
        return invoices;
    }
}
