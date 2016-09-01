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
package org.wso2.msf4j.examples.petstore.transaction;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.annotation.Timed;
import org.wso2.msf4j.analytics.httpmonitoring.HTTPMonitored;
import org.wso2.msf4j.examples.petstore.util.model.Order;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Transaction microservice.
 */
@HTTPMonitored
@Path("/transaction")
public class TxnService {
    private static final Logger log = LoggerFactory.getLogger(TxnService.class);
    private static String REDIS_MASTER_HOST = System.getenv("REDIS_MASTER_HOST");
    private static int REDIS_MASTER_PORT = Integer.parseInt(System.getenv("REDIS_MASTER_PORT"));

    static {
        log.info("Using Redis master:" + REDIS_MASTER_HOST + ":" + REDIS_MASTER_PORT);
    }

    private static final JedisPool pool =
            new JedisPool(new JedisPoolConfig(), REDIS_MASTER_HOST, REDIS_MASTER_PORT);

    @POST
    @Consumes("application/json")
    @Timed
    public Response addOrder(Order order) {
        String orderId = order.getId();
        try (Jedis jedis = pool.getResource()) {
            if (!jedis.smembers(TxnConstants.ORDERS_KEY).contains(orderId)) {
                jedis.sadd(TxnConstants.ORDERS_KEY, orderId);
            }
            String orderKey = TxnConstants.ORDER_KEY_PREFIX + orderId;
            if (jedis.get(orderKey) != null) {
                return Response.status(Response.Status.CONFLICT).
                        entity("Order with ID " + orderId + " already exists").build();
            } else {
                jedis.set(orderKey, new Gson().toJson(order));
                log.info("Added order");
            }
            // We are ignoring the credit card details. In the real world, this is where we would make a call to the
            // payment gateway
            return Response.status(Response.Status.OK).entity(new Gson().toJson(orderId)).build();
        }
    }

    @GET
    @Path("/all")
    @Produces("application/json")
    @Timed
    public List<Order> getOrders(String txnId) {
        try (Jedis jedis = pool.getResource()) {
            Set<String> orderKeys = jedis.smembers(TxnConstants.ORDERS_KEY);
            List<Order> result = new ArrayList<>(orderKeys.size());
            for (String orderKey : orderKeys) {
                String orderValue = jedis.get(orderKey);
                result.add(new Gson().fromJson(orderValue, Order.class));
            }
            return result;
        }
    }
}
