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

package com.wso2.jwt.token.builder.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

/**
* @scr.component name="carbon.custom.claim.handler.dscomponent" immediate="true"
* @scr.reference name="user.realmservice.default"
*                interface="org.wso2.carbon.user.core.service.RealmService"
*                cardinality="1..1" policy="dynamic" bind="setRealmService"
*                unbind="unsetRealmService"
* @scr.reference name="registry.service"
*                interface="org.wso2.carbon.registry.core.service.RegistryService"
*                cardinality="1..1" policy="dynamic" bind="setRegistryService"
*                unbind="unsetRegistryService"
*/
public class JWTAccessTokenBuilderDSComponent {

    private static Log log = LogFactory.getLog(JWTAccessTokenBuilderDSComponent.class);
    private static RealmService realmService;
    private static RegistryService registryService;

    protected void activate(ComponentContext ctxt) {
        try {
            log.info("Custom token builder activated successfully.");
        } catch (Exception e) {
            log.error("Failed to activate custom token builder ", e);
        }
    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.debug("Custom token builder is deactivated ");
        }
    }

    protected void setRealmService(RealmService realmService) {
        JWTAccessTokenBuilderDSComponent.realmService = realmService;
        if (log.isDebugEnabled()) {
            log.debug("RealmService is set in the custom token builder bundle");
        }

    }

    protected void unsetRealmService(RealmService realmService) {
        JWTAccessTokenBuilderDSComponent.realmService = null;
        if (log.isDebugEnabled()) {
            log.debug("RealmService is unset in the custom token builder bundle");
        }

    }

    public static RealmService getRealmService() {
        return JWTAccessTokenBuilderDSComponent.realmService;
    }

    protected void setRegistryService(RegistryService registryService) {
        JWTAccessTokenBuilderDSComponent.registryService = registryService;
        if (log.isDebugEnabled()) {
            log.debug("RegistryService is set in the custom token builder bundle");
        }

    }

    protected void unsetRegistryService(RegistryService registryService) {
        JWTAccessTokenBuilderDSComponent.registryService = null;
        if (log.isDebugEnabled()) {
            log.debug("RegistryService is unset in the custom token builder bundle");
        }

    }

    public static RegistryService getRegistryService() {
        return JWTAccessTokenBuilderDSComponent.registryService;
    }

}
