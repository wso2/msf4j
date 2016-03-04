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

package org.wso2.msf4j.examples.petstore.security.ldap.server;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.log4j.Logger;
import org.wso2.carbon.apacheds.AdminGroupInfo;
import org.wso2.carbon.apacheds.AdminInfo;
import org.wso2.carbon.apacheds.KdcConfiguration;
import org.wso2.carbon.apacheds.LDAPConfiguration;
import org.wso2.carbon.apacheds.PartitionInfo;
import org.wso2.carbon.apacheds.PasswordAlgorithm;
import org.wso2.carbon.ldap.server.exception.DirectoryServerException;
import org.wso2.carbon.ldap.server.util.EmbeddingLDAPException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This class is responsible for building LDAP and KDC configurations. Given a file
 * name this will read configuration values and populate them into configuration classes.
 * Related configuration file is embedded-ldap.xml.
 */
public class LDAPServerConfigurationBuilder {

    private Logger logger = Logger.getLogger(LDAPServerConfigurationBuilder.class);

    public static final java.lang.String LOCAL_NAME_PROPERTY = "Property";

    public static final java.lang.String ATTR_NAME_PROP_NAME = "name";

    private InputStream configurationFileStream;
    /*Password to connect with the embedded-ldap server*/
    private String connectionPassword;
    /*contains embedded-ldap server configurations*/
    private LDAPConfiguration ldapConfiguration;
    /*contains default partition's configurations*/
    private PartitionInfo partitionConfigurations;
    /*contains KDC server configurations*/
    private KdcConfiguration kdcConfigurations;

    private boolean kdcEnabled = false;

    private static final String CARBON_KDC_PORT_CONFIG_SECTION = "Ports.EmbeddedLDAP.KDCServerPort";
    private static final int DEFAULT_KDC_SERVER_PORT = 8000;

    /**
     * Constructor with the configuration file as input that reads the file into an InputStream.
     *
     * @param file that includes embedded-ldap server configurations.
     * @throws FileNotFoundException
     */
    public LDAPServerConfigurationBuilder(File file) throws FileNotFoundException {

        if (!file.exists()) {
            String msg = "File not found. - " + file.getAbsolutePath();
            logger.error(msg);
            throw new FileNotFoundException(msg);
        }

        try {
            configurationFileStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            String msg = "Could not open file - " + file.getAbsolutePath();
            logger.error(msg);
            throw new FileNotFoundException(msg);
        }

    }

    /**
     * Build separate sections of the configuration file and the configuration file as a whole, as
     * OMElements.
     *
     * @throws org.wso2.carbon.ldap.server.util.EmbeddingLDAPException
     */
    public void buildConfigurations() throws EmbeddingLDAPException {

        StAXOMBuilder builder;

        try {
            builder = new StAXOMBuilder(configurationFileStream);
        } catch (XMLStreamException e) {
            logger.error("Unable to build LDAP configurations.", e);
            throw new EmbeddingLDAPException("Unable to build LDAP configurations", e);
        }
        /*Read the whole config file as an OMElement*/
        OMElement documentElement = builder.getDocumentElement();

        /*Extract the part that contains embedded-ldap specific configurations*/
        OMElement embeddedLdap = documentElement.getFirstChildWithName(new QName("EmbeddedLDAP"));
        /*Set properties in ldapConfiguration object from those read from the config element.*/
        buildLDAPConfigurations(embeddedLdap);

        if (ldapConfiguration.isEnable()) {
            /*Set properties in partitionConfigurations object from those read from the config file.*/
            buildPartitionConfigurations(documentElement);

            /*Extract the part that contains kdc-server specific configurations*/
            OMElement kdcConfigElement = documentElement.getFirstChildWithName(new QName("KDCServer"));
            /*Set properties in kdcConfiguration object from those read from the config element.*/
            buildKDCConfigurations(kdcConfigElement);

            /*Says root partition that KDC is enabled. Root partition admin should have KDC object
          attributes in LDAP*/
            this.partitionConfigurations.setKdcEnabled(this.kdcEnabled);

            // Do some cross checking
            if (this.kdcEnabled) {

                this.kdcConfigurations.setSystemAdminPassword(this.getConnectionPassword());

                // Set admin partition for KDC
                this.kdcConfigurations.setPartitionInfo(this.getPartitionConfigurations());

            }
        }
    }

    public String getConnectionPassword() throws EmbeddingLDAPException {
        if (connectionPassword == null) {
            buildConfigurations();
        }
        return connectionPassword;
    }

