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

package org.wso2.msf4j.formparam;

import org.apache.commons.io.IOUtils;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.exception.FormUploadException;
import org.wso2.msf4j.formparam.exception.InvalidContentTypeException;
import org.wso2.msf4j.formparam.util.FormItemHeader;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

import static java.lang.String.format;

/**
 *
 */
public class FormParamIterator implements Iterator {

    /**
     * HTTP content type header name.
     */
    private static final String CONTENT_TYPE = "Content-type";

    /**
     * HTTP content disposition header name.
     */
    private static final String CONTENT_DISPOSITION = "Content-disposition";

    /**
     * HTTP content length header name.
     */
    private static final String CONTENT_LENGTH = "Content-length";

    /**
     * Content-disposition value for form data.
     */
    private static final String FORM_DATA = "form-data";

    /**
     * Content-disposition value for file attachment.
     */
    private static final String ATTACHMENT = "attachment";

    /**
     * Part of HTTP content type header.
     */
    private static final String MULTIPART = "multipart/";

    /**
     * HTTP content type header for multipart forms.
     */
    private static final String MULTIPART_FORM_DATA = "multipart/form-data";

    /**
     * HTTP content type header for multiple uploads.
     */
    private static final String MULTIPART_MIXED = "multipart/mixed";

    /**
     * The multi part stream to process.
     */
    private final MultipartStream multi;

    /**
     * The boundary, which separates the various parts.
     */
    private final byte[] boundary;

    /**
     * The item, which we currently process.
     */
    private FormItem currentItem;

    /**
     * The current items field name.
     */
    private String currentFieldName;

    /**
     * Whether we are currently skipping the preamble.
     */
    private boolean skipPreamble;

    /**
     * Whether the current item may still be read.
     */
    private boolean itemValid;

    /**
     * Whether we have seen the end of the file.
     */
    private boolean eof;

    // ----------------------------------------------------------- Data members

    /**
     * Processes an <a href="http://www.ietf.org/rfc/rfc1867.txt">RFC 1867</a>
     * compliant <code>multipart/form-data</code> stream.
     *
     * @param request The context for the request to be parsed.
     * @throws FormUploadException if there are problems reading/parsing
     *                             the request or storing files.
     * @throws IOException         An I/O error occurred. This may be a network
     *                             error while communicating with the client or a problem while
     *                             storing the uploaded content.
     */
    public FormParamIterator(Request request) throws FormUploadException, IOException {
        this(new RequestContext(request));
    }

    // ------------------------------------------------------ Protected methods

    /**
     * Retrieves the boundary from the <code>Content-type</code> header.
     *
     * @param contentType The value of the content type header from which to
     *                    extract the boundary value.
     * @return The boundary, as a byte array.
     */
    private byte[] getBoundary(String contentType) {
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        // Parameter parser can handle null input
        Map<String, String> params = parser.parse(contentType, new char[] { ';', ',' });
        String boundaryStr = params.get("boundary");

        if (boundaryStr == null) {
            return new byte[] {};
        }
        byte[] boundary;
        try {
            boundary = boundaryStr.getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            boundary = boundaryStr.getBytes(Charset.defaultCharset()); // Intentionally falls back to default charset
        }
        return boundary;
    }

    /**
     * Retrieves the file name from the <code>Content-disposition</code>
     * header.
     *
     * @param headers The HTTP headers object.
     * @return The file name for the current <code>encapsulation</code>.
     */
    private String getFileName(FormItemHeader headers) {
        return getFileName(headers.getHeader(CONTENT_DISPOSITION));
    }

    /**
     * Returns the given content-disposition headers file name.
     *
     * @param pContentDisposition The content-disposition headers value.
     * @return The file name
     */
    private String getFileName(String pContentDisposition) {
        String fileName = null;
        if (pContentDisposition != null) {
            String cdl = pContentDisposition.toLowerCase(Locale.ENGLISH);
            if (cdl.startsWith(FORM_DATA) || cdl.startsWith(ATTACHMENT)) {
                ParameterParser parser = new ParameterParser();
                parser.setLowerCaseNames(true);
                // Parameter parser can handle null input
                Map<String, String> params = parser.parse(pContentDisposition, ';');
                if (params.containsKey("filename")) {
                    fileName = params.get("filename");
                    if (fileName != null) {
                        fileName = fileName.trim();
                    } else {
                        // Even if there is no value, the parameter is present,
                        // so we return an empty file name rather than no file
                        // name.
                        fileName = "";
                    }
                }
            }
        }
        return fileName;
    }

