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

import org.testng.annotations.Test;
import org.wso2.msf4j.internal.beanconversion.BeanConverter;
import org.wso2.msf4j.pojo.Category;
import org.wso2.msf4j.pojo.Pet;
import org.wso2.msf4j.pojo.XmlBean;

import java.nio.ByteBuffer;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Tests the functionality of BeanConverter.
 */
public class BeanConverterTest {

    @Test
    public void testJsonBeanConversionTextJson() throws BeanConversionException {
        Pet pet = makePet();
        ByteBuffer json = BeanConverter.getConverter("text/json").toMedia(pet);
        Pet pet1 = (Pet) BeanConverter.getConverter("text/json").toObject(json, Pet.class);
        assertEquals(pet.getId(), pet1.getId());
        assertEquals(pet.getDetails(), pet1.getDetails());
        assertEquals(pet.getImage(), pet1.getImage());
        assertEquals(pet.getCategory().getName(), pet1.getCategory().getName());
        assertEquals(pet.getAgeMonths(), pet1.getAgeMonths());
        assertEquals(pet.getPrice(), pet1.getPrice(), 0);
        assertEquals(pet.getDateAdded(), pet1.getDateAdded());
    }

    @Test
    public void testJsonBeanConversionApplicationJson() throws BeanConversionException {
        Pet original = makePet();
        ByteBuffer json = BeanConverter.getConverter("application/json").toMedia(original);
        Pet result = (Pet) BeanConverter.getConverter("application/json").toObject(json, Pet.class);
        assertEquals(original.getId(), result.getId());
        assertEquals(original.getDetails(), result.getDetails());
        assertEquals(original.getImage(), result.getImage());
        assertEquals(original.getCategory().getName(), result.getCategory().getName());
        assertEquals(original.getAgeMonths(), result.getAgeMonths());
        assertEquals(original.getPrice(), result.getPrice(), 0);
        assertEquals(original.getDateAdded(), result.getDateAdded());
    }

    @Test
    public void testTextPlainBeanConversion() throws BeanConversionException {
        String val = "Test_String";
        ByteBuffer media = BeanConverter.getConverter("text/plain").toMedia(val);
        Object obj1 = BeanConverter.getConverter("text/plain").toObject(media, null);
        assertEquals(obj1, val);
    }

    @Test
    public void testAnyBeanConversion() throws BeanConversionException {
        String original = "Test_String";
        ByteBuffer media = BeanConverter.getConverter("*/*").toMedia(original);
        Object result = BeanConverter.getConverter("*/*").toObject(media, null);
        assertEquals(original, result);
    }

    @Test
    public void testXmlBeanConversion() throws BeanConversionException {
        XmlBean original = makeXmlBan();
        ByteBuffer xml = BeanConverter.getConverter("text/xml").toMedia(original);
        XmlBean result = (XmlBean) BeanConverter.getConverter("text/xml").toObject(xml, XmlBean.class);
        assertEquals(original.getName(), result.getName());
        assertEquals(original.getId(), result.getId());
        assertEquals(original.getValue(), result.getValue());
    }

    @Test(expectedExceptions = BeanConversionException.class, expectedExceptionsMessageRegExp = "Object cannot be null")
    public void testBeansConversionConvertToMediaException() {
        BeanConverter.getConverter("text/xml").convertToMedia(null);
    }

    @Test(expectedExceptions = BeanConversionException.class,
            expectedExceptionsMessageRegExp = "Content or target type cannot be null")
    public void testBeansConversionConvertToObjectExceptionWithNullContent() {
        BeanConverter.getConverter("text/xml").convertToObject(null, Pet.class);
    }

    @Test(expectedExceptions = BeanConversionException.class,
            expectedExceptionsMessageRegExp = "Content or target type cannot be null")
    public void testBeansConversionConvertToObjectExceptionWithNullTarget() {
        XmlBean original = makeXmlBan();
        ByteBuffer xml = BeanConverter.getConverter("text/xml").toMedia(original);
        BeanConverter.getConverter("text/xml").convertToObject(xml, null);
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