    public LDAPConfiguration getLdapConfiguration() throws EmbeddingLDAPException {
        if (ldapConfiguration == null) {
            buildConfigurations();
        }
        return ldapConfiguration;
    }

    /**
     * Read and set the connection password from the property map.
     *
     * @param propertyMap : containing properties of EmbeddedLDAP Element.
     * @throws org.wso2.carbon.ldap.server.util.EmbeddingLDAPException
     */
    private void buildConnectionPassword(Map<String, String> propertyMap)
            throws EmbeddingLDAPException {

        connectionPassword = propertyMap.get("connectionPassword");
        if (connectionPassword == null) {
            throw new EmbeddingLDAPException("Connection password not specified in the " +
                    "configuration file.");
        }

    }

    /**
     * Read properties from EmbeddedLDAP element in configuration and set them in the
     * ldapConfiguration object.
     *
     * @param embeddedLDAP: part of the XML config file named: EmbeddedLDAP
     * @throws org.wso2.carbon.ldap.server.util.EmbeddingLDAPException
     */
    private void buildLDAPConfigurations(OMElement embeddedLDAP) throws EmbeddingLDAPException {
        /*Read the properties of EmbeddedLDAP XML element.*/
        Map<String, String> propertyMap = getChildPropertyElements(embeddedLDAP);

        ldapConfiguration = new LDAPConfiguration();
        /*set connectionPassword*/
        buildConnectionPassword(propertyMap);
        String booleanString;

        if ((booleanString = propertyMap.get("accessControlEnabled")) != null) {
            ldapConfiguration.setAccessControlOn(Boolean.parseBoolean(booleanString));
        }

        if ((booleanString = propertyMap.get("allowAnonymousAccess")) != null) {
            ldapConfiguration.setAllowAnonymousAccess(Boolean.parseBoolean(booleanString));
        }

        if ((booleanString = propertyMap.get("changedLogEnabled")) != null) {
            ldapConfiguration.setChangeLogEnabled(Boolean.parseBoolean(booleanString));
        }

        if ((booleanString = propertyMap.get("denormalizeOpAttrsEnabled")) != null) {
            ldapConfiguration.setDeNormalizedAttributesEnabled(Boolean.parseBoolean(booleanString));
        }
        //check and set whether embedded ldap is enabled.
        String enableInfo = propertyMap.get("enable");

        if (("true").equals(enableInfo)) {
            ldapConfiguration.setEnable(true);
        } else {
            ldapConfiguration.setEnable(false);
        }
        ldapConfiguration.setInstanceId(propertyMap.get("instanceId"));

        //Set LDAP server port
        ldapConfiguration.setLdapPort(Integer.parseInt(propertyMap.get("port")));

        ldapConfiguration.setWorkingDirectory(propertyMap.get("workingDirectory"));
        ldapConfiguration.setAdminEntryObjectClass(propertyMap.get("AdminEntryObjectClass"));
        ldapConfiguration.setMaxPDUSize(getIntegerValue(propertyMap.get("maxPDUSize")));
        ldapConfiguration.setSaslHostName(propertyMap.get("saslHostName"));
        ldapConfiguration.setSaslPrincipalName(propertyMap.get("saslPrincipalName"));
    }

    private int getIntegerValue(String value) {
        if (value != null) {
            return Integer.parseInt(value);
        }

        return -1;

    }

    /**
     * Reads the properties mentioned under the XML Element in the config file, which is passed
     * as an OMElement to the method.
     *
     * @param omElement : main XML element whose properties should be read
     * @return : the map containing property names and the values.
     */
    private Map<String, String> getChildPropertyElements(OMElement omElement) {
        Map<String, String> map = new HashMap<String, String>();
        Iterator<?> ite = omElement.getChildrenWithName(new QName(LOCAL_NAME_PROPERTY));
        while (ite.hasNext()) {
            OMElement propElem = (OMElement) ite.next();
            String propName = propElem.getAttributeValue(new QName(ATTR_NAME_PROP_NAME));
            String propValue = propElem.getText();
            map.put(propName, propValue);
        }
        return map;
    }

