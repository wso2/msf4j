/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.mss.examples.petstore.store.dao;

import org.wso2.carbon.mss.examples.petstore.store.client.TxnServiceClient;
import org.wso2.carbon.mss.examples.petstore.store.model.Cart;
import org.wso2.carbon.mss.examples.petstore.store.model.OrderServiceException;
import org.wso2.carbon.mss.examples.petstore.util.model.CreditCard;

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
