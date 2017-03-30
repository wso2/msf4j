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

package org.wso2.msf4j.util;

import org.testng.annotations.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Tests for QueryStringDecoderUtil.
 */
public class QueryStringDecoderUtilTest {

    @Test
    public void testBasicUris() throws URISyntaxException {
        QueryStringDecoderUtil d = new QueryStringDecoderUtil(new URI("http://localhost/path"));
        assertEquals(0, d.parameters().size());
    }

    @Test
    public void testBasic() throws Exception {
        QueryStringDecoderUtil d;

        d = new QueryStringDecoderUtil("/foo?a=b=c");
        assertEquals("/foo", d.path());
        assertEquals(1, d.parameters().size());
        assertEquals(1, d.parameters().get("a").size());
        assertEquals("b=c", d.parameters().get("a").get(0));

        d = new QueryStringDecoderUtil("/foo?a=1&a=2");
        assertEquals("/foo", d.path());
        assertEquals(1, d.parameters().size());
        assertEquals(2, d.parameters().get("a").size());
        assertEquals("1", d.parameters().get("a").get(0));
        assertEquals("2", d.parameters().get("a").get(1));

        d = new QueryStringDecoderUtil("/foo?a=&a=2");
        assertEquals("/foo", d.path());
        assertEquals(1, d.parameters().size());
        assertEquals(2, d.parameters().get("a").size());
        assertEquals("", d.parameters().get("a").get(0));
        assertEquals("2", d.parameters().get("a").get(1));

        d = new QueryStringDecoderUtil("/foo?a=1&a=");
        assertEquals("/foo", d.path());
        assertEquals(1, d.parameters().size());
        assertEquals(2, d.parameters().get("a").size());
        assertEquals("1", d.parameters().get("a").get(0));
        assertEquals("", d.parameters().get("a").get(1));

        d = new QueryStringDecoderUtil("/foo?a=1&a=&a=");
        assertEquals(d.path(), "/foo");
        assertEquals(d.parameters().size(), 1);
        assertEquals(3, d.parameters().get("a").size());
        assertEquals("1", d.parameters().get("a").get(0));
        assertEquals("", d.parameters().get("a").get(1));
        assertEquals("", d.parameters().get("a").get(2));

        d = new QueryStringDecoderUtil("/foo?a=1=&a==2");
        assertEquals("/foo", d.path());
        assertEquals(1, d.parameters().size());
        assertEquals(2, d.parameters().get("a").size());
        assertEquals("1=", d.parameters().get("a").get(0));
        assertEquals("=2", d.parameters().get("a").get(1));
    }

    @Test
    public void testExotic() throws Exception {
        assertQueryString("", "");
        assertQueryString("foo", "foo");
        assertQueryString("/foo", "/foo");
        assertQueryString("?a=", "?a");
        assertQueryString("foo?a=", "foo?a");
        assertQueryString("/foo?a=", "/foo?a");
        assertQueryString("/foo?a=", "/foo?a&");
        assertQueryString("/foo?a=", "/foo?&a");
        assertQueryString("/foo?a=", "/foo?&a&");
        assertQueryString("/foo?a=", "/foo?&=a");
        assertQueryString("/foo?a=", "/foo?=a&");
        assertQueryString("/foo?a=", "/foo?a=&");
        assertQueryString("/foo?a=b&c=d", "/foo?a=b&&c=d");
        assertQueryString("/foo?a=b&c=d", "/foo?a=b&=&c=d");
        assertQueryString("/foo?a=b&c=d", "/foo?a=b&==&c=d");
        assertQueryString("/foo?a=b&c=&x=y", "/foo?a=b&c&x=y");
        assertQueryString("/foo?a=", "/foo?a=");
        assertQueryString("/foo?a=", "/foo?&a=");
        assertQueryString("/foo?a=b&c=d", "/foo?a=b&c=d");
        assertQueryString("/foo?a=1&a=&a=", "/foo?a=1&a&a=");
    }

    @Test
    public void testHashDos() throws Exception {
        StringBuilder buf = new StringBuilder();
        buf.append('?');
        for (int i = 0; i < 65536; i++) {
            buf.append('k');
            buf.append(i);
            buf.append("=v");
            buf.append(i);
            buf.append('&');
        }
        assertEquals(1024, new QueryStringDecoderUtil(buf.toString()).parameters().size());
    }

    @Test
    public void testHasPath() throws Exception {
        QueryStringDecoderUtil decoder = new QueryStringDecoderUtil("1=2", false);
        assertEquals("", decoder.path());
        Map<String, List<String>> params = decoder.parameters();
        assertEquals(1, params.size());
        assertTrue(params.containsKey("1"));
        List<String> param = params.get("1");
        assertNotNull(param);
        assertEquals(1, param.size());
        assertEquals("2", param.get(0));
    }

