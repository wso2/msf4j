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
package org.wso2.carbon.mss.examples.petstore.security.ldap.server;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apacheds.DirectoryServiceFactory;
import org.wso2.carbon.apacheds.KDCServer;
import org.wso2.carbon.apacheds.KdcConfiguration;
import org.wso2.carbon.apacheds.LDAPConfiguration;
import org.wso2.carbon.apacheds.LDAPServer;
import org.wso2.carbon.apacheds.PartitionInfo;
import org.wso2.carbon.apacheds.PartitionManager;
import org.wso2.carbon.ldap.server.exception.DirectoryServerException;
import org.wso2.carbon.ldap.server.util.EmbeddingLDAPException;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * This start the EmbeddedLDAP-server. Needs to have "MSS_HOME" system variable configured by
 * pointing to the directory that ldap data and config files get stored. If the "MSS_HOME" is not set
 * current working directory will use as the MSS_HOME".
 */
public class ApacheDirectoryServerActivator {

    private static final Logger log = LoggerFactory.getLogger(ApacheDirectoryServerActivator.class);

    private String mssHome = SystemVariableUtil.getValue("MSS_HOME", System.getProperty("user.dir"));
    private LDAPServer ldapServer;
    private KDCServer kdcServer;

    /**
     * Starts EmbeddedLDAP-server.
     */
    public void start() {

        try {
            /*Configure embedded-ldap.xml and  default ldap schema, if those are not defined.*/
            copyResources();

            /*Read the embedded-ldap configuration file.*/
            LDAPServerConfigurationBuilder configurationBuilder = new LDAPServerConfigurationBuilder(
                    getLdapConfigurationFile());
            /*Make relevant objects that encapsulate different parts of config file.*/
            configurationBuilder.buildConfigurations();

            boolean embeddedLDAPEnabled = configurationBuilder.isEmbeddedLDAPEnabled();
            //start LDAPServer only if embedded-ldap is enabled.
            if (embeddedLDAPEnabled) {

                LDAPConfiguration ldapConfiguration = configurationBuilder.getLdapConfiguration();
                /*set the embedded-apacheds's schema location which is: carbon-home/repository/data/
                is-default-schema.zip
                */
                setSchemaLocation();

                /* Set working directory where schema directory and ldap partitions are created*/
                setWorkingDirectory(ldapConfiguration);

                startLdapServer(ldapConfiguration);

                /* replace default password with that is provided in the configuration file.*/
                this.ldapServer.changeConnectionUserPassword(
                        configurationBuilder.getConnectionPassword());

                // Add admin (default)partition if it is not already created.
                PartitionManager partitionManager = this.ldapServer.getPartitionManager();
                PartitionInfo defaultPartitionInfo = configurationBuilder.getPartitionConfigurations();
                boolean defaultPartitionAlreadyExisted =
                        partitionManager.partitionDirectoryExists(defaultPartitionInfo.getPartitionId());

                if (!defaultPartitionAlreadyExisted) {
                    partitionManager.addPartition(defaultPartitionInfo);
                    if (kdcServer == null) {
                        kdcServer = DirectoryServiceFactory.createKDCServer(DirectoryServiceFactory.LDAPServerType.
                                APACHE_DIRECTORY_SERVICE);
                    }
                    kdcServer.kerberizePartition(configurationBuilder.
                            getPartitionConfigurations(), this.ldapServer);
                } else {
                    partitionManager.initializeExistingPartition(defaultPartitionInfo);
                }

                // Start KDC if enabled
                if (configurationBuilder.isKdcEnabled()) {
                    startKDC(configurationBuilder.getKdcConfigurations());
                }

                if (log.isDebugEnabled()) {
                    log.debug("apacheds-server started.");
                }

            } else {
                //if needed, create a dummy tenant manager service and register it.
                log.info("Embedded LDAP is disabled.");
            }

        } catch (FileNotFoundException e) {
            String errorMessage = "Could not start the embedded-ldap. ";
            log.error(errorMessage, e);

        } catch (DirectoryServerException e) {
            String errorMessage = "Could not start the embedded-ldap. ";
            log.error(errorMessage, e);

        } catch (EmbeddingLDAPException e) {
            String errorMessage = "Could not start the embedded-ldap. ";
            log.error(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = "An unknown exception occurred while starting LDAP server. ";
            log.error(errorMessage, e);
        } catch (Throwable e) {
            String errorMessage = "An unknown error occurred while starting LDAP server. ";
            log.error(errorMessage, e);
        }

    }

    private void setSchemaLocation() throws EmbeddingLDAPException {

        String schemaLocation = "repository" + File.separator + "data" + File.separator +
                "is-default-schema.zip";
        File dataDir = new File(getCarbonHome(), schemaLocation);

        // Set schema location
        System.setProperty("schema.zip.store.location", dataDir.getAbsolutePath());
    }

    private String getCarbonHome() throws EmbeddingLDAPException {
        return mssHome;
    }

    private File getLdapConfigurationFile() throws EmbeddingLDAPException {

        String configurationFilePath = "repository" + File.separator + "conf" + File.separator +
                "embedded-ldap.xml";
        return new File(getCarbonHome(), configurationFilePath);
    }

    private void setWorkingDirectory(LDAPConfiguration ldapConfiguration)
            throws EmbeddingLDAPException {

        if (ldapConfiguration.getWorkingDirectory().equals(".")) {
            File dataDir = new File(getCarbonHome(), "repository" + File.separator + "data");
            if (!dataDir.exists()) {
                if (!dataDir.mkdir()) {
                    String msg = "Unable to create data directory at " + dataDir.getAbsolutePath();
                    log.error(msg);
                    throw new EmbeddingLDAPException(msg);
                }
            }

            File bundleDataDir = new File(dataDir, "org.wso2.carbon.directory");
            if (!bundleDataDir.exists()) {
                if (!bundleDataDir.mkdirs()) {
                    String msg = "Unable to create schema data directory at " + bundleDataDir.
                            getAbsolutePath();
                    log.error(msg);
                    throw new EmbeddingLDAPException(msg);

                }
            }

            ldapConfiguration.setWorkingDirectory(bundleDataDir.getAbsolutePath());
        }
    }

    private void startLdapServer(LDAPConfiguration ldapConfiguration)
            throws DirectoryServerException {

        this.ldapServer = DirectoryServiceFactory.createLDAPServer(DirectoryServiceFactory.
                LDAPServerType.APACHE_DIRECTORY_SERVICE);

        log.info("Initializing Directory Server with working directory " + ldapConfiguration.
                getWorkingDirectory() + " and port " + ldapConfiguration.getLdapPort());

        this.ldapServer.init(ldapConfiguration);

        this.ldapServer.start();
    }

    private void startKDC(KdcConfiguration kdcConfiguration)
            throws DirectoryServerException {

        if (kdcServer == null) {
            kdcServer = DirectoryServiceFactory
                    .createKDCServer(DirectoryServiceFactory.LDAPServerType.APACHE_DIRECTORY_SERVICE);
        }
        kdcServer.init(kdcConfiguration, this.ldapServer);

        kdcServer.start();

    }

    public void stop() throws Exception {

        if (this.kdcServer != null) {
            this.kdcServer.stop();
        }

        if (this.ldapServer != null) {

            this.ldapServer.stop();
        }
    }

    private void copyResources() throws IOException, EmbeddingLDAPException {

        final File jarFile = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());

        final String repositoryDirectory = "repository";
        final File destinationRoot = new File(getCarbonHome());

        //Check whether ladap configs are already configured
        File repository = new File(getCarbonHome() + File.separator + repositoryDirectory);

        if (!repository.exists()) {
            JarFile jar = null;

            try {
                jar = new JarFile(jarFile);

                final Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar

                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();

                    // Skip if the entry is not about the 'repository' directory.
                    if (!entry.getName().startsWith(repositoryDirectory)) {
                        continue;
                    }

                    // If the entry is a directory create the relevant directory in the destination.
                    File destination = new File(destinationRoot, entry.getName());
                    if (entry.isDirectory()) {
                        if (destination.mkdirs()) {
                            continue;
                        }
                    }

                    InputStream in = null;
                    OutputStream out = null;

                    try {
                        // If the entry is a file, copy the file to the destination
                        in = jar.getInputStream(entry);
                        out = new FileOutputStream(destination);
                        IOUtils.copy(in, out);
                        IOUtils.closeQuietly(in);
                    } finally {
                        if (out != null) {
                            out.close();
                        }
                        if (in != null) {
                            in.close();
                        }
                    }
                }
            } finally {
                if (jar != null) {
                    jar.close();

                }
            }
        }
    }

}
