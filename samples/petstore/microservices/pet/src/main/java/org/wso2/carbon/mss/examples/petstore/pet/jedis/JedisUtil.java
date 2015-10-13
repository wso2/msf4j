/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.mss.examples.petstore.pet.jedis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.List;
import java.util.Map;

/**
 * TODO: class level comment
 */
public class JedisUtil {
    private static final Logger log = LoggerFactory.getLogger(JedisUtil.class);

    private static final String SENTINEL1_IP = "127.0.0.1";
    private static final String SENTINEL2_IP = "127.0.0.1";

    private static final int SENTINEL1_PORT = 5000;
    private static final int SENTINEL2_PORT = 5001;

    private static final String MASTER_NAME = "mymaster";

    private static Jedis sentinel1 = new Jedis(SENTINEL1_IP, SENTINEL1_PORT);
    private static Jedis sentinel2 = new Jedis(SENTINEL2_IP, SENTINEL2_PORT);

    public static Jedis getJedis() {
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
                log.debug("master IP: {}, master port: {}", masterIP, masterPort);
                return new Jedis(masterIP, masterPort);
            }
        }
        return null;
    }

    public static void main(String[] args) {
        while (true) {
            Jedis jedis = JedisUtil.getJedis();
            if (jedis != null) {
                jedis.set("foo", "foo-" + System.currentTimeMillis());
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