    @Test
    public void testUrlDecoding() throws Exception {
        final String caffe = new String(
                // "Caffé" but instead of putting the literal E-acute in the
                // source file, we directly use the UTF-8 encoding so as to
                // not rely on the platform's default encoding (not portable).
                new byte[] { 'C', 'a', 'f', 'f', (byte) 0xC3, (byte) 0xA9 }, "UTF-8");
        final String[] tests = {
                // Encoded   ->   Decoded or error message substring
                "", "", "foo", "foo", "f%%b", "f%b", "f+o", "f o", "f++", "f  ", "fo%", "unterminated escape sequence",
                "%42", "B", "%5f", "_", "f%4", "partial escape sequence", "%x2",
                "invalid escape sequence `%x2' at index 0 of: %x2", "%4x",
                "invalid escape sequence `%4x' at index 0 of: %4x", "Caff%C3%A9", caffe, };
        for (int i = 0; i < tests.length; i += 2) {
            final String encoded = tests[i];
            final String expected = tests[i + 1];
            try {
                final String decoded = QueryStringDecoderUtil.decodeComponent(encoded);
                assertEquals(expected, decoded);
            } catch (IllegalArgumentException e) {
                assertTrue("String " + e.getMessage() + "\" does not contain \"" + expected + '"',
                           e.getMessage().contains(expected));
            }
        }
    }

    private static void assertQueryString(String expected, String actual) {
        QueryStringDecoderUtil ed = new QueryStringDecoderUtil(expected, Charset.defaultCharset());
        QueryStringDecoderUtil ad = new QueryStringDecoderUtil(actual, Charset.defaultCharset());
        assertEquals(ed.path(), ad.path());
        assertEquals(ed.parameters(), ad.parameters());
    }

    // See #189
    @Test
    public void testURI() {
        URI uri = URI.create("http://localhost:8080/foo?param1=value1&param2=value2&param3=value3");
        QueryStringDecoderUtil decoder = new QueryStringDecoderUtil(uri);
        assertEquals("/foo", decoder.path());
        Map<String, List<String>> params = decoder.parameters();
        assertEquals(3, params.size());
        Iterator<Entry<String, List<String>>> entries = params.entrySet().iterator();

        Entry<String, List<String>> entry = entries.next();
        assertEquals("param1", entry.getKey());
        assertEquals(1, entry.getValue().size());
        assertEquals("value1", entry.getValue().get(0));

        entry = entries.next();
        assertEquals("param2", entry.getKey());
        assertEquals(1, entry.getValue().size());
        assertEquals("value2", entry.getValue().get(0));

        entry = entries.next();
        assertEquals("param3", entry.getKey());
        assertEquals(1, entry.getValue().size());
        assertEquals("value3", entry.getValue().get(0));

        assertFalse(entries.hasNext());
    }

    // See #189
    @Test
    public void testURISlashPath() {
        URI uri = URI.create("http://localhost:8080/?param1=value1&param2=value2&param3=value3");
        QueryStringDecoderUtil decoder = new QueryStringDecoderUtil(uri);
        assertEquals("/", decoder.path());
        Map<String, List<String>> params = decoder.parameters();
        assertEquals(3, params.size());
        Iterator<Entry<String, List<String>>> entries = params.entrySet().iterator();

        Entry<String, List<String>> entry = entries.next();
        assertEquals("param1", entry.getKey());
        assertEquals(1, entry.getValue().size());
        assertEquals("value1", entry.getValue().get(0));

        entry = entries.next();
        assertEquals("param2", entry.getKey());
        assertEquals(1, entry.getValue().size());
        assertEquals("value2", entry.getValue().get(0));

        entry = entries.next();
        assertEquals("param3", entry.getKey());
        assertEquals(1, entry.getValue().size());
        assertEquals("value3", entry.getValue().get(0));

        assertFalse(entries.hasNext());
    }

    // See #189
    @Test
    public void testURINoPath() {
        URI uri = URI.create("http://localhost:8080?param1=value1&param2=value2&param3=value3");
        QueryStringDecoderUtil decoder = new QueryStringDecoderUtil(uri);
        assertEquals("", decoder.path());
        Map<String, List<String>> params = decoder.parameters();
        assertEquals(3, params.size());
        Iterator<Entry<String, List<String>>> entries = params.entrySet().iterator();

        Entry<String, List<String>> entry = entries.next();
        assertEquals("param1", entry.getKey());
        assertEquals(1, entry.getValue().size());
        assertEquals("value1", entry.getValue().get(0));

        entry = entries.next();
        assertEquals("param2", entry.getKey());
        assertEquals(1, entry.getValue().size());
        assertEquals("value2", entry.getValue().get(0));

        entry = entries.next();
        assertEquals("param3", entry.getKey());
        assertEquals(1, entry.getValue().size());
        assertEquals("value3", entry.getValue().get(0));

        assertFalse(entries.hasNext());
    }

    // See https://github.com/netty/netty/issues/1833
    @Test
    public void testURI2() {
        URI uri = URI.create("http://foo.com/images;num=10?query=name;value=123");
        QueryStringDecoderUtil decoder = new QueryStringDecoderUtil(uri);
        assertEquals("/images;num=10", decoder.path());
        Map<String, List<String>> params = decoder.parameters();
        assertEquals(2, params.size());
        Iterator<Entry<String, List<String>>> entries = params.entrySet().iterator();

        Entry<String, List<String>> entry = entries.next();
        assertEquals("query", entry.getKey());
        assertEquals(1, entry.getValue().size());
        assertEquals("name", entry.getValue().get(0));

        entry = entries.next();
        assertEquals("value", entry.getKey());
        assertEquals(1, entry.getValue().size());
        assertEquals("123", entry.getValue().get(0));

        assertFalse(entries.hasNext());
    }
}
