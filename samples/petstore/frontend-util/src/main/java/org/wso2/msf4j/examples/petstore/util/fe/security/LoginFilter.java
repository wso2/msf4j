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

package org.wso2.msf4j.examples.petstore.util.fe.security;

import org.wso2.msf4j.examples.petstore.util.fe.view.LoginBean;

import java.io.IOException;
import java.util.Collections;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Login Filter.
 */
public class LoginFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(LoginFilter.class.getName());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        printHeaders((HttpServletRequest) request);
        LoginBean loginBean = (LoginBean) ((HttpServletRequest) request).getSession().getAttribute("loginBean");
        if (isRestrictedPath(request) && (loginBean == null || !loginBean.isLoggedIn())) {
            String contextPath = ((HttpServletRequest) request).getContextPath();
            ((HttpServletResponse) response).sendRedirect(contextPath + "/login.xhtml");
        }
        ((HttpServletRequest) request).getUserPrincipal();
        chain.doFilter(request, response);
    }

    private boolean isRestrictedPath(ServletRequest request) {
        String path = ((HttpServletRequest) request).getRequestURL().toString();
        if (path.contains("login.xhtml") || path.contains(".css")) {
            return false;
        }
        return true;
    }

    private void printHeaders(HttpServletRequest request) {
        LOGGER.info("................... Printing HTTP Headers ............");
        for (String key : Collections.list(request.getHeaderNames())) {
            LOGGER.info(key + " = " + request.getHeader(key));
        }
        LOGGER.info("......................................................");
    }

    @Override
    public void destroy() {
    }
}
