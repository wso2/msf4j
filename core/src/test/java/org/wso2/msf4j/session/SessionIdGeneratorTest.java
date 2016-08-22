/*
 *  Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.msf4j.session;

import org.testng.annotations.Test;
import org.wso2.msf4j.internal.session.SessionIdGenerator;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.fail;

/**
 * Tests SessionIdGenerator.
 */
public class SessionIdGeneratorTest {

    @Test
    public void test() {
        List<String> sessions = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            String id = new SessionIdGenerator().generateSessionId("foo:");
            if (sessions.contains(id)) {
                fail("Duplicate session found " + id);
            }
            sessions.add(id);
        }
    }
}
