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

package org.wso2.msf4j.example;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.HttpStreamHandler;
import org.wso2.msf4j.HttpStreamer;
import org.wso2.msf4j.internal.mime.MimeMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 * FileServer service class. This uses request streaming
 * to save the submitted files in the server. Also this demonstrates
 * how to respond with files.
 */
@Path("/")
public class FileServer {

    private static final Logger log = LoggerFactory.getLogger(FileServer.class);
    private static final java.nio.file.Path MOUNT_PATH = Paths.get(".");

    /**
     * Upload a file with streaming.
     *
     * @param httpStreamer Handle for setting the {@link HttpStreamHandler}callback for streaming.
     * @param fileName     Name of the file that was uploaded.
     * @throws IOException
     */
    @POST
    @Path("/{fileName}")
    public void postFile(@Context HttpStreamer httpStreamer,
                         @PathParam("fileName") String fileName) throws IOException {
        httpStreamer.callback(new HttpStreamHandlerImpl(fileName));
    }

    /**
     * Download file with streaming using a {@link File} object in the response. Streaming is automatically handled
     * by MSF4J.
     *
     * @param fileName Name of the file to be downloaded.
     * @return Response
     */
    @GET
    @Path("/{fileName}")
    public Response getFile(@PathParam("fileName") String fileName) {
        File file = Paths.get(MOUNT_PATH.toString(), fileName).toFile();
        if (file.exists()) {
            return Response.ok(file).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Download file with streaming using a {@link java.io.InputStream} object in the response.
     * Streaming is automatically handled by MSF4J.
     *
     * @param fileName Name of the file to be downloaded.
     * @return Response
     */
    @GET
    @Path("/ip/{fileName}")
    public Response getFileFromInputStream(@PathParam("fileName") String fileName) throws FileNotFoundException {
        String mimeType = MimeMapper.getMimeType(FilenameUtils.getExtension(fileName));
        File file = Paths.get(MOUNT_PATH.toString(), fileName).toFile();
        return Response.ok(new FileInputStream(file)).type(mimeType).build();
    }

    /**
     * Download file with Streaming using using {@link javax.ws.rs.core.StreamingOutput}.
     * You have more control over streaming chunk sizes when this method is used.
     *
     * @param fileName Name of the file to be downloaded.
     * @return Response
     */
    @GET
    @Path("/op/{fileName}")
    public Response getFileUsingStreamingOutput(@PathParam("fileName") String fileName) {
        StreamingOutput stream = os -> {
            File file = Paths.get(MOUNT_PATH.toString(), fileName).toFile();
            int chunkSize = 4096;
            byte[] buf = new byte[chunkSize];
            InputStream is = new FileInputStream(file);
            int c = 0;
            while ((c = is.read(buf, 0, buf.length)) > 0) {
                os.write(buf, 0, c);
                os.flush();
            }
            os.close();
            is.close();
        };
        String mimeType = MimeMapper.getMimeType(FilenameUtils.getExtension(fileName));
        return Response.ok(stream).type(mimeType).build();
    }

    private static class HttpStreamHandlerImpl implements HttpStreamHandler {
        private FileChannel fileChannel = null;
        private org.wso2.msf4j.Response response;

        public HttpStreamHandlerImpl(String fileName) throws FileNotFoundException {
            File file = Paths.get(MOUNT_PATH.toString(), fileName).toFile();
            if (file.getParentFile().exists() || file.getParentFile().mkdirs()) {
                fileChannel = new FileOutputStream(file).getChannel();
            }
        }

        @Override
        public void init(org.wso2.msf4j.Response response) {
            this.response = response;
        }

        @Override
        public void end() throws Exception {
            fileChannel.close();
            response.setStatus(Response.Status.ACCEPTED.getStatusCode());
            response.send();
        }

        @Override
        public void chunk(ByteBuffer content) throws Exception {
            if (fileChannel == null) {
                throw new IOException("Unable to write file");
            }
            fileChannel.write(content);
        }

        @Override
        public void error(Throwable cause) {
            try {
                if (fileChannel != null) {
                    fileChannel.close();
                }
            } catch (IOException e) {
                // Log if unable to close the output stream
                log.error("Unable to close file output stream", e);
            }
        }
    }
}
