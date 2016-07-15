package org.wso2.msf4j.formparam.util;
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

import org.apache.commons.io.IOUtils;
import org.wso2.msf4j.formparam.exception.FormUploadException;
import org.wso2.msf4j.formparam.exception.InvalidFileNameException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * Utility class for working with streams.
 */
public final class StreamUtil {

    /**
     * Private constructor, to prevent instantiation.
     * This class has only static methods.
     */
    private StreamUtil() {
        // Does nothing
    }

    /**
     * Default buffer size for use in
     * {@link #copy(InputStream, OutputStream, boolean)}.
     */
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * Copies the contents of the given {@link InputStream}
     * to the given {@link OutputStream}. Shortcut for
     * <pre>
     *   copy(pInputStream, pOutputStream, new byte[8192]);
     * </pre>
     *
     * @param inputStream       The input stream, which is being read.
     *                          It is guaranteed, that {@link InputStream#close()} is called
     *                          on the stream.
     * @param outputStream      The output stream, to which data should
     *                          be written. May be null, in which case the input streams
     *                          contents are simply discarded.
     * @param closeOutputStream True guarantees, that {@link OutputStream#close()}
     *                          is called on the stream. False indicates, that only
     *                          {@link OutputStream#flush()} should be called finally.
     * @return Number of bytes, which have been copied.
     */
    public static long copy(InputStream inputStream, OutputStream outputStream, boolean closeOutputStream) {
        return copy(inputStream, outputStream, closeOutputStream, new byte[DEFAULT_BUFFER_SIZE]);
    }

    /**
     * Copies the contents of the given {@link InputStream}
     * to the given {@link OutputStream}.
     *
     * @param inputStream       The input stream, which is being read.
     *                          It is guaranteed, that {@link InputStream#close()} is called
     *                          on the stream.
     * @param outputStream      The output stream, to which data should
     *                          be written. May be null, in which case the input streams
     *                          contents are simply discarded.
     * @param closeOutputStream True guarantees, that {@link OutputStream#close()}
     *                          is called on the stream. False indicates, that only
     *                          {@link OutputStream#flush()} should be called finally.
     * @param buffer            Temporary buffer, which is to be used for
     *                          copying data.
     * @return Number of bytes, which have been copied.
     */
    public static long copy(InputStream inputStream, OutputStream outputStream, boolean closeOutputStream,
                            byte[] buffer) {
        OutputStream out = outputStream;
        InputStream in = inputStream;
        try {
            long total = 0;
            for (;;) {
                int res = in.read(buffer);
                if (res == -1) {
                    break;
                }
                if (res > 0) {
                    total += res;
                    if (out != null) {
                        out.write(buffer, 0, res);
                    }
                }
            }
            if (out != null) {
                if (!closeOutputStream) {
                    out.flush();
                }
            }
            return total;
        } catch (IOException e) {
            throw new FormUploadException("Error while copying streams", e);
        } finally {
            IOUtils.closeQuietly(in);
            if (closeOutputStream) {
                IOUtils.closeQuietly(out);
            }
        }
    }

    /**
     * This convenience method allows to read a
     * {@link org.wso2.msf4j.formparam.FormItem}'s
     * content into a string. The platform's default character encoding
     * is used for converting bytes into characters.
     *
     * @param inputStream The input stream to read.
     * @return The streams contents, as a string.
     * @throws IOException An I/O error occurred.
     * @see #asString(InputStream, String)
     */
    public static String asString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copy(inputStream, baos, true);
        return baos.toString(Charset.defaultCharset().name());
    }

    /**
     * This convenience method allows to read a
     * {@link org.wso2.msf4j.formparam.FormItem}'s
     * content into a string, using the given character encoding.
     *
     * @param inputStream The input stream to read.
     * @param encoding    The character encoding, typically "UTF-8".
     * @return The streams contents, as a string.
     * @throws IOException An I/O error occurred.
     * @see #asString(InputStream)
     */
    public static String asString(InputStream inputStream, String encoding) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copy(inputStream, baos, true);
        return baos.toString(encoding);
    }

    /**
     * Checks, whether the given file name is valid in the sense,
     * that it doesn't contain any NUL characters. If the file name
     * is valid, it will be returned without any modifications. Otherwise,
     * an {@link InvalidFileNameException} is raised.
     *
     * @param fileName The file name to check
     * @return Unmodified file name, if valid.
     */
    public static String checkFileName(String fileName) {
        if (fileName != null && fileName.indexOf('\u0000') != -1) {
            // pFileName.replace("\u0000", "\\0")
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < fileName.length(); i++) {
                char c = fileName.charAt(i);
                switch (c) {
                    case 0:
                        sb.append("\\0");
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            }
            throw new InvalidFileNameException(fileName, "Invalid file name: " + sb);
        }
        return fileName;
    }

}
