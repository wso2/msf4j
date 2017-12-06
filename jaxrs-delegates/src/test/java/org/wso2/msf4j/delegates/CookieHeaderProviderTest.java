/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.msf4j.delegates;

import org.testng.annotations.Test;

import java.util.Date;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.NewCookie;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class CookieHeaderProviderTest {

    @Test
    public void testFromString() throws Exception {
        Cookie cookie = Cookie.valueOf("JSESSIONID=3508015E4EF0ECA8C4B761FCC4BC1718");
        assertEquals(cookie.getName(), "JSESSIONID");
        assertEquals(cookie.getValue(), "3508015E4EF0ECA8C4B761FCC4BC1718");
    }

    @Test
    public void testFromStringWithExtendedParameters() {
        String cookieString = "Version=1; Application=msf4j; Path=/carbon; Domain=wso2; Expires=Sun, 06 Nov 1994 " +
                "08:49:37 GMT; Secure; HttpOnly; MaxAge=50; Comment=TestOnly";
        String name = "Application";
        String value = "msf4j";
        String path = "/carbon";
        String domain = "wso2";
        long dateTime = 784111777000L;

        NewCookie cookie = (NewCookie) Cookie.valueOf(cookieString);
        assertEquals(cookie.getName(), name);
        assertEquals(cookie.getValue(), value);
        assertEquals(cookie.getPath(), path);
        assertEquals(cookie.getVersion(), 1);
        assertEquals(cookie.getDomain(), domain);
        assertEquals(cookie.getComment(), "TestOnly");
        assertEquals(cookie.getExpiry().getTime(), dateTime);
        assertEquals(cookie.getMaxAge(), 50);
        assertEquals(cookie.isSecure(), true);
        assertEquals(cookie.isHttpOnly(), true);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Cookie value can " +
            "not be null")
    public void testFromStringWithoutCookieString() {
        Cookie.valueOf(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFromStringWithoutName() {
        String cookieString = "Version=1; Path=/carbon; Domain=wso2";
        Cookie.valueOf(cookieString);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testFromStringWithoutValue() {
        String cookieString = "Version=1; Application;  Path=/carbon; Domain=wso2";
        Cookie.valueOf(cookieString);
    }

    @Test
    public void testToString() throws Exception {
        String expectedString = "Version=2;Application=msf4j;Path=/carbon;Domain=wso2;MaxAge=50;Comment=TestOnly;" +
                "Expires=Sun, 06 Nov 1994 08:49:37 GMT;Secure;HttpOnly";
        Cookie cookie = new NewCookie("Application", "msf4j", "/carbon", "wso2", 2, "TestOnly", 50, new Date
                (784111777000L), true, true);
        CookieHeaderProvider cookieHeaderProvider = new CookieHeaderProvider();
        String cookieString = cookieHeaderProvider.toString(cookie);
        assertNotNull(cookieString);
        assertEquals(cookieString, expectedString);
    }
}
