/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.msf4j.internal.beanconversion;

import org.wso2.msf4j.beanconversion.BeanConversionException;
import org.wso2.msf4j.beanconversion.MediaTypeConverter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * Media type converter for text/xml mime type.
 */
public class XmlConverter extends MediaTypeConverter {

    private static final String TEXT_XML = "text/xml";

    /**
     * Provides the supported media types for bean conversions.
     */
    @Override
    public String[] getSupportedMediaTypes() {
        return new String[]{MediaType.APPLICATION_XML, TEXT_XML};
    }


    /**
     * Convert an Object to a xml encoded ByteBuffer.
     *
     * @param object object that needs to be converted to a media content
     * @return xml encoded byte buffer
     */
    @Override
    public ByteBuffer toMedia(Object object) throws BeanConversionException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
            Marshaller marshaller = jaxbContext.createMarshaller();
            StringWriter stringWriter = new StringWriter();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(object, stringWriter);
            return ByteBuffer.wrap(stringWriter.toString().getBytes(Charset.defaultCharset()));
        } catch (JAXBException e) {
            throw new BeanConversionException("Unable to perform object to xml conversion", e);
        }
    }

    /**
     * Convert a xml ByteBuffer content to an object.
     *
     * @param content    content that needs to be converted to an object
     * @param targetType media type of the content
     * @return Object that maps the xml data
     */
    @Override
    public Object toObject(ByteBuffer content, Type targetType) throws BeanConversionException {
        try {
            String str = Charset.defaultCharset().decode(content).toString();
            JAXBContext jaxbContext = null;
            if (targetType instanceof Class) {
                jaxbContext = JAXBContext.newInstance((Class) targetType);
                return jaxbContext.createUnmarshaller().unmarshal(new StringReader(str));
            }
        } catch (JAXBException e) {
            throw new BeanConversionException("Unable to perform xml to object conversion", e);
        }
        return null;
    }

    @Override
    protected Object toObject(InputStream inputStream, Type targetType) throws BeanConversionException {
        try {
            JAXBContext jaxbContext;
            Reader reader = new InputStreamReader(inputStream, UTF_8_CHARSET);
            if (targetType instanceof Class) {
                jaxbContext = JAXBContext.newInstance((Class) targetType);
                return jaxbContext.createUnmarshaller().unmarshal(reader);
            }
        } catch (JAXBException | UnsupportedEncodingException e) {
            throw new BeanConversionException("Unable to perform xml to object conversion", e);
        }
        return null;
    }
}
