/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.wso2.msf4j.deployer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.deployment.engine.Artifact;
import org.wso2.carbon.deployment.engine.exception.CarbonDeploymentException;
import org.wso2.msf4j.MicroservicesRegistry;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.deployer.internal.DataHolder;
import org.wso2.msf4j.deployer.internal.MicroservicesDeployer;
import org.wso2.msf4j.internal.MicroservicesRegistryImpl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import javax.ws.rs.HttpMethod;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * Tests microservice deployer.
 */
public class MSF4JDeployerTest {

    private static final String HOSTNAME = "localhost";
    private static final int PORT = 8090;
    private MicroservicesDeployer deployer;
    private static final String HEADER_VAL_CLOSE = "CLOSE";
    private static final Gson GSON = new Gson();
    private static final Type STRING_MAP_TYPE = new TypeToken<Map<String, String>>() {
    }.getType();
    protected static URI baseURI;
    private String stockqoutesSamplesFile;

    @BeforeClass
    public void setup() throws Exception {
        MicroservicesRunner microservicesRunner = new MicroservicesRunner(PORT);
        microservicesRunner.start();
        Map<String, MicroservicesRegistry> microservicesRegistries =
                DataHolder.getInstance().getMicroservicesRegistries();
        microservicesRegistries.put("test", microservicesRunner.getMsRegistry());
        deployer = new MicroservicesDeployer();
        baseURI = URI.create(String.format("http://%s:%d", HOSTNAME, 8090));
        // get the stockquote sample project path. sample.filepath property is added to surefire plugins configuration
        String rootDirectory = Paths.get(System.getProperty("project.filepath")).getParent().toString();
        stockqoutesSamplesFile = Paths.get(rootDirectory, "samples", "stockquote").toString();
    }

    @AfterClass
    public void teardown() throws Exception {
        Map<String, MicroservicesRegistryImpl> microservicesRegistries =
                org.wso2.msf4j.internal.DataHolder.getInstance().getMicroservicesRegistries();
        microservicesRegistries.remove("test");
    }

    @Test
    public void testJarArtifactDeployment() throws Exception {
        // compile the stockqoute deployable-jar sample
        compileTestSamples(Paths.get(stockqoutesSamplesFile, "deployable-jar", "pom.xml").toFile());
        // get the jar file path
        Optional<Path> path = getSampleJarFile(Paths.get(stockqoutesSamplesFile, "deployable-jar", "target"));
        assertTrue("Sample artifact doesn't found in output directory : "
                + Paths.get(stockqoutesSamplesFile, "deployable-jar", "target"), path.isPresent());
        File file = path.get().toFile();
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
        Optional<Path> path = getSampleJarFile(Paths.get(stockqoutesSamplesFile, "deployable-jar", "target"));
        assertTrue("Sample artifact doesn't found in output directory : "
                + Paths.get(stockqoutesSamplesFile, "deployable-jar", "target"), path.isPresent());
        File file = path.get().toFile();
        deployer.undeploy(file.getAbsolutePath());
        HttpURLConnection urlConn = request("/stockquote/IBM", HttpMethod.GET);
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, urlConn.getResponseCode());
    }

    @Test(expectedExceptions = CarbonDeploymentException.class,
            expectedExceptionsMessageRegExp = "Error while processing the artifact.*")
    public void testFatJarArtifactDeployment() throws Exception {
        // compile the stockqoute fatjar sample
        compileTestSamples(Paths.get(stockqoutesSamplesFile, "fatjar", "pom.xml").toFile());
        Optional<Path> path = getSampleJarFile(Paths.get(stockqoutesSamplesFile, "fatjar", "target"));
        assertTrue("Sample artifact doesn't found in output directory : "
                + Paths.get(stockqoutesSamplesFile, "fatjar", "target"), path.isPresent());
        File file = path.get().toFile();
        Artifact artifact = new Artifact(file);
        deployer.deploy(artifact);
    }

    @Test(expectedExceptions = CarbonDeploymentException.class,
            expectedExceptionsMessageRegExp = "Error while processing the artifact.*")
    public void testBundleArtifactDeployment() throws Exception {
        // compile the stockqoute bundle sample
        compileTestSamples(Paths.get(stockqoutesSamplesFile, "bundle", "pom.xml").toFile());
        Optional<Path> path = getSampleJarFile(Paths.get(stockqoutesSamplesFile, "bundle", "target"));
        assertTrue("Sample artifact doesn't found in output directory : "
                + Paths.get(stockqoutesSamplesFile, "bundle", "target"), path.isPresent());
        File file = path.get().toFile();
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
            urlConn.setRequestProperty(HttpHeaderNames.CONNECTION.toString(), HEADER_VAL_CLOSE);
        }

        return urlConn;
    }

    private String getContent(HttpURLConnection urlConn) throws IOException {
        return new String(IOUtils.toByteArray(urlConn.getInputStream()), Charsets.UTF_8);
    }

    /**
     * compile the sample project in the given filepath.
     * @param projectFile sample project pom file location
     * @throws MavenInvocationException
     */
    private void compileTestSamples(File projectFile) throws MavenInvocationException {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile(projectFile);
        request.setGoals(Collections.singletonList("install"));

        Invoker invoker = new DefaultInvoker();
        String mavenLocalRepo = System.getProperty("maven.repo.local");
        if (mavenLocalRepo != null && !mavenLocalRepo.isEmpty()) {
            invoker.setLocalRepositoryDirectory(new File(mavenLocalRepo));
        }
        invoker.execute(request);
    }

    /**
     * Returns the jar file in the given project target directory location.
     * @param targetDirectory target file path
     * @return
     * @throws IOException
     */
    private Optional<Path> getSampleJarFile(Path targetDirectory) throws IOException {
        try (Stream<Path> paths = Files.walk(targetDirectory)) {
            return paths.filter(filePath -> Files.isRegularFile(filePath) && filePath.toString().endsWith(".jar"))
                    .findFirst();
        }
    }
}
