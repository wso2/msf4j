/*
 * Copyright © 2014 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.wso2.carbon.mss.internal.router;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * BasicHandlerContext returns an empty runtime arguments.
 */
public class BasicHandlerContext implements HandlerContext {
    private final HttpResourceHandler httpResourceHandler;

    public BasicHandlerContext(HttpResourceHandler httpResourceHandler) {
        this.httpResourceHandler = httpResourceHandler;
    }

    @Override
    public Map<String, String> getRuntimeArguments() {
        return ImmutableMap.of();
    }

    @Override
    public HttpResourceHandler getHttpResourceHandler() {
        return httpResourceHandler;
    }
}
