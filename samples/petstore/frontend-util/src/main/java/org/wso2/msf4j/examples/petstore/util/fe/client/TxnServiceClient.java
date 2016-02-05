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

package org.wso2.msf4j.examples.petstore.util.fe.client;

import com.google.gson.Gson;
import org.wso2.msf4j.examples.petstore.util.fe.model.Cart;
import org.wso2.msf4j.examples.petstore.util.fe.model.Configuration;
import org.wso2.msf4j.examples.petstore.util.fe.model.OrderServiceException;
import org.wso2.msf4j.examples.petstore.util.fe.view.LoginBean;
import org.wso2.msf4j.examples.petstore.util.model.CreditCard;
import org.wso2.msf4j.examples.petstore.util.model.Order;
import org.wso2.msf4j.examples.petstore.util.model.Pet;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Client to access TxnServiceClient.
 */
@ManagedBean
@ApplicationScoped
public class TxnServiceClient extends AbstractServiceClient {

    @Nullable
    @ManagedProperty("#{configuration}")
    private Configuration configuration;

    private static final Logger LOGGER = Logger.getLogger(TxnServiceClient.class.getName());

    public String addOrder(Cart cart, CreditCard card) throws OrderServiceException {
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target(configuration.getTxServiceEP() + "/transaction");
        Order order = createOrder(cart, card);
        Gson gson = new Gson();
        LOGGER.info("Connecting to TXN service on " + configuration.getTxServiceEP());
        final Response response = target.request().header(LoginBean.X_JWT_ASSERTION, getJWTToken())
                .post(Entity.entity(gson.toJson(order), MediaType.APPLICATION_JSON));
        LOGGER.info("Returned from TXN service " + configuration.getTxServiceEP());
        if (Response.Status.OK.getStatusCode() == response.getStatus()) {
            return response.readEntity(String.class);
        } else {
            LOGGER.log(Level.SEVERE, "TXN service return code is  " + response.getStatus() + " , " +
                                     "hence can't proceed with the response");
            throw new OrderServiceException("Can't proceed with the order");
        }
    }

    private Order createOrder(Cart cart, CreditCard card) {
        Order order = new Order();
        List<String> orderIds = new ArrayList<>();
        for (Pet pet : cart.getItems()) {
            orderIds.add(pet.getId());
        }
        order.setPets(orderIds);
        order.setCreditCard(card);
        order.setTotal(cart.getTotal());
        return order;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
