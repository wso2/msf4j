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

package org.wso2.carbon.mss.security.oauth2;

/**
 * Introspection response bean class
 */
public final class IntrospectionResponse {

    /*
     * REQUIRED. Boolean indicator of whether or not the presented token is currently active. The specifics of a token's
     * "active" state will vary depending on the implementation of the authorization server and the information it keeps
     * about its tokens, but a "true" value return for the "active" property will generally indicate that a given token
     * has been issued by this authorization server, has not been revoked by the resource owner, and is within its given
     * time window of validity (e.g., after its issuance time and before its expiration time). See Section 4 for
     * information on implementation of such checks.
     */
    public static final String ACTIVE = "active";

    /*
     * OPTIONAL. A JSON string containing a space-separated list of scopes associated with this token, in the format
     * described in Section 3.3 of OAuth 2.0
     */
    public static final String SCOPE = "scope";

    /*
     * OPTIONAL. Client identifier for the OAuth 2.0 client that requested this token.
     */
    public static final String CLIENT_ID = "client_id";

    /*
     * OPTIONAL. Human-readable identifier for the resource owner who authorized this token.
     */
    public static final String USERNAME = "username";

    /*
     * OPTIONAL. Type of the token as defined in Section 5.1 of OAuth 2.0
     */
    public static final String TOKEN_TYPE = "token_type";

    /*
     * OPTIONAL. Integer time-stamp, measured in the number of seconds since January 1 1970 UTC, indicating when this
     * token is not to be used before, as defined in JWT
     */
    public static final String NBF = "nbf";

    /*
     * OPTIONAL. Service-specific string identifier or list of string identifiers representing the intended audience for
     * this token, as defined in JWT
     */
    public static final String AUD = "aud";

    /*
     * OPTIONAL. String representing the issuer of this token, as defined in JWT
     */
    public static final String ISS = "iss";

    /*
     * OPTIONAL. String identifier for the token, as defined in JWT
     */
    public static final String JTI = "jti";

    /*
     * OPTIONAL. Subject of the token, as defined in JWT [RFC7519]. Usually a machine-readable identifier of the
     * resource owner who authorized this token.
     */
    public static final String SUB = "sub";

    /*
     * OPTIONAL. Integer time-stamp, measured in the number of seconds since January 1 1970 UTC, indicating when this
     * token will expire, as defined in JWT
     */
    public static final String EXP = "exp";

    /*
     * OPTIONAL. Integer time-stamp, measured in the number of seconds since January 1 1970 UTC, indicating when this
     * token was originally issued, as defined in JWT
     */
    public static final String IAT = "iat";


    /*
     * The request is missing a required parameter, includes an unsupported parameter value (other than grant type),
     * repeats a parameter, includes multiple credentials, utilizes more than one mechanism for authenticating the
     * client, or is otherwise malformed.
     */
    public static final String INVALID_REQUEST = "invalid_request";

    /*
     * A single ASCII [USASCII] error code
     */
    public static final String ERROR = "error";

    /*
     * Human-readable ASCII [USASCII] text providing additional information, used to assist the client developer in
     * understanding the error that occurred.
     */
    public static final String ERROR_DESCRIPTION = "error_description";
}
