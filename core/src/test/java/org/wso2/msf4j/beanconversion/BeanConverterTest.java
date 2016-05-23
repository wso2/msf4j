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

package org.wso2.msf4j.beanconversion;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.msf4j.internal.beanconversion.BeanConverter;
import org.wso2.msf4j.internal.router.Category;
import org.wso2.msf4j.internal.router.Pet;
import org.wso2.msf4j.internal.router.XmlBean;

import java.nio.ByteBuffer;

/**
 * Tests the functionality of BeanConverter.
 */
public class BeanConverterTest {

    @Test
    public void testJsonBeanConversionTextJson() throws BeanConversionException {
        Pet pet = makePet();
        ByteBuffer json = BeanConverter.getConverter("text/json").toMedia(pet);
        Pet pet1 = (Pet) BeanConverter.getConverter("text/json").toObject(json, Pet.class);
        Assert.assertEquals(pet.getId(), pet1.getId());
        Assert.assertEquals(pet.getDetails(), pet1.getDetails());
        Assert.assertEquals(pet.getImage(), pet1.getImage());
        Assert.assertEquals(pet.getCategory().getName(), pet1.getCategory().getName());
        Assert.assertEquals(pet.getAgeMonths(), pet1.getAgeMonths());
        Assert.assertEquals(pet.getPrice(), pet1.getPrice(), 0);
        Assert.assertEquals(pet.getDateAdded(), pet1.getDateAdded());
    }

    @Test
    public void testJsonBeanConversionApplicationJson() throws BeanConversionException {
        Pet pet = makePet();
        ByteBuffer json = BeanConverter.getConverter("application/json").toMedia(pet);
        Pet pet1 = (Pet) BeanConverter.getConverter("application/json").toObject(json, Pet.class);
        Assert.assertEquals(pet.getId(), pet1.getId());
        Assert.assertEquals(pet.getDetails(), pet1.getDetails());
        Assert.assertEquals(pet.getImage(), pet1.getImage());
        Assert.assertEquals(pet.getCategory().getName(), pet1.getCategory().getName());
        Assert.assertEquals(pet.getAgeMonths(), pet1.getAgeMonths());
        Assert.assertEquals(pet.getPrice(), pet1.getPrice(), 0);
        Assert.assertEquals(pet.getDateAdded(), pet1.getDateAdded());
    }

    @Test
    public void testTextPlainBeanConversion() throws BeanConversionException {
        String val = "Test_String";
        ByteBuffer media = BeanConverter.getConverter("text/plain").toMedia(val);
        Object obj1 = BeanConverter.getConverter("text/plain").toObject(media, null);
        Assert.assertEquals(obj1, val);
    }

    @Test
    public void testAnyBeanConversion() throws BeanConversionException {
        String val = "Test_String";
        ByteBuffer media = BeanConverter.getConverter("*/*").toMedia(val);
        Object obj1 = BeanConverter.getConverter("*/*").toObject(media, null);
        Assert.assertEquals(obj1, val);
    }

    @Test
    public void testXmlBeanConversion() throws BeanConversionException {
        XmlBean xmlBean = makeXmlBan();
        ByteBuffer xml = BeanConverter.getConverter("text/xml").toMedia(xmlBean);
        XmlBean xmlBean1 = (XmlBean) BeanConverter.getConverter("text/xml").toObject(xml, XmlBean.class);
        Assert.assertEquals(xmlBean.getName(), xmlBean1.getName());
        Assert.assertEquals(xmlBean.getId(), xmlBean1.getId());
        Assert.assertEquals(xmlBean.getValue(), xmlBean1.getValue());
    }

    private XmlBean makeXmlBan() {
        XmlBean xmlBean = new XmlBean();
        xmlBean.setId(12);
        xmlBean.setName("xml-bean-name");
        xmlBean.setValue(457);
        return xmlBean;
    }

    private Pet makePet() {
        Pet pet = new Pet();
        pet.setCategory(new Category("dog"));
        pet.setAgeMonths(3);
        pet.setDetails("small-cat");
        pet.setPrice(10.5f);
        pet.setImage("cat.png");
        return pet;
    }

}
