/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.mss.template;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.IOException;
import java.io.StringWriter;

/**
 * Wrapper for mustache template engine that implements
 * org.wso2.carbon.mss.template.TemplateEngine.
 */
public class MustacheTemplateEngine implements TemplateEngine {

    private MustacheFactory mustacheFactory;
    private static MustacheTemplateEngine mustacheTemplateEngine = null;

    /**
     * Constructs a mustache template engine
     */
    private MustacheTemplateEngine() {
        mustacheFactory = new DefaultMustacheFactory("templates");
    }

    /**
     * Render a given model from a given template.
     *
     * @param view  name of the template file in resources/templates directory
     * @param model model to be rendered from the template
     * @return rendered template
     */
    @Override
    public String render(String view, Object model) {
        Mustache mustache = mustacheFactory.compile(view);
        StringWriter stringWriter = new StringWriter();
        try {
            mustache.execute(stringWriter, model).close();
        } catch (IOException e) {
            throw new RuntimeTemplateException(e);
        }
        return stringWriter.toString();
    }

    /**
     * @return MustacheTemplateEngine singleton
     */
    public static MustacheTemplateEngine instance() {
        if (mustacheTemplateEngine == null) {
            synchronized (MustacheTemplateEngine.class) {
                if (mustacheTemplateEngine == null) {
                    mustacheTemplateEngine = new MustacheTemplateEngine();
                }
            }
        }
        return mustacheTemplateEngine;
    }
}
