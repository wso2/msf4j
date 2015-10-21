/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.mss.examples.petstore.admin.client;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.wso2.carbon.mss.examples.petstore.admin.model.Configuration;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.servlet.http.Part;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.io.IOException;

@ManagedBean
@ApplicationScoped
public class ImageServiceClient {

    @ManagedProperty("#{configuration}")
    private Configuration configuration;

    public String uploadImage(Part file) throws IOException {
        String imageURL = null;
        final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
        final StreamDataBodyPart stream = new StreamDataBodyPart("file", file.getInputStream());
        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        final FormDataMultiPart multipart = (FormDataMultiPart) formDataMultiPart.field("fileName", file.getSubmittedFileName()).bodyPart(stream);
        final WebTarget target = client.target(configuration.getFileUploadServiceEP());
        final Response response = target.request().post(Entity.entity(multipart, multipart.getMediaType()));
        if (Response.Status.OK.getStatusCode() == response.getStatus()) {
            imageURL = response.readEntity(String.class);
        }
        formDataMultiPart.close();
        multipart.close();
        return imageURL;
    }

    public boolean deleteImage(String url) {
        //TODO -
        return false;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
