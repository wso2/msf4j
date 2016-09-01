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
package org.wso2.msf4j.examples.petstore.pet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.annotation.Timed;
import org.wso2.msf4j.analytics.httpmonitoring.HTTPMonitored;
import org.wso2.msf4j.examples.petstore.util.model.Category;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * Pet category microservice.
 */
@HTTPMonitored
@Path("/category")
public class PetCategoryService {
    private static final Logger log = LoggerFactory.getLogger(PetCategoryService.class);
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
    public Response addCategory(Category category) {
        String name = category.getName();
        log.info("Using Redis master:" + REDIS_MASTER_HOST + ":" + REDIS_MASTER_PORT);
        try (Jedis jedis = pool.getResource()) {
            jedis.sadd(org.wso2.msf4j.examples.petstore.pet.PetConstants.CATEGORIES_KEY, name);
            log.info("Added category");
        }
        return Response.status(Response.Status.OK).entity("Category with name " + name + " successfully added").build();
    }

    @DELETE
    @Path("/{name}")
    @Timed
    public Response deleteCategory(@PathParam("name") String name) {
        try (Jedis jedis = pool.getResource()) {
            if (!jedis.smembers(PetConstants.CATEGORIES_KEY).contains(name)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            String categoryKey = PetConstants.CATEGORY_KEY_PREFIX + name;
            jedis.srem(PetConstants.CATEGORIES_KEY, name);
            jedis.del(categoryKey);
            log.info("Deleted category: " + name);
        }
        return Response.status(Response.Status.OK).entity("OK").build();
    }

    @GET
    @Produces("application/json")
    @Path("/{name}")
    @Timed
    public Response getCategory(@PathParam("name") String name) {
        try (Jedis jedis = pool.getResource()) {
            if (!jedis.smembers(PetConstants.CATEGORIES_KEY).contains(name)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            log.info("Got category");
        }
        return Response.status(Response.Status.OK).entity(new Category(name)).build();
    }

    @GET
    @Path("/all")
    @Produces("application/json")
    @Timed
    public Set<Category> getAllCategories() {
        try (Jedis jedis = pool.getResource()) {
            Set<String> smembers = jedis.smembers(PetConstants.CATEGORIES_KEY);
            Set<Category> categories = new HashSet<>(smembers.size());
            for (String smember : smembers) {
                categories.add(new Category(smember));
            }
            return categories;
        }
    }

}
