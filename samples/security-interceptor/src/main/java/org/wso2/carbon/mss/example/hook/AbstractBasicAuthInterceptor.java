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

package org.wso2.carbon.mss.example.hook;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.mss.HttpResponder;
import org.wso2.carbon.mss.internal.router.HandlerInfo;
import org.wso2.carbon.mss.internal.router.Interceptor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPrivateKey;
import java.util.Base64;
import java.util.Date;

/**
 * AbstractBasicAuthHook hide Netty based header processing and provide authenticate()
 * method to plug-in custom authentication logic.
 */
public abstract class AbstractBasicAuthInterceptor implements Interceptor {

    private final Log log = LogFactory.getLog(AbstractBasicAuthInterceptor.class);

    private static final String AUTH_TYPE_BASIC = "Basic";

    private static final int AUTH_TYPE_BASIC_LENGTH = AUTH_TYPE_BASIC.length();

    private static final String JWT_HEADER = "X-JWT-Assertion";

    private static String keyStore = "wso2carbon.jks";

    private String alias = "wso2carbon";

    private String keyStorePassword = "wso2carbon";

    @Override
    public boolean preCall(HttpRequest request, HttpResponder responder, HandlerInfo handlerInfo) {
        HttpHeaders headers = request.headers();
        if (headers != null) {
            String authHeader = headers.get(HttpHeaders.Names.AUTHORIZATION);
            if (authHeader != null) {
                String authType = authHeader.substring(0, AUTH_TYPE_BASIC_LENGTH);
                String authEncoded = authHeader.substring(AUTH_TYPE_BASIC_LENGTH).trim();
                if (AUTH_TYPE_BASIC.equals(authType) && !authEncoded.isEmpty()) {
                    byte[] decodedByte = authEncoded.getBytes(Charset.forName("UTF-8"));
                    String authDecoded = new String(Base64.getDecoder().decode(decodedByte), Charset.forName("UTF-8"));
                    String[] authParts = authDecoded.split(":");
                    String username = authParts[0];
                    String password = authParts[1];
                    if (authenticate(username, password)) {
                        try {
                            String jwt = generateJWT(username);
                            log.info("### JWT ### " + jwt);
                            headers.add(JWT_HEADER , jwt);
                        } catch (Exception e) {
                            log.error("Error while generating JWT. " + e);
                            return false;
                        }
                        return true;
                    }
                }

            }
        }
        Multimap<String, String> map = ArrayListMultimap.create();
        map.put(HttpHeaders.Names.WWW_AUTHENTICATE, AUTH_TYPE_BASIC);
        responder.sendStatus(HttpResponseStatus.UNAUTHORIZED, map);
        return false;
    }

    @Override
    public void postCall(HttpRequest request, HttpResponseStatus status, HandlerInfo handlerInfo) {

    }

    protected abstract boolean authenticate(String username, String password);

    private String generateJWT(String userName) throws Exception {

        RSAPrivateKey privateKey = getPrivateKey(keyStore, keyStorePassword, alias);
        // Create RSA-signer with the private key
        JWSSigner signer = new RSASSASigner(privateKey);

        // Prepare JWT with claims set
        JWTClaimsSet claimsSet = new JWTClaimsSet();
        claimsSet.setSubject(userName);
        claimsSet.setIssuer("wso2.org/products/mss");
        claimsSet.setExpirationTime(new Date(new Date().getTime() + 60 * 1000));

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claimsSet);

        // Compute the RSA signature
        signedJWT.sign(signer);

        // To serialize to compact form, produces something like
        // eyJhbGciOiJSUzI1NiJ9.SW4gUlNBIHdlIHRydXN0IQ.IRMQENi4nJyp4er2L
        // mZq3ivwoAjqa1uUkSBKFIX7ATndFF5ivnt-m8uApHO4kfIFOrW7w2Ezmlg3Qd
        // maXlS9DhN0nUk_hGI3amEjkKd0BWYCB8vfUbUv0XGjQip78AI4z1PrFRNidm7
        // -jPDm5Iq0SZnjKjCNS5Q15fokXZc8u0A
        String jwt = signedJWT.serialize();

//        String jwt = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJqb2huIiwiaXNzIjoid3NvMi5vcmdcL3Byb2R1Y3RzXC9t" +
//                     "c3MiLCJleHAiOjE0NDUzMjMyMTV9.DIXVvku4AAuwmCs9NH2h_uAwC1Nh2GxaXe11mcKaHt06lalQ" +
//                     "4QBUvl6GnFoVNf0SQAeWdjNbCYqMRsgrbSphqwRcAfk4LnhtTuC4En4GaPvCJs61QMGfdq4sEcyq" +
//                     "3Puqt2c82L57uE1NtdzimT_vQUKq3O6hHqaT7aJ14OQuWp4aaa";

        return jwt;

    }

    private RSAPrivateKey getPrivateKey(String keyStorePath, String keyStorePassword, String alias)
            throws IOException, KeyStoreException, CertificateException,
                   NoSuchAlgorithmException, UnrecoverableKeyException {

        InputStream inputStream = null;
        try {
            inputStream = this.getClass().getClassLoader().getResourceAsStream(keyStorePath);
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(inputStream, keyStorePassword.toCharArray());

            RSAPrivateKey privateKey = (RSAPrivateKey) keystore.getKey(alias, keyStorePassword.toCharArray());
            return privateKey;

        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
}
