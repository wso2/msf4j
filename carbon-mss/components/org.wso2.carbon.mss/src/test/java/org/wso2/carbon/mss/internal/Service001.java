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

package org.wso2.carbon.mss.internal;

import com.google.common.base.Charsets;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.wso2.carbon.mss.HttpResponder;
import org.wso2.carbon.mss.HttpStreaming;
import org.wso2.carbon.mss.MicroservicesRunner;
import org.wso2.carbon.mss.internal.router.BodyConsumer;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

@Path("/Service001")
public class Service001 {

    @Path("/chunkedReq")
    @POST
    public void testChunkedReq(@Context HttpStreaming httpStreaming) throws Exception {
        final StringBuffer sb = new StringBuffer();
        httpStreaming.bodyConsumer(new BodyConsumer() {
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
            public void handleError(Throwable cause) {

            }
        });
    }

    @Path("/noChunkedReq")
    @POST
    public String testNoChunkedReq(String content) throws Exception {
        return content;
    }


    @Path("/string")
    @GET
    public String stringResp() {
        return "Samiyuru";
    }

    public static void main(String[] args) {
        System.setProperty("org.jboss.netty.debug", "true");
        new MicroservicesRunner().deploy(new Service001()).start();
    }

}