    /**
     * Retrieves the field name from the <code>Content-disposition</code>
     * header.
     *
     * @param headers A <code>Map</code> containing the HTTP request headers.
     * @return The field name for the current <code>encapsulation</code>.
     */
    private String getFieldName(FormItemHeader headers) {
        return getFieldName(headers.getHeader(CONTENT_DISPOSITION));
    }

    /**
     * Returns the field name, which is given by the content-disposition
     * header.
     *
     * @param pContentDisposition The content-dispositions header value.
     * @return The field jake
     */
    private String getFieldName(String pContentDisposition) {
        String fieldName = null;
        if (pContentDisposition != null && pContentDisposition.toLowerCase(Locale.ENGLISH).startsWith(FORM_DATA)) {
            ParameterParser parser = new ParameterParser();
            parser.setLowerCaseNames(true);
            // Parameter parser can handle null input
            Map<String, String> params = parser.parse(pContentDisposition, ';');
            fieldName = params.get("name");
            if (fieldName != null) {
                fieldName = fieldName.trim();
            }
        }
        return fieldName;
    }

    /**
     * <p> Parses the <code>header-part</code> and returns as key/value
     * pairs.
     * <p>
     * <p> If there are multiple headers of the same names, the name
     * will map to a comma-separated list containing the values.
     *
     * @param headerPart The <code>header-part</code> of the current
     *                   <code>encapsulation</code>.
     * @return A <code>Map</code> containing the parsed HTTP request headers.
     */
    private FormItemHeader getParsedHeaders(String headerPart) {
        final int len = headerPart.length();
        FormItemHeader headers = newFileItemHeaders();
        int start = 0;
        for (;;) {
            int end = parseEndOfLine(headerPart, start);
            if (start == end) {
                break;
            }
            StringBuilder header = new StringBuilder(headerPart.substring(start, end));
            start = end + 2;
            while (start < len) {
                int nonWs = start;
                while (nonWs < len) {
                    char c = headerPart.charAt(nonWs);
                    if (c != ' ' && c != '\t') {
                        break;
                    }
                    ++nonWs;
                }
                if (nonWs == start) {
                    break;
                }
                // Continuation line found
                end = parseEndOfLine(headerPart, nonWs);
                header.append(" ").append(headerPart.substring(nonWs, end));
                start = end + 2;
            }
            parseHeaderLine(headers, header.toString());
        }
        return headers;
    }

    /**
     * Creates a new instance of {@link FormItemHeader}.
     *
     * @return The new instance.
     */
    private FormItemHeader newFileItemHeaders() {
        return new FormItemHeader();
    }

    /**
     * Skips bytes until the end of the current line.
     *
     * @param headerPart The headers, which are being parsed.
     * @param end        Index of the last byte, which has yet been
     *                   processed.
     * @return Index of the \r\n sequence, which indicates
     * end of line.
     */
    private int parseEndOfLine(String headerPart, int end) {
        int index = end;
        for (;;) {
            int offset = headerPart.indexOf('\r', index);
            if (offset == -1 || offset + 1 >= headerPart.length()) {
                throw new IllegalStateException("Expected headers to be terminated by an empty line.");
            }
            if (headerPart.charAt(offset + 1) == '\n') {
                return offset;
            }
            index = offset + 1;
        }
    }

    /**
     * Reads the next header line.
     *
     * @param headers String with all headers.
     * @param header  Map where to store the current header.
     */
    private void parseHeaderLine(FormItemHeader headers, String header) {
        final int colonOffset = header.indexOf(':');
        if (colonOffset == -1) {
            // This header line is malformed, skip it.
            return;
        }
        String headerName = header.substring(0, colonOffset).trim();
        String headerValue = header.substring(header.indexOf(':') + 1).trim();
        headers.addHeader(headerName, headerValue);
    }

