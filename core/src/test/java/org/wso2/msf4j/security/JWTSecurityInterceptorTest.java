/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.msf4j.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.DefaultCarbonMessage;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;

import java.security.Key;
import java.security.KeyStore;
import java.security.interfaces.RSAPrivateKey;
import java.util.Date;

import static org.testng.AssertJUnit.assertTrue;

public class JWTSecurityInterceptorTest {

    @BeforeClass
    public void setup() {
        System.setProperty("PETSTORE_KEYSTORE", "wso2carbon.jks");
    }

    @Test
    public void testJWTValidation() throws Exception {

        KeyStore store = KeyStore.getInstance("JKS");
        store.load(getClass().getResourceAsStream("/wso2carbon.jks"), "wso2carbon".toCharArray());
        KeyStore primaryKeyStore = store;

        final Key wso2carbon = primaryKeyStore.getKey("wso2carbon", "wso2carbon".toCharArray());
        JWSSigner signer = new RSASSASigner((RSAPrivateKey) wso2carbon);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder().subject("wso2").issuer("https://wso2.com")
                                                           .expirationTime(new Date(new Date().getTime() + 60 * 1000))
                                                           .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);

        // Compute the RSA signature
        signedJWT.sign(signer);

        String s = signedJWT.serialize();

        CarbonMessage carbonMessage = new DefaultCarbonMessage();
        carbonMessage.setHeader("X-JWT-Assertion", s);
        Request request = new Request(carbonMessage);
        Response response = new Response(carbonMessage1 -> {
        });

        JWTSecurityInterceptor jwtSecurityInterceptor = new JWTSecurityInterceptor();
        assertTrue(jwtSecurityInterceptor.interceptRequest(request, response));

    }
}
