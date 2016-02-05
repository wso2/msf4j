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

package org.wso2.msf4j.examples.petstore.util.fe.dao;

import org.wso2.msf4j.examples.petstore.util.fe.client.TxnServiceClient;
import org.wso2.msf4j.examples.petstore.util.fe.model.Cart;
import org.wso2.msf4j.examples.petstore.util.fe.model.OrderServiceException;
import org.wso2.msf4j.examples.petstore.util.model.CreditCard;

import javax.annotation.Nullable;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;

/**
 * PaymentService class.
 */
@ManagedBean
@ApplicationScoped
public class PaymentService {

    @Nullable
    @ManagedProperty("#{txnServiceClient}")
    private TxnServiceClient txnServiceClient;

    public String order(Cart cart, CreditCard creditCard) throws OrderServiceException {
        return txnServiceClient.addOrder(cart, creditCard);
    }

    public TxnServiceClient getTxnServiceClient() {
        return txnServiceClient;
    }

    public void setTxnServiceClient(TxnServiceClient txnServiceClient) {
        this.txnServiceClient = txnServiceClient;
    }

}
