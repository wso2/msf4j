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
package org.wso2.msf4j;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.msf4j.conf.Constants;
import org.wso2.msf4j.internal.MicroservicesRegistryImpl;
import org.wso2.msf4j.internal.deployer.MicroservicesDeployer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import javax.ws.rs.HttpMethod;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Tests microservice deployer.
 */
public class MSF4JDeployerTest {

    private MicroservicesDeployer deployer;
    private static final String HEADER_KEY_CONNECTION = "CONNECTION";
    private static final String HEADER_VAL_CLOSE = "CLOSE";
    private static final Gson GSON = new Gson();
    private static final Type STRING_MAP_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();
    protected static URI baseURI;

    @BeforeClass
    public void setup() throws Exception {
        MicroservicesRunner microservicesRunner = new MicroservicesRunner(Constants.PORT);
        microservicesRunner.start();
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                org.wso2.msf4j.internal.DataHolder.getInstance().getMicroservicesRegistries();
        microservicesRegistries.put("test", microservicesRunner.getMsRegistry());
        deployer = new MicroservicesDeployer();
        baseURI = URI.create(String.format("http://%s:%d", Constants.HOSTNAME, 8090));
    }

    @AfterClass
    public void teardown() throws Exception {
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                org.wso2.msf4j.internal.DataHolder.getInstance().getMicroservicesRegistries();
        microservicesRegistries.remove("test");
    }

    @Test
    public void testJarArtifactDeployment() throws Exception {
        File file = new File(Thread.currentThread().getContextClassLoader()
                .getResource("stockquote-deployable-jar-2.1.1.jar").getFile());
        Artifact artifact = new Artifact(file);
        deployer.deploy(artifact);

        HttpURLConnection urlConn = request("/stockquote/IBM", HttpMethod.GET);
        assertEquals(HttpURLConnection.HTTP_OK, urlConn.getResponseCode());
        String content = getContent(urlConn);

        Map<String, String> map = GSON.fromJson(content, STRING_MAP_TYPE);
        assertEquals(5, map.size());
        assertEquals("IBM", map.get("symbol"));
        assertEquals("International Business Machines", map.get("name"));
        urlConn.disconnect();
    }

    @Test(dependsOnMethods = "testJarArtifactDeployment")
    public void testJarArtifactUndeployment() throws Exception {
        File file = new File(Thread.currentThread().getContextClassLoader()
                .getResource("stockquote-deployable-jar-2.1.1.jar").getFile());
        deployer.undeploy(file.getAbsolutePath());
        HttpURLConnection urlConn = request("/stockquote/IBM", HttpMethod.GET);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, urlConn.getResponseCode());
    }

    @Test(expectedExceptions = CarbonDeploymentException.class,
            expectedExceptionsMessageRegExp = "Error while processing the artifact.*")
    public void testFatJarArtifactDeployment() throws Exception {
        File file = new File(Thread.currentThread().getContextClassLoader()
                .getResource("stockquote-fatjar-2.1.1.jar").getFile());
        Artifact artifact = new Artifact(file);
        deployer.deploy(artifact);
    }

    @Test(expectedExceptions = CarbonDeploymentException.class,
            expectedExceptionsMessageRegExp = "Error while processing the artifact.*")
    public void testBundleArtifactDeployment() throws Exception {
        File file = new File(Thread.currentThread().getContextClassLoader()
                .getResource("stockquote-bundle-2.1.1.jar").getFile());
        Artifact artifact = new Artifact(file);
        deployer.deploy(artifact);
    }

    private HttpURLConnection request(String path, String method) throws IOException {
        return request(path, method, false);
    }

    private HttpURLConnection request(String path, String method, boolean keepAlive) throws IOException {
        URL url = baseURI.resolve(path).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
            urlConn.setDoOutput(true);
        }
        urlConn.setRequestMethod(method);
        if (!keepAlive) {
            urlConn.setRequestProperty(HEADER_KEY_CONNECTION, HEADER_VAL_CLOSE);
        }

        return urlConn;
    }

    private String getContent(HttpURLConnection urlConn) throws IOException {
        return new String(IOUtils.toByteArray(urlConn.getInputStream()), Charsets.UTF_8);
    }
}