    /**
     * Creates a new instance.
     *
     * @param ctx The request context.
     * @throws FormUploadException An error occurred while
     *                             parsing the request.
     * @throws IOException         An I/O error occurred.
     */
    private FormParamIterator(RequestContext ctx) throws FormUploadException, IOException {
        if (ctx == null) {
            throw new NullPointerException("ctx parameter");
        }

        String contentType = ctx.getContentType();
        if ((null == contentType) || (!contentType.toLowerCase(Locale.ENGLISH).startsWith(MULTIPART))) {
            throw new InvalidContentTypeException(
                    format("the request doesn't contain a %s or %s stream, content type header is %s",
                           MULTIPART_FORM_DATA, MULTIPART_MIXED, contentType));
        }

        InputStream input = ctx.getInputStream();

        String charEncoding = ctx.getCharacterEncoding();

        boundary = getBoundary(contentType);
        if (boundary.length == 0) {
            IOUtils.closeQuietly(input); // avoid possible resource leak
            throw new FormUploadException("the request was rejected because no multipart boundary was found");
        }

        try {
            multi = new MultipartStream(input, boundary);
        } catch (IllegalArgumentException iae) {
            IOUtils.closeQuietly(input); // avoid possible resource leak
            throw new InvalidContentTypeException(
                    format("The boundary specified in the %s header is too long", CONTENT_TYPE), iae);
        }
        multi.setHeaderEncoding(charEncoding);

        skipPreamble = true;
        findNextItem();
    }

    /**
     * Called for finding the next item, if any.
     *
     * @return True, if an next item was found, otherwise false.
     * @throws IOException An I/O error occurred.
     */
    private boolean findNextItem() {
        if (eof) {
            return false;
        }
        if (currentItem != null) {
            currentItem.close();
            currentItem = null;
        }
        for (;;) {
            boolean nextPart;
            if (skipPreamble) {
                nextPart = multi.skipPreamble();
            } else {
                nextPart = multi.readBoundary();
            }
            if (!nextPart) {
                if (currentFieldName == null) {
                    // Outer multipart terminated -> No more data
                    eof = true;
                    return false;
                }
                // Inner multipart terminated -> Return to parsing the outer
                multi.setBoundary(boundary);
                currentFieldName = null;
                continue;
            }
            FormItemHeader headers = getParsedHeaders(multi.readHeaders());
            if (currentFieldName == null) {
                // We're parsing the outer multipart
                String fieldName = getFieldName(headers);
                if (fieldName != null) {
                    String subContentType = headers.getHeader(CONTENT_TYPE);
                    if (subContentType != null &&
                        subContentType.toLowerCase(Locale.ENGLISH).startsWith(MULTIPART_MIXED)) {
                        currentFieldName = fieldName;
                        // Multiple files associated with this field name
                        byte[] subBoundary = getBoundary(subContentType);
                        multi.setBoundary(subBoundary);
                        skipPreamble = true;
                        continue;
                    }
                    String fileName = getFileName(headers);
                    currentItem = new FormItem(fileName, fieldName, headers.getHeader(CONTENT_TYPE), fileName == null,
                                               getContentLength(headers), multi);
                    currentItem.setHeaders(headers);
                    itemValid = true;
                    return true;
                }
            } else {
                String fileName = getFileName(headers);
                if (fileName != null) {
                    currentItem = new FormItem(fileName, currentFieldName, headers.getHeader(CONTENT_TYPE), false,
                                               getContentLength(headers), multi);
                    currentItem.setHeaders(headers);
                    itemValid = true;
                    return true;
                }
            }
            multi.discardBodyData();
        }
    }

    private long getContentLength(FormItemHeader pHeaders) {
        try {
            return Long.parseLong(pHeaders.getHeader(CONTENT_LENGTH));
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * Returns, whether another instance of {@link FormItem}
     * is available.
     *
     * @return True, if one or more additional file items
     * are available, otherwise false.
     */
    public boolean hasNext() {
        if (eof) {
            return false;
        }
        return itemValid || findNextItem();
    }

    /**
     * Returns the next available {@link FormItem}.
     *
     * @return FileItemStream instance, which provides
     * access to the next file item.
     */
    public FormItem next() {
        if (eof || (!itemValid && !hasNext())) {
            throw new NoSuchElementException();
        }
        itemValid = false;
        return currentItem;
    }
}
