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

package org.wso2.carbon.mss.examples.petstore.store.client;

import com.google.gson.Gson;
import org.wso2.carbon.mss.examples.petstore.store.model.Cart;
import org.wso2.carbon.mss.examples.petstore.store.model.Configuration;
import org.wso2.carbon.mss.examples.petstore.store.model.OrderServiceException;
import org.wso2.carbon.mss.examples.petstore.store.view.LoginBean;
import org.wso2.carbon.mss.examples.petstore.util.model.CreditCard;
import org.wso2.carbon.mss.examples.petstore.util.model.Order;
import org.wso2.carbon.mss.examples.petstore.util.model.Pet;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@ManagedBean
@ApplicationScoped
public class TxnServiceClient extends AbstractServiceClient {

    @ManagedProperty("#{configuration}")
    private Configuration configuration;

    public String addOrder(Cart cart, CreditCard card) throws OrderServiceException {
        final Client client = ClientBuilder.newBuilder().build();
        final WebTarget target = client.target(configuration.getTxServiceEP() + "/transaction");
        Order order = createOrder(cart, card);
        Gson gson = new Gson();
        final Response response = target.request().header(LoginBean.X_JWT_ASSERTION, getJWTToken())
                .post(Entity.entity(gson.toJson(order), MediaType.APPLICATION_JSON));
        if (Response.Status.OK.getStatusCode() == response.getStatus()) {
            return response.readEntity(String.class);
        } else {
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
