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

package org.wso2.msf4j.examples.petstore.security.ldap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.AttributeInUseException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.NoSuchAttributeException;


/**
 * Perform user management operations for LDAP. OrganizationUnit (OU) for Users and Groups
 * should have defined in system variables "LDAP_USER_OU" and "LDAP_GROUPS_OU" correspondingly.
 * If not default OU/s are used.
 */
public class LDAPUserStoreManager {

    private static final Logger log = LoggerFactory.getLogger(LDAPUserStoreManager.class);

    //The OU (organizational unit) to add users to
    private static final String USERS_OU = SystemVariableUtil.getValue("LDAP_USER_OU",
                                                                       "ou=Users,dc=WSO2,dc=ORG");

    //The OU (organizational unit) to add groups to
    private static final String GROUPS_OU = SystemVariableUtil.getValue("LDAP_GROUPS_OU",
                                                                        "ou=Groups,dc=WSO2,dc=ORG");

    //The default LDAP port
    private static final int DEFAULT_PORT = 389;

    //The LDAPManager instance object
    private static Map instances = new HashMap();

    //The connection, through a <code>DirContext</code>, to LDAP
    private DirContext context;

    //The hostname connected to
    private String hostname;

    //The port connected to
    private int port;

    protected LDAPUserStoreManager(String hostname, int port, String username, String password)
            throws NamingException {

        context = getInitialContext(hostname, port, username, password);

        // Only save data if we got connected
        this.hostname = hostname;
        this.port = port;
    }

    public static LDAPUserStoreManager getInstance(String hostname, int port,
                                                   String username, String password)
            throws NamingException {

        // Construct the key for the supplied information
        String key = hostname + ":" + port + "|" +
                (username == null ? "" : username) + "|" +
                (password == null ? "" : password);

        if (!instances.containsKey(key)) {
            synchronized (LDAPUserStoreManager.class) {
                if (!instances.containsKey(key)) {
                    LDAPUserStoreManager instance = new LDAPUserStoreManager(hostname, port,
                                                                             username, password);
                    instances.put(key, instance);
                    return instance;
                }
            }
        }

        return (LDAPUserStoreManager) instances.get(key);
    }

    public static LDAPUserStoreManager getInstance(String hostname)
            throws NamingException {

        return getInstance(hostname, DEFAULT_PORT, null, null);
    }

    public void addUser(String username, String firstName, String lastName, String password,
                        String email) throws NamingException {

        // Create a container set of attributes
        Attributes container = new BasicAttributes();

        // Create the objectclass to add
        Attribute objClasses = new BasicAttribute("objectClass");
        objClasses.add("top");
        objClasses.add("person");
        objClasses.add("organizationalPerson");
        objClasses.add("inetOrgPerson");

        // Assign the username, first name, and last name
        String cnValue;
        Attribute givenName;
        Attribute sn;
        if (firstName == null || lastName == null) {
            cnValue = username;
            givenName = new BasicAttribute("givenName", username);
            sn = new BasicAttribute("sn", username);
        } else {
            cnValue = firstName + " " + lastName;
            givenName = new BasicAttribute("givenName", firstName);
            sn = new BasicAttribute("sn", lastName);
        }
        Attribute cn = new BasicAttribute("cn", cnValue);
        Attribute uid = new BasicAttribute("uid", username);
        Attribute mail = new BasicAttribute("mail", email);

        // Add password
        Attribute userPassword = new BasicAttribute("userpassword", password);
        
        // Add these to the container
        container.put(objClasses);
        container.put(cn);
        container.put(sn);
        container.put(givenName);
        container.put(uid);
        container.put(mail);
        container.put(userPassword);

        // Create the entry
        context.createSubcontext(getUserDN(username), container);
    }

    public void deleteUser(String username) throws NamingException {
        try {
            context.destroySubcontext(getUserDN(username));
        } catch (NameNotFoundException e) {
            // If the user is not found, ignore the error
        }
    }

    public boolean isValidUser(String username, String password)
            throws Exception {

        try {
            getInitialContext(hostname, port, getUserDN(username), password);
            return true;
        } catch (javax.naming.NameNotFoundException e) {
            throw new Exception("Authentication failed " + username);
        } catch (NamingException e) {
            log.error("error", e);
            // Any other error indicates couldn't log user in
            return false;
        }
    }

    public void addGroup(String name, String description)
            throws NamingException {

        // Create a container set of attributes
        Attributes container = new BasicAttributes();

        // Create the objectclass to add
        Attribute objClasses = new BasicAttribute("objectClass");
        objClasses.add("top");
        objClasses.add("groupOfNames");

        // Assign the name and description to the group
        Attribute cn = new BasicAttribute("cn", name);
        Attribute desc = new BasicAttribute("description", description);

        // Add these to the container
        container.put(objClasses);
        container.put(cn);
        container.put(desc);

        try {
            // Create the entry
            context.createSubcontext(getGroupDN(name), container);
        } catch (NameAlreadyBoundException e) {
            log.info("Group already exist ..");
            // Group already added. ignore exception.
        }
    }

    public void deleteGroup(String name) throws NamingException {
        try {
            context.destroySubcontext(getGroupDN(name));
        } catch (NameNotFoundException e) {
            // If the group is not found, ignore the error
        }
    }

    public void assignUser(String username, String groupName) throws NamingException {

        try {
            ModificationItem[] mods = new ModificationItem[1];
            Attribute mod = new BasicAttribute("member", getUserDN(username));
            mods[0] = new ModificationItem(DirContext.ADD_ATTRIBUTE, mod);
            context.modifyAttributes(getGroupDN(groupName), mods);
        } catch (AttributeInUseException e) {
            // If user is already added, ignore exception
        }
    }

    public void removeUser(String username, String groupName) throws NamingException {

        try {
            ModificationItem[] mods = new ModificationItem[1];
            Attribute mod = new BasicAttribute("member", getUserDN(username));
            mods[0] = new ModificationItem(DirContext.REMOVE_ATTRIBUTE, mod);
            context.modifyAttributes(getGroupDN(groupName), mods);
        } catch (NoSuchAttributeException e) {
            // If user is not assigned, ignore the error
        }
    }

    public String getAttributeValue(String username, String attributeName) throws NamingException {

        String attributeValue = null;

        // Set up attributes to search for
        String[] searchAttributes = new String[1];
        searchAttributes[0] = attributeName;
        Attributes attributes = context.getAttributes(getUserDN(username), searchAttributes);

        if (attributes != null) {
           attributeValue = attributes.get(attributeName).get().toString();
        }

        return attributeValue;
    }

    public void addUserAndAssignGroups(String username, String firstName, String lastName,
                                       String password, String email, List<String> groups)
            throws NamingException {

        addUser(username, firstName, lastName, password, email);
        if (groups != null) {
            for (String group : groups) {
                assignUser(username, group);

            }
        }
    }


    private String getUserDN(String username) {
        return "uid=" + username + "," + USERS_OU;
    }

    private String getGroupDN(String name) {
        return "cn=" + name + "," + GROUPS_OU;
    }

    private DirContext getInitialContext(String hostname, int port,
                                         String username, String password)
            throws NamingException {

        String providerURL = "ldap://" + hostname + ":" + port;

        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        props.put(Context.PROVIDER_URL, providerURL);

        if ((username != null) && (!username.equals(""))) {
            props.put(Context.SECURITY_AUTHENTICATION, "simple");
            props.put(Context.SECURITY_PRINCIPAL, username);
            props.put(Context.SECURITY_CREDENTIALS, (password == null) ? "" : password);
        }

        return new InitialDirContext(props);
    }

}
