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

package org.wso2.msf4j.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;
import org.wso2.msf4j.internal.router.beanconversion.BeanConversionException;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * Verify the JWT header in request.
 */
public class JWTSecurityInterceptor implements Interceptor {

    private final Log log = LogFactory.getLog(JWTSecurityInterceptor.class);

    private static final String JWT_HEADER = "X-JWT-Assertion";
    private static final String AUTH_TYPE_JWT = "JWT";
    private static final String KEYSTORE = SystemVariableUtil.getValue("PETSTORE_KEYSTORE", "wso2carbon.jks");
    private static final String ALIAS = SystemVariableUtil.getValue("PETSTORE_KEY_ALIAS", "wso2carbon");
    private static final String KEYSTORE_PASSWORD = SystemVariableUtil.getValue("PETSTORE_KEYSTORE_PASS", "wso2carbon");

    public boolean preCall(Request request, Response responder, ServiceMethodInfo serviceMethodInfo)
            throws BeanConversionException {
        Map<String, String> headers = request.getHeaders();
        boolean isValidSignature;
        if (headers != null) {
            String jwtHeader = headers.get(JWT_HEADER);
            if (jwtHeader != null) {
                isValidSignature = verifySignature(jwtHeader);
                if (isValidSignature) {
                    return true;
                }
            }
        }
        responder.setHeader(javax.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE, AUTH_TYPE_JWT);
        responder.setStatus(javax.ws.rs.core.Response.Status.UNAUTHORIZED.getStatusCode());
        responder.send();
        return false;
    }

    public void postCall(Request request, int status, ServiceMethodInfo serviceMethodInfo) {
        // Nothing to do
    }

    private boolean verifySignature(String jwt) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(jwt);
            if (new Date().before(signedJWT.getJWTClaimsSet().getExpirationTime())) {
                JWSVerifier verifier =
                        new RSASSAVerifier((RSAPublicKey) getPublicKey(KEYSTORE, KEYSTORE_PASSWORD, ALIAS));
                return signedJWT.verify(verifier);
            } else {
                log.info("Token has expired");
            }
        } catch (ParseException | IOException | KeyStoreException | CertificateException |
                NoSuchAlgorithmException | UnrecoverableKeyException | JOSEException e) {
            log.error("Error occurred while JWT signature verification", e);
        }
        return false;
    }

    private PublicKey getPublicKey(String keyStorePath, String keyStorePassword, String alias)
            throws IOException, KeyStoreException, CertificateException,
            NoSuchAlgorithmException, UnrecoverableKeyException {

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(keyStorePath)) {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(inputStream, keyStorePassword.toCharArray());

            Key key = keystore.getKey(alias, keyStorePassword.toCharArray());
            if (key instanceof PrivateKey) {
                // Get certificate of public key
                java.security.cert.Certificate cert = keystore.getCertificate(alias);

                // Get public key
                return cert.getPublicKey();
            }
        }
        return null;
    }
}
