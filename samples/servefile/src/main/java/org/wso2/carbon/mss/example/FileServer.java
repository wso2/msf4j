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

package org.wso2.carbon.mss.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * File server resource class.
 */
@Path("/file")
public class FileServer {

    private static final Logger log = LoggerFactory.getLogger(FileServer.class);
    File file = null;

    public FileServer() {
        // Create temp file.
        OutputStreamWriter out = null;
        try {
            file = File.createTempFile("data", ".txt");
            file.deleteOnExit();
            out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            out.write("DATA FILE CONTENT");
        } catch (IOException e) {
            log.error("Unable to create the temp file", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error("Unable to close the temp file", e);
                }
            }
        }
    }

    /**
     * Resource method that serves the file.
     * This method will return a Response object that
     * contains a File entity and status code 200 or
     * status code 404 if file is not available
     *
     * @return Response object
     */
    @GET
    public Response serveFile() {
        if (file != null) {
            return Response.ok(file).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

}
