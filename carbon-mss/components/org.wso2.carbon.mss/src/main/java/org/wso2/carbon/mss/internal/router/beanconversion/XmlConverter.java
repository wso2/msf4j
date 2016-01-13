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

package org.wso2.carbon.mss.internal.router.beanconversion;

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * Media type converter for text/xml mime type.
 */
public class XmlConverter implements MediaTypeConverter {

    @Override
    public Object toMedia(Object object) throws BeanConversionException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
            Marshaller marshaller = jaxbContext.createMarshaller();
            StringWriter stringWriter = new StringWriter();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(object, stringWriter);
            return stringWriter.toString();
        } catch (JAXBException e) {
            throw new BeanConversionException("Unable to perform object to xml conversion", e);
        }
    }

    @Override
    public Object toObject(String content, Type targetType) throws BeanConversionException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance((Class) targetType);
            return jaxbContext.createUnmarshaller().unmarshal(new StringReader(content));
        } catch (JAXBException e) {
            throw new BeanConversionException("Unable to perform xml to object conversion", e);
        }
    }
}
