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

package org.wso2.msf4j.template;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests for MustacheTemplateEngine
 */
public class MustacheTemplateEngineTest {

    @Test
    public void testTemplateWithNoModel() {
        String rendered = MustacheTemplateEngine.instance().render("nomodel.mustache", null);
        Assert.assertEquals("Hello, world!", rendered);
    }

    @Test
    public void testTemplateWithAModel() {
        String content = "MODEL_CONTENT";
        Map<String, String> map = new HashMap<>();
        map.put("name", content);
        String rendered = MustacheTemplateEngine.instance().render("withmodel.mustache", map);
        Assert.assertEquals("Hello, " + content + "!", rendered);
    }

}
