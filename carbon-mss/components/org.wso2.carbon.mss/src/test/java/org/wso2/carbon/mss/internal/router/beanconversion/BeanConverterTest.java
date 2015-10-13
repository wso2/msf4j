/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mss.internal.router.beanconversion;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.mss.internal.router.Category;
import org.wso2.carbon.mss.internal.router.Pet;

/**
 * Tests the functionality of BeaanConverter
 */
public class BeanConverterTest {

    private Pet pet = new Pet();
    private Gson gson = new Gson();

    @Before
    public void setUp() {
        pet.setId("0001");
        pet.setCategory(new Category("dog"));
        pet.setAgeMonths(3);
        pet.setDetails("small-cat");
        pet.setPrice(10.5f);
        pet.setDateAdded(99999);
        pet.setImage("cat.png");
    }

    @Test
    public void testToJsonForMimeTextJson() throws BeanConversionException {
        Object json = BeanConverter.instance("text/json").toMedia(pet);
        Assert.assertTrue(json instanceof String);
        JsonObject jsonObj = (new Gson()).fromJson(json.toString(), JsonObject.class);
        Assert.assertTrue(jsonObj.get("id").getAsString().equals("0001"));
    }

    @Test
    public void testToPetObjectForMimeTextJson() throws BeanConversionException {
        Object json = gson.toJson(pet);
        Pet pet = (Pet) BeanConverter.instance("text/json").toObject(json.toString(), Pet.class);
        Assert.assertTrue(pet instanceof Pet);
        Assert.assertTrue(pet.getDetails().equals("small-cat"));
    }

    @Test
    public void testToJsonForMimeApplicationJson() throws BeanConversionException {
        Object json = BeanConverter.instance("application/json").toMedia(pet);
        Assert.assertTrue(json instanceof String);
        JsonObject jsonObj = (new Gson()).fromJson(json.toString(), JsonObject.class);
        Assert.assertTrue(jsonObj.get("id").getAsString().equals("0001"));
    }

    @Test
    public void testDefaultForTextPlainMime() throws BeanConversionException {
        Object obj = BeanConverter.instance("text/plain").toMedia("Test_String");
        Assert.assertTrue(obj instanceof String);
        Assert.assertTrue(obj.equals("Test_String"));
    }

    @Test
    public void testDefaultForAnyMime() throws BeanConversionException {
        Object obj = BeanConverter.instance("*/*").toMedia("Test_String");
        Assert.assertTrue(obj instanceof String);
        Assert.assertTrue(obj.equals("Test_String"));
    }

}
