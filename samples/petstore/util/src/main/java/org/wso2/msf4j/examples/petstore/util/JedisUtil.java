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
package org.wso2.msf4j.examples.petstore.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.util.SystemVariableUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Utility class for connecting to Redis &amp; handling Redis calls.
 */
public class JedisUtil {
    private static final Logger log = LoggerFactory.getLogger(JedisUtil.class);

    private static final String SENTINEL1_HOST;
    private static final String SENTINEL2_HOST;

    private static final int SENTINEL1_PORT;
    private static final int SENTINEL2_PORT;

    private static final String MASTER_NAME = "mymaster";

    private static Jedis sentinel1;
    private static Jedis sentinel2;

    private static ReentrantLock lock = new ReentrantLock();

    static {
        SENTINEL1_HOST = SystemVariableUtil.getValue("SENTINEL1_HOST", "127.0.0.1");
        SENTINEL2_HOST = SystemVariableUtil.getValue("SENTINEL2_HOST", "127.0.0.1");
        SENTINEL1_PORT = Integer.parseInt(SystemVariableUtil.getValue("SENTINEL1_PORT", "5000"));
        SENTINEL2_PORT = Integer.parseInt(SystemVariableUtil.getValue("SENTINEL2_PORT", "5001"));

        sentinel1 = new Jedis(SENTINEL1_HOST, SENTINEL1_PORT);
        sentinel2 = new Jedis(SENTINEL2_HOST, SENTINEL2_PORT);

        log.info("Sentinel 1: " + SENTINEL1_HOST + ":" + SENTINEL1_PORT);
        log.info("Sentinel 2: " + SENTINEL2_HOST + ":" + SENTINEL2_PORT);
    }

    private static Jedis master = getJedis();

    private JedisUtil() {}

    public static String getSentinelHost() {
        return SENTINEL1_HOST;
    }

    public static int getSentinelPort() {
        return SENTINEL1_PORT;
    }

    public static void set(String key, String value) {
        fetchMaster();
        try {
            master.set(key, value);
        } catch (JedisConnectionException e) {
            master = getJedis();
            master.set(key, value);
        }
    }

    public static String get(String key) {
        fetchMaster();
        try {
            return master.get(key);
        } catch (JedisConnectionException e) {
            master = getJedis();
            return master.get(key);
        }
    }

    public static void del(String key) {
        fetchMaster();
        try {
            master.del(key);
        } catch (JedisConnectionException e) {
            master = getJedis();
            master.del(key);
        }
    }

    public static void sadd(String key, String value) {
        fetchMaster();
        try {
            master.sadd(key, value);
        } catch (JedisConnectionException e) {
            master = getJedis();
            master.sadd(key, value);
        }
    }

    public static Set<String> smembers(String key) {
        fetchMaster();
        try {
            return master.smembers(key);
        } catch (JedisConnectionException e) {
            master = getJedis();
            return master.smembers(key);
        }
    }

    public static void srem(String key, String value) {
        fetchMaster();
        try {
            master.srem(key, value);
        } catch (JedisConnectionException e) {
            master = getJedis();
            master.sadd(key, value);
        }
    }

    public static List<String> getValues(String keyPattern) {
        fetchMaster();
        List<String> values = new ArrayList<>();
        try {
            getValuesInternal(keyPattern, values);
        } catch (JedisConnectionException e) {
            master = getJedis();
            getValuesInternal(keyPattern, values);
        }
        return values;
    }

    private static void getValuesInternal(String keyPattern, List<String> values) {
        Set<String> keys = master.keys(keyPattern);
        values.addAll(keys.stream().map(master::get).collect(Collectors.toList()));
    }

    private static void fetchMaster() {
        if (master == null) {
            lock.lock();
            if (master == null) {
                master = getJedis();
            }
        }
    }

    private static Jedis getJedis() {
        try {
            log.info("Using sentinel: " + sentinel1);
            return getJedisInternal(sentinel1);
        } catch (JedisConnectionException e) {
            sentinel1.close();
            Jedis tempSentinel = sentinel1;
            sentinel1 = sentinel2;
            sentinel2 = tempSentinel;
            log.info("Using sentinel: " + sentinel1);
            return getJedisInternal(sentinel1);
        }
    }

    private static Jedis getJedisInternal(Jedis sentinel) {
        List<Map<String, String>> masters = sentinel.sentinelMasters();
        for (Map<String, String> master : masters) {
            if (MASTER_NAME.equals(master.get("name"))) {
                String masterIP = master.get("ip");
                int masterPort = Integer.parseInt(master.get("port"));
                log.info("Redis master : {}:{}", masterIP, masterPort);
                return new Jedis(masterIP, masterPort);
            }
        }
        return null;
    }
}
