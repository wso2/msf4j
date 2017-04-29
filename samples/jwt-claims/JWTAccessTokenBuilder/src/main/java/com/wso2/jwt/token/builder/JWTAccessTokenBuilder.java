/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.wso2.jwt.token.builder;

import java.security.Key;
import java.security.interfaces.RSAPrivateKey;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.util.KeyStoreManager;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.application.common.model.Claim;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth.config.OAuthServerConfiguration;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.authz.OAuthAuthzReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.OAuthTokenReqMessageContext;
import org.wso2.carbon.identity.oauth2.token.OauthTokenIssuerImpl;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import com.nimbusds.jwt.SignedJWT;

/**
 * JWTAccessTokenBuilder.
 */
public class JWTAccessTokenBuilder extends OauthTokenIssuerImpl {

    /**
     * Signature algorithms
     */
    private static final String NONE = "NONE";
    private static final String SHA256_WITH_RSA = "SHA256withRSA";
    private static final String SHA384_WITH_RSA = "SHA384withRSA";
    private static final String SHA512_WITH_RSA = "SHA512withRSA";
    private static final String SHA256_WITH_HMAC = "SHA256withHMAC";
    private static final String SHA384_WITH_HMAC = "SHA384withHMAC";
    private static final String SHA512_WITH_HMAC = "SHA512withHMAC";
    private static final String SHA256_WITH_EC = "SHA256withEC";
    private static final String SHA384_WITH_EC = "SHA384withEC";
    private static final String SHA512_WITH_EC = "SHA512withEC";

    private static final Log log = LogFactory.getLog(JWTAccessTokenBuilder.class);
    /**
     * Map for private keys
     */
    private static Map<Integer, Key> privateKeys = new ConcurrentHashMap<Integer, Key>();
    private OAuthServerConfiguration config = null;
    private Algorithm signatureAlgorithm = null;
    
    private UserStoreManager userStoreManager;
    
    public JWTAccessTokenBuilder() throws IdentityOAuth2Exception {
        if (log.isDebugEnabled()) {
            log.debug("JWT Access token builder is initiated");
        }
        try {
			userStoreManager = CarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager();
		} catch (UserStoreException e) {
			log.error(e.getMessage(), e);
		}
        config = OAuthServerConfiguration.getInstance();
        //map signature algorithm from identity.xml to nimbus format, this is a one time configuration
        signatureAlgorithm = mapSignatureAlgorithm(config.getSignatureAlgorithm());
    }