    /**
     * Read properties related to default partition and set them in partitionConfigurations object.
     *
     * @param documentElement: whole config file read as an OMElement.
     *                         Following parts are read from the config file:
     *                         <p>
     *                         Default partition configurations
     *                         <p>
     *                         &lt;DefaultPartition&gt;
     *                         &lt;Property name="id"&gt;root &lt;/Property&gt;
     *                         &lt;Property name="realm"&gt;wso2.com &lt;/Property&gt;
     *                         &lt;Property name="kdcPassword"&gt;secret &lt;/Property&gt;
     *                         &lt;Property name="ldapServerPrinciplePassword"&gt;randall &lt;/Property&gt;
     *                         &lt;/DefaultPartition&gt;
     *                         <p>
     *                         Default partition admin configurations
     *                         <p>
     *                         &lt;PartitionAdmin&gt;
     *                         &lt;Property name="uid"&gt;admin&lt;/Property&gt;
     *                         &lt;Property name="commonName"&gt;admin&lt;/Property&gt;
     *                         &lt;Property name="lastName"&gt;admin&lt;/Property&gt;
     *                         &lt;Property name="email"&gt;admin&lt;/Property&gt;
     *                         &lt;Property name="password"&gt;admin&lt;/Property&gt;
     *                         &lt;Property name="passwordType"&gt;SHA&lt;/Property&gt;
     *                         &lt;/PartitionAdmin>
     *                         <p>
     *                         Default partition admin's group configuration
     *                         <p>
     *                         &lt;PartitionAdminGroup&gt;
     *                         &lt;Property name="adminRoleName"&gt;admin&lt;/Property&gt;
     *                         &lt;Property name="groupNameAttribute"&gt;cn&lt;/Property&gt;
     *                         &lt;Property name="memberNameAttribute"&gt;member&lt;/Property&gt;
     *                         &lt;/PartitionAdminGroup&gt;
     */
    private void buildPartitionConfigurations(OMElement documentElement) {

        this.partitionConfigurations = new PartitionInfo();

        OMElement defaultPartition = documentElement.getFirstChildWithName(new QName("DefaultPartition"));
        Map<String, String> propertyMap = getChildPropertyElements(defaultPartition);

        this.partitionConfigurations.setPartitionId(propertyMap.get("id"));
        this.partitionConfigurations.setRealm(propertyMap.get("realm"));

        this.partitionConfigurations.setPartitionKdcPassword(propertyMap.get("kdcPassword"));
        this.partitionConfigurations.setLdapServerPrinciplePassword(propertyMap.get("ldapServerPrinciplePassword"));
        this.partitionConfigurations.setRootDN(getDomainNameForRealm(propertyMap.get("realm")));

        // Admin user config
        OMElement partitionAdmin = documentElement.getFirstChildWithName(new QName("PartitionAdmin"));
        propertyMap = getChildPropertyElements(partitionAdmin);
        AdminInfo defaultPartitionAdmin = buildPartitionAdminConfigurations(propertyMap);

        // Admin role config
        OMElement partitionAdminRole = documentElement.getFirstChildWithName(new QName("PartitionAdminGroup"));
        propertyMap = getChildPropertyElements(partitionAdminRole);
        AdminGroupInfo adminGroupInfo = buildPartitionAdminGroupConfigurations(propertyMap);

        defaultPartitionAdmin.setGroupInformation(adminGroupInfo);
        this.partitionConfigurations.setPartitionAdministrator(defaultPartitionAdmin);

    }

    private AdminInfo buildPartitionAdminConfigurations(Map<String, String> propertyMap) {
        AdminInfo adminInfo = new AdminInfo();

        adminInfo.setAdminUserName(propertyMap.get("uid"));
        adminInfo.setAdminCommonName(propertyMap.get("firstName"));
        adminInfo.setAdminLastName(propertyMap.get("lastName"));
        adminInfo.setAdminEmail(propertyMap.get("email"));
        adminInfo.setAdminPassword(propertyMap.get("password"));
        adminInfo.setPasswordAlgorithm(PasswordAlgorithm.valueOf(propertyMap.get("passwordType")));
        adminInfo.addObjectClass(ldapConfiguration.getAdminEntryObjectClass());
        adminInfo.setUsernameAttribute("uid");

        return adminInfo;
    }

    private AdminGroupInfo buildPartitionAdminGroupConfigurations(Map<String, String> propertyMap) {
        AdminGroupInfo adminGroupInfo = new AdminGroupInfo();
        adminGroupInfo.setAdminRoleName(propertyMap.get("adminRoleName"));
        adminGroupInfo.setGroupNameAttribute(propertyMap.get("groupNameAttribute"));
        adminGroupInfo.setMemberNameAttribute(propertyMap.get("memberNameAttribute"));
        return adminGroupInfo;
    }

