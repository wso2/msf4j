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

package org.wso2.msf4j;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.msf4j.conf.SSLConfig;
import org.wso2.msf4j.conf.SSLHandlerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Tests SSL KeyStore behaviour.
 */
public class SSLKeyStoreTest {

    public static File tmpFolder;
    private static File keyStore;

    @BeforeClass
    public static void setup() throws Exception {
        keyStore = new File(tmpFolder, "KeyStore.jks");
        keyStore.createNewFile();
        Files.copy(Thread.currentThread().getContextClassLoader().getResource("cert.jks").openStream(),
                   keyStore.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    @AfterClass
    public static void cleanup() {
        keyStore.delete();
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSslCertPathConfiguration1() throws IllegalArgumentException {
        //Bad Certificate Path
        new SSLHandlerFactory(SSLConfig.builder(new File("badCertificate"), "secret").setCertificatePassword("secret")
                .build());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSslCertPathConfiguration2() throws IllegalArgumentException {
        //Null Certificate Path
        new SSLHandlerFactory(SSLConfig.builder(null, "secret").setCertificatePassword("secret").build());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testSslKeyStorePassConfiguration2() throws IllegalArgumentException {
        //Missing Key Pass
        new SSLHandlerFactory(SSLConfig.builder(keyStore, null).setCertificatePassword("secret").build());
    }

    @Test
    public void testSslCertPassConfiguration() throws IllegalArgumentException {
        //Bad Cert Pass
        new SSLHandlerFactory(SSLConfig.builder(keyStore, "secret").build());
    }
}
