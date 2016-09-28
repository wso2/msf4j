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
package controllers;

import com.google.inject.Singleton;
import ninja.Context;
import ninja.Result;
import ninja.Results;
import ninja.jaxy.Path;
import ninja.jaxy.POST;
import ninja.jaxy.Order;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Singleton
@Path("/EchoService")
public class ApplicationController {

    @Path("/echo")
    @POST
    public Result echo(String text) {
        return Results.text().render(text);
    }

    @Path("/fileecho")
    @POST
    public Result fileecho(String body) throws InterruptedException, IOException {
        java.nio.file.Path tempfile = Files.createTempFile(UUID.randomUUID().toString(), ".tmp");
        Files.write(tempfile, body.getBytes(Charset.defaultCharset()), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        String returnStr = new String(Files.readAllBytes(tempfile), Charset.defaultCharset());
        Files.delete(tempfile);
        return Results.text().render(returnStr);
    }
}
