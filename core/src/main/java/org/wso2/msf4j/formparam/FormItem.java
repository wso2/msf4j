package org.wso2.msf4j.formparam;
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

import org.wso2.msf4j.formparam.util.Closeable;
import org.wso2.msf4j.formparam.util.FormItemHeader;
import org.wso2.msf4j.formparam.util.StreamUtil;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p> This interface provides access to a file or form item that was
 * received within a <code>multipart/form-data</code> POST request.
 * The items contents are retrieved by calling {@link #openStream()}.</p>
 * <p>Instances of this class are created by accessing the
 * iterator, returned by
 * {@link FormParamIterator(org.wso2.msf4j.Request)}.</p>
 * <p><em>Note</em>: There is an interaction between the iterator and
 * its associated instances of {@link FormItem}: By invoking
 * {@link java.util.Iterator#hasNext()} on the iterator, you discard all data,
 * which hasn't been read so far from the previous data.</p>
 *
 */
public class FormItem {

    /**
     * The file items content type.
     */
    private final String contentType;

    /**
     * The file items field name.
     */
    private final String fieldName;

    /**
     * The file items file name.
     */
    private final String name;

    /**
     * Whether the file item is a form field.
     */
    private final boolean formField;

    /**
     * The file items input stream.
     */
    private final InputStream stream;

    /**
     * The headers, if any.
     */
    private FormItemHeader headers;

    /**
     * Creates a new instance.
     *
     * @param pName          The items file name, or null.
     * @param pFieldName     The items field name.
     * @param pContentType   The items content type, or null.
     * @param pFormField     Whether the item is a form field.
     * @param pContentLength The items content length, if known, or -1
     * @throws IOException Creating the file item failed.
     */
    FormItem(String pName, String pFieldName, String pContentType, boolean pFormField, long pContentLength,
             MultipartStream multi) throws IOException {
        name = pName;
        fieldName = pFieldName;
        contentType = pContentType;
        formField = pFormField;
        stream = multi.newInputStream();
    }

    /**
     * Returns the items content type, or null.
     *
     * @return Content type, if known, or null.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Returns the items field name.
     *
     * @return Field name.
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Returns the items file name.
     *
     * @return File name, if known, or null.
     */
    public String getName() {
        return StreamUtil.checkFileName(name);
    }

    /**
     * Returns, whether this is a form field.
     *
     * @return True, if the item is a form field,
     * otherwise false.
     */
    public boolean isFormField() {
        return formField;
    }

    /**
     * Returns an input stream, which may be used to
     * read the items contents.
     *
     * @return Opened input stream.
     * @throws IOException An I/O error occurred.
     */
    public InputStream openStream() throws IOException {
        if (((Closeable) stream).isClosed()) {
            throw new ItemSkippedException();
        }
        return stream;
    }

    /**
     * Closes the file item.
     *
     * @throws IOException An I/O error occurred.
     */
    void close() throws IOException {
        stream.close();
    }

    /**
     * Returns the file item headers.
     *
     * @return The items header object
     */
    public FormItemHeader getHeaders() {
        return headers;
    }

    /**
     * Sets the file item headers.
     *
     * @param pHeaders The items header object
     */
    public void setHeaders(FormItemHeader pHeaders) {
        headers = pHeaders;
    }

    static class ItemSkippedException extends IOException {

        /**
         * The exceptions serial version UID, which is being used
         * when serializing an exception instance.
         */
        private static final long serialVersionUID = -7280778431581963740L;

    }
}
