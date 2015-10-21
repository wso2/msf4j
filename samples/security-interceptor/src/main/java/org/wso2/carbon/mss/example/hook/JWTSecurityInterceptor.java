/*
*Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.mss.example.hook;

import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
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
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;

/**
 * Verify the JWT header in request.
 */
public class JWTSecurityInterceptor implements Interceptor {

    private final Log log = LogFactory.getLog(JWTSecurityInterceptor.class);

    private static final String JWT_HEADER = "X-JWT-Assertion";

    private static String keyStore = "wso2carbon.jks";

    private String alias = "wso2carbon";

    private String keyStorePassword = "wso2carbon";

    public boolean preCall(HttpRequest request, HttpResponder responder, HandlerInfo handlerInfo) {

        HttpHeaders headers = request.headers();
        boolean isValidSignature = false;
        if (headers != null) {
            String jwtHeader = headers.get(JWT_HEADER);
            if (jwtHeader != null) {
                try {
                    isValidSignature = verifySignature(jwtHeader);
                } catch (Exception e) {
                    log.error("Error while JWT signature validation." + e);
                    return false;
                }
            }
        }

        log.info("## signature validation ## " + isValidSignature);
        return isValidSignature;
    }

    public void postCall(HttpRequest request, HttpResponseStatus status, HandlerInfo handlerInfo) {

    }

    private PublicKey getPublicKey(String keyStorePath, String keyStorePassword, String alias)
            throws IOException, KeyStoreException, CertificateException,
                   NoSuchAlgorithmException, UnrecoverableKeyException {

        InputStream inputStream = null;
        try {
            inputStream = this.getClass().getClassLoader().getResourceAsStream(keyStorePath);
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            keystore.load(inputStream, keyStorePassword.toCharArray());

            Key key = keystore.getKey(alias, keyStorePassword.toCharArray());
            if (key instanceof PrivateKey) {
                // Get certificate of public key
                java.security.cert.Certificate cert = keystore.getCertificate(alias);

                // Get public key
                return cert.getPublicKey();
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }

        return null;
    }

    private boolean verifySignature(String jwt) throws Exception {

        SignedJWT signedJWT = SignedJWT.parse(jwt);
        JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) getPublicKey(keyStore,
                                                                              keyStorePassword,
                                                                              alias));
        return signedJWT.verify(verifier);
    }

}
