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


import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.wso2.carbon.mss.HttpResponder;
import org.wso2.carbon.mss.HttpStreamHandler;
import org.wso2.carbon.mss.HttpStreamer;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

/**
 * Streamer service resource class
 */
@Path("/streamer")
public class StreamerService {

    @POST
    @Path("/stream")
    @Consumes("text/plain")
    public void stream(@Context HttpStreamer httpStreamer) {
        httpStreamer.callback(new HttpStreamHandlerImpl());
    }

    @POST
    @Path("/aggregate")
    @Consumes("text/plain")
    public String aggregate(String content) {
        return content;
    }

    private static class HttpStreamHandlerImpl implements HttpStreamHandler {
        final StringBuffer sb = new StringBuffer();

        @Override
        public void chunk(ByteBuf request, HttpResponder responder) {
            sb.append(request.toString(Charsets.UTF_8));
        }

        @Override
        public void finished(ByteBuf request, HttpResponder responder) {
            sb.append(request.toString(Charsets.UTF_8));
            responder.sendString(HttpResponseStatus.OK, sb.toString());
        }

        @Override
        public void error(Throwable cause) {
            sb.delete(0, sb.length());
        }
    }

}
