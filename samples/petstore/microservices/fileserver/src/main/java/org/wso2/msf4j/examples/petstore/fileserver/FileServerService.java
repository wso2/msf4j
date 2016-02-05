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

package org.wso2.msf4j.examples.petstore.fileserver;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.annotation.Timed;
import org.wso2.msf4j.HttpResponder;
import org.wso2.msf4j.HttpStreamHandler;
import org.wso2.msf4j.HttpStreamer;
import org.wso2.msf4j.analytics.httpmonitoring.HTTPMonitored;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * FileServer service class. This uses request streaming
 * to save the submitted files in the server.
 */
@HTTPMonitored
@Path("/")
public class FileServerService {

    private static final Logger log = LoggerFactory.getLogger(FileServerService.class);
    private static final String MOUNT_PATH = "/var/www/html/upload";

    @POST
    @Path("/{fileName}")
    @Timed
    public void postFile(@Context HttpStreamer httpStreamer,
                         @PathParam("fileName") String fileName)
            throws IOException {
        httpStreamer.callback(new HttpStreamHandlerImpl(fileName));
    }

    @GET
    @Path("/{fileName}")
    @Timed
    public Response getFile(@PathParam("fileName") String fileName) {
        File file = Paths.get(MOUNT_PATH, fileName).toFile();
        if (file.exists()) {
            return Response.ok(file).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    private static class HttpStreamHandlerImpl implements HttpStreamHandler {
        private BufferedOutputStream outputStream = null;

        public HttpStreamHandlerImpl(String fileName) throws FileNotFoundException {
            File file = Paths.get(MOUNT_PATH, fileName).toFile();
            if (file.getParentFile().exists() || file.getParentFile().mkdirs()) {
                outputStream = new BufferedOutputStream(new FileOutputStream(file));
            }
        }

        @Override
        public void chunk(ByteBuf request, HttpResponder responder) throws IOException {
            if (outputStream == null) {
                throw new IOException("Unable to write file");
            }
            request.readBytes(outputStream, request.capacity());
        }

        @Override
        public void finished(ByteBuf request, HttpResponder responder) throws IOException {
            if (outputStream == null) {
                throw new IOException("Unable to write file");
            }
            request.readBytes(outputStream, request.capacity());
            outputStream.close();
            responder.sendStatus(HttpResponseStatus.ACCEPTED);
        }

        @Override
        public void error(Throwable cause) {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                // Log if unable to close the output stream
                log.error("Unable to close file output stream", e);
            }
        }
    }
}