    public String accessToken(OAuthTokenReqMessageContext oAuthTokenReqMessageContext) throws OAuthSystemException {
        if (log.isDebugEnabled()) {
            log.debug("Access token request with token request message context. Authorized user " +
                    oAuthTokenReqMessageContext.getAuthorizedUser().toString());
        }
        try {
            return this.buildIDToken(oAuthTokenReqMessageContext);
        } catch (IdentityOAuth2Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while issuing jwt access token. Hence returning default token", e);
            }
            // Return default access token if it fails to build jwt
            return super.accessToken(oAuthTokenReqMessageContext);
        } catch (UserStoreException e) {
        	if (log.isDebugEnabled()) {
                log.debug("Error occurred while access user store", e);
            }
            // Return default access token if it fails to build jwt
            return super.accessToken(oAuthTokenReqMessageContext);
		}
    }


    public String accessToken(OAuthAuthzReqMessageContext oAuthAuthzReqMessageContext) throws OAuthSystemException {
        if (log.isDebugEnabled()) {
            log.debug("Access token request with authorization request message context message context. Authorized " +
                    "user " + oAuthAuthzReqMessageContext.getAuthorizationReqDTO().getUser().toString());
        }
        try {
            return this.buildIDToken(oAuthAuthzReqMessageContext);
        } catch (IdentityOAuth2Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Error occurred while issuing jwt access token. Hence returning default token", e);
            }
            // Return default access token if it fails to build jwt
            return super.accessToken(oAuthAuthzReqMessageContext);
        }
    }

    /**
     * To build id token from OauthToken request message context
     *
     * @param request Token request message context
     * @return Signed jwt string.
     * @throws IdentityOAuth2Exception
     * @throws UserStoreException 
     */
    protected String buildIDToken(OAuthTokenReqMessageContext request)
            throws IdentityOAuth2Exception, UserStoreException {

        String issuer = OAuth2Util.getIDTokenIssuer();
        long lifetimeInMillis = OAuthServerConfiguration.getInstance().
                getApplicationAccessTokenValidityPeriodInSeconds() * 1000;
        long curTimeInMillis = Calendar.getInstance().getTimeInMillis();
        // setting subject
        String subject = request.getAuthorizedUser().getUserName();
        if (!StringUtils.isNotBlank(subject)) {
            subject = request.getAuthorizedUser().getAuthenticatedSubjectIdentifier();
        }
        String[] roles = userStoreManager.getRoleListOfUser(subject);
        // Set claims to jwt token.
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet();
        jwtClaimsSet.setIssuer(issuer);
        jwtClaimsSet.setSubject(subject);
        jwtClaimsSet.setAudience(Arrays.asList(request.getOauth2AccessTokenReqDTO().getClientId()));
        jwtClaimsSet.setClaim("grupos", roles);
        jwtClaimsSet.setExpirationTime(new Date(curTimeInMillis + lifetimeInMillis));
        jwtClaimsSet.setIssueTime(new Date(curTimeInMillis));
        addUserClaims(jwtClaimsSet, request.getAuthorizedUser());

        if (JWSAlgorithm.NONE.getName().equals(signatureAlgorithm.getName())) {
            return new PlainJWT(jwtClaimsSet).serialize();
        }
        return signJWT(jwtClaimsSet, request);
    }

    /**
     * Build a signed jwt token from authorization request message context
     *
     * @param request Oauth authorization message context
     * @return Signed jwt string
     * @throws IdentityOAuth2Exception
     */
    protected String buildIDToken(OAuthAuthzReqMessageContext request)
            throws IdentityOAuth2Exception {

        String issuer = OAuth2Util.getIDTokenIssuer();
        long lifetimeInMillis = OAuthServerConfiguration.getInstance().
                getApplicationAccessTokenValidityPeriodInSeconds() * 1000;
        long curTimeInMillis = Calendar.getInstance().getTimeInMillis();
        // setting subject
        String subject = request.getAuthorizationReqDTO().getUser().getUserName();

        if (!StringUtils.isNotBlank(subject)) {
            subject = request.getAuthorizationReqDTO().getUser().getAuthenticatedSubjectIdentifier();
        }
        
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet();
        jwtClaimsSet.setIssuer(issuer);
        jwtClaimsSet.setSubject(subject);
        jwtClaimsSet.setAudience(Arrays.asList(request.getAuthorizationReqDTO().getConsumerKey()));
        jwtClaimsSet.setClaim("grupos", Arrays.asList("ENTREGADOR", "ADMIN"));
        jwtClaimsSet.setExpirationTime(new Date(curTimeInMillis + lifetimeInMillis));
        jwtClaimsSet.setIssueTime(new Date(curTimeInMillis));
        addUserClaims(jwtClaimsSet, request.getAuthorizationReqDTO().getUser());
        
        if (JWSAlgorithm.NONE.getName().equals(signatureAlgorithm.getName())) {
            return new PlainJWT(jwtClaimsSet).serialize();
        }
        return signJWT(jwtClaimsSet, request);
    }

    /**
     * sign JWT token from RSA algorithm
     *
     * @param jwtClaimsSet contains JWT body
     * @param request
     * @return signed JWT token
     * @throws IdentityOAuth2Exception
     */
    protected String signJWTWithRSA(JWTClaimsSet jwtClaimsSet, OAuthTokenReqMessageContext request)
            throws IdentityOAuth2Exception {
        try {

            String tenantDomain = request.getAuthorizedUser().getTenantDomain();
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);

            Key privateKey;

            if (!(privateKeys.containsKey(tenantId))) {
                // get tenant's key store manager
                KeyStoreManager tenantKSM = KeyStoreManager.getInstance(tenantId);

                if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                    // derive key store name
                    String ksName = tenantDomain.trim().replace(".", "-");
                    String jksName = ksName + Constants.KEY_STORE_EXTENSION;
                    // obtain private key
                    privateKey = tenantKSM.getPrivateKey(jksName, tenantDomain);

                } else {
                    try {
                        privateKey = tenantKSM.getDefaultPrivateKey();
                    } catch (Exception e) {
                        throw new IdentityOAuth2Exception("Error while obtaining private key for super tenant", e);
                    }
                }
                //privateKey will not be null always
                privateKeys.put(tenantId, privateKey);
            } else {
                //privateKey will not be null because containsKey() true says given key is exist and ConcurrentHashMap
                // does not allow to store null values
                privateKey = privateKeys.get(tenantId);
            }
            JWSSigner signer = new RSASSASigner((RSAPrivateKey) privateKey);
            SignedJWT signedJWT = null;
            if (signatureAlgorithm instanceof JWSAlgorithm) {
                signedJWT = new SignedJWT(new JWSHeader((JWSAlgorithm) signatureAlgorithm), jwtClaimsSet);
                signedJWT.sign(signer);
                return signedJWT.serialize();
            }
        } catch (JOSEException e) {
            throw new IdentityOAuth2Exception("Error occurred while signing JWT", e);
        }
        return null;
    }

    protected String signJWTWithRSA(JWTClaimsSet jwtClaimsSet, OAuthAuthzReqMessageContext request)
            throws IdentityOAuth2Exception {
        try {

            // All applications are registered under super tenant domain and currently we dont have access to SP
            // tenant domain.
            String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
            int tenantId = IdentityTenantUtil.getTenantId(tenantDomain);
            Key privateKey;

            if (!(privateKeys.containsKey(tenantId))) {
                // get tenant's key store manager
                KeyStoreManager tenantKSM = KeyStoreManager.getInstance(tenantId);

                if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
                    // derive key store name
                    String ksName = tenantDomain.trim().replace(".", "-");
                    String jksName = ksName + Constants.KEY_STORE_EXTENSION;
                    // obtain private key
                    privateKey = tenantKSM.getPrivateKey(jksName, tenantDomain);

                } else {
                    try {
                        privateKey = tenantKSM.getDefaultPrivateKey();
                    } catch (Exception e) {
                        throw new IdentityOAuth2Exception("Error while obtaining private key for super tenant", e);
                    }
                }
                //privateKey will not be null always
                privateKeys.put(tenantId, privateKey);
            } else {
                //privateKey will not be null because containsKey() true says given key is exist and ConcurrentHashMap
                // does not allow to store null values
                privateKey = privateKeys.get(tenantId);
            }
            JWSSigner signer = new RSASSASigner((RSAPrivateKey) privateKey);
            SignedJWT signedJWT = null;
            if (signatureAlgorithm instanceof JWSAlgorithm) {
                signedJWT = new SignedJWT(new JWSHeader((JWSAlgorithm) signatureAlgorithm), jwtClaimsSet);
                signedJWT.sign(signer);
                return signedJWT.serialize();
            }
        } catch (JOSEException e) {
            throw new IdentityOAuth2Exception("Error occurred while signing JWT", e);
        }
        return null;
    }

    /**
     * Generic Signing function
     *
     * @param jwtClaimsSet contains JWT body
     * @param request
     * @return
     * @throws IdentityOAuth2Exception
     */
    protected String signJWT(JWTClaimsSet jwtClaimsSet, OAuthTokenReqMessageContext request)
            throws IdentityOAuth2Exception {

        if (JWSAlgorithm.RS256.equals(signatureAlgorithm) || JWSAlgorithm.RS384.equals(signatureAlgorithm) ||
                JWSAlgorithm.RS512.equals(signatureAlgorithm)) {
            return signJWTWithRSA(jwtClaimsSet, request);
        } else if (JWSAlgorithm.HS256.equals(signatureAlgorithm) || JWSAlgorithm.HS384.equals(signatureAlgorithm) ||
                JWSAlgorithm.HS512.equals(signatureAlgorithm)) {
            // return signWithHMAC(jwtClaimsSet,jwsAlgorithm,request); implementation need to be done
            return null;
        } else {
            // return signWithEC(jwtClaimsSet,jwsAlgorithm,request); implementation need to be done
            return null;
        }
    }

    protected String signJWT(JWTClaimsSet jwtClaimsSet, OAuthAuthzReqMessageContext request)
            throws IdentityOAuth2Exception {

        if (JWSAlgorithm.RS256.equals(signatureAlgorithm) || JWSAlgorithm.RS384.equals(signatureAlgorithm) ||
                JWSAlgorithm.RS512.equals(signatureAlgorithm)) {
            return signJWTWithRSA(jwtClaimsSet, request);
        } else if (JWSAlgorithm.HS256.equals(signatureAlgorithm) || JWSAlgorithm.HS384.equals(signatureAlgorithm) ||
                JWSAlgorithm.HS512.equals(signatureAlgorithm)) {
            // return signWithHMAC(jwtClaimsSet,jwsAlgorithm,request); implementation need to be done
            return null;
        } else {
            // return signWithEC(jwtClaimsSet,jwsAlgorithm,request); implementation need to be done
            return null;
        }
    }

    /**
     * This method map signature algorithm define in identity.xml to nimbus
     * signature algorithm
     * format, Strings are defined inline hence there are not being used any
     * where
     *
     * @param signatureAlgorithm
     * @return
     * @throws IdentityOAuth2Exception
     */
    protected JWSAlgorithm mapSignatureAlgorithm(String signatureAlgorithm) throws IdentityOAuth2Exception {

        if (NONE.equals(signatureAlgorithm)) {
            return new JWSAlgorithm(JWSAlgorithm.NONE.getName());
        } else if (SHA256_WITH_RSA.equals(signatureAlgorithm)) {
            return JWSAlgorithm.RS256;
        } else if (SHA384_WITH_RSA.equals(signatureAlgorithm)) {
            return JWSAlgorithm.RS384;
        } else if (SHA512_WITH_RSA.equals(signatureAlgorithm)) {
            return JWSAlgorithm.RS512;
        } else if (SHA256_WITH_HMAC.equals(signatureAlgorithm)) {
            return JWSAlgorithm.HS256;
        } else if (SHA384_WITH_HMAC.equals(signatureAlgorithm)) {
            return JWSAlgorithm.HS384;
        } else if (SHA512_WITH_HMAC.equals(signatureAlgorithm)) {
            return JWSAlgorithm.HS512;
        } else if (SHA256_WITH_EC.equals(signatureAlgorithm)) {
            return JWSAlgorithm.ES256;
        } else if (SHA384_WITH_EC.equals(signatureAlgorithm)) {
            return JWSAlgorithm.ES384;
        } else if (SHA512_WITH_EC.equals(signatureAlgorithm)) {
            return JWSAlgorithm.ES512;
        }
        throw new IdentityOAuth2Exception("Unsupported Signature Algorithm in identity.xml");
    }

    private void addUserClaims(JWTClaimsSet jwtClaimsSet, AuthenticatedUser user) {
        for (Map.Entry<ClaimMapping, String> entry : user.getUserAttributes().entrySet()) {
            ClaimMapping claimMapping = entry.getKey();
            Claim claim = claimMapping.getLocalClaim();
            if (claim != null && Constants.CUSTOMER_ID_CLAIM_URI.equalsIgnoreCase(claim.getClaimUri())) {
                jwtClaimsSet.setClaim(Constants.CUSTOMER_ID_CLAIM_URI, entry.getValue());
            }
        }
    }

}
