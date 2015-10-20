/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.mss.examples.petstore.transaction;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a transaction in the pet store
 */
@SuppressWarnings("unused")
@XmlRootElement
public class Transaction {

    public String id;

    /**
     * The pets that have been purchased as part of this transaction
     */
    public List<String> petIDs;

    /**
     * The user who purchased the pet
     */
    public String userID;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getPetIDs() {
        return petIDs;
    }

    public void setPetIDs(List<String> petIDs) {
        this.petIDs = petIDs;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
