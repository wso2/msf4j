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

package org.wso2.msf4j.examples.petstore.util.fe.client;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;
import org.wso2.msf4j.examples.petstore.util.fe.model.Configuration;
import org.wso2.msf4j.examples.petstore.util.fe.view.LoginBean;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.servlet.http.Part;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

/**
 * Client to access ImageServiceClient.
 */
@ManagedBean
@ApplicationScoped
public class ImageServiceClient extends AbstractServiceClient {

    private static final Logger LOGGER = Logger.getLogger(ImageServiceClient.class.getName());

    @Nullable
    @ManagedProperty("#{configuration}")
    private Configuration configuration;

    public String uploadImage(Part file) throws IOException {
        String imageURL = null;
        final Client client = ClientBuilder.newBuilder().register(MultiPartFeature.class).build();
        final StreamDataBodyPart stream = new StreamDataBodyPart("file", file.getInputStream());
        FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
        final MultiPart multiPart = formDataMultiPart.field("fileName", file.getSubmittedFileName()).bodyPart(stream);
        if (multiPart instanceof FormDataMultiPart) {
            final FormDataMultiPart dataMultiPart = (FormDataMultiPart) multiPart;
            final WebTarget target = client.target(configuration.getFileUploadServiceEP());
            LOGGER.info("Connecting to file service on " + configuration.getFileUploadServiceEP());
            final Response response = target.request().header(LoginBean.X_JWT_ASSERTION, getJWTToken())
                    .post(Entity.entity(dataMultiPart, dataMultiPart.getMediaType()));
            LOGGER.info("Returned from file service " + configuration.getFileUploadServiceEP());
            if (Response.Status.OK.getStatusCode() == response.getStatus()) {
                imageURL = response.readEntity(String.class);
            } else {
                LOGGER.log(Level.SEVERE, "TXN service return code is  " + response.getStatus() + " , " +
                                         "hence can't proceed with the response");
            }
            formDataMultiPart.close();
            dataMultiPart.close();
            return imageURL;
        }

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