    private String getDomainNameForRealm(String realm) {

        if (realm == null) {
            return null;
        }

        String[] components = realm.split("\\.");

        if (components.length == 0) {
            return "dc=" + realm;
        }

        StringBuilder domainName = new StringBuilder();

        for (int i = 0; i < components.length; ++i) {
            domainName.append("dc=");
            domainName.append(components[i]);

            if (i != (components.length - 1)) {
                domainName.append(",");
            }
        }

        return domainName.toString();
    }

    public PartitionInfo getPartitionConfigurations() throws EmbeddingLDAPException {
        if (partitionConfigurations == null) {
            buildConfigurations();
        }
        return partitionConfigurations;
    }

    public KdcConfiguration getKdcConfigurations() throws EmbeddingLDAPException {
        if (kdcConfigurations == null) {
            buildConfigurations();
        }
        return kdcConfigurations;
    }

    public boolean isKdcEnabled() {
        return kdcEnabled;
    }

    /**
     * Read properties from KDCConfiguration element in configuration and set them in the
     * kdcConfigurations object.
     *
     * @param kdcConfigElement
     * @throws org.wso2.carbon.ldap.server.util.EmbeddingLDAPException <p>
     *                                                                 KDC configurations
     *                                                                 <p>
     *                                                                 &lt;KDCServer&gt;
     *                                                                 &lt;Property name="name"&gt;defaultKDC&lt;/Property&gt;
     *                                                                 &lt;Property name="enabled"&gt;false&lt;/Property&gt;
     *                                                                 &lt;Property name="protocol"&gt;UDP&lt;/Property&gt;
     *                                                                 &lt;Property name="host"&gt;localhost&lt;/Property&gt;
     *                                                                 &lt;Property name="maximumTicketLifeTime"&gt;8640000&lt;/Property&gt;
     *                                                                 &lt;Property name="maximumRenewableLifeTime"&gt;604800000&lt;/Property&gt;
     *                                                                 &lt;Property name="preAuthenticationTimeStampEnabled"&gt;true&lt;/Property&gt;
     *                                                                 &lt;/KDCServer&gt;
     */
    private void buildKDCConfigurations(OMElement kdcConfigElement)
            throws EmbeddingLDAPException {
        Map<String, String> propertyMap = getChildPropertyElements(kdcConfigElement);
        String booleanString;
        if ((booleanString = propertyMap.get("enabled")) != null) {
            this.kdcEnabled = Boolean.parseBoolean(booleanString);
            if (!this.kdcEnabled) {
                logger.info("KDC server is disabled.");
                return;
            }
        } else {
            logger.info("KDC server is disabled.");
            return;
        }
        this.kdcConfigurations = new KdcConfiguration();
        this.kdcConfigurations.setKdcName(propertyMap.get("name"));
        try {
            this.kdcConfigurations.setKdcCommunicationProtocol(propertyMap.get("protocol"));
        } catch (DirectoryServerException e) {
            String errorMessage = "Can not read/set protocol parameter in KDCConfig.";
            logger.error(errorMessage, e);
            throw new EmbeddingLDAPException(errorMessage, e);
        }
        this.kdcConfigurations.setKdcHostAddress(propertyMap.get("host"));

        //Read KDC port from carbon.xml and set it
        int port = Integer.parseInt(propertyMap.get("port"));
        if (port == -1) {
            logger.warn("KDC port defined in carbon.xml's " + CARBON_KDC_PORT_CONFIG_SECTION +
                    " config section or embedded-ldap.xml is invalid. " +
                    "Setting KDC server port to default - " + DEFAULT_KDC_SERVER_PORT);
            port = DEFAULT_KDC_SERVER_PORT;
        }

        this.kdcConfigurations.setKdcCommunicationPort(port);

        this.kdcConfigurations.setMaxTicketLifeTime(getIntegerValue(propertyMap.get(
                "maximumTicketLifeTime")));
        this.kdcConfigurations.setMaxRenewableLifeTime(getIntegerValue(propertyMap.get(
                "maximumRenewableLifeTime")));

        if ((booleanString = propertyMap.get("preAuthenticationTimeStampEnabled")) != null) {
            boolean preAuthenticationTSEnabled = Boolean.parseBoolean(booleanString);
            this.kdcConfigurations.setPreAuthenticateTimeStampRequired(preAuthenticationTSEnabled);
        }
    }

    public boolean isEmbeddedLDAPEnabled() {
        return ldapConfiguration.isEnable();
    }

}

