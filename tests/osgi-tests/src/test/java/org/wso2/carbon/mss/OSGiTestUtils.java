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
package org.wso2.carbon.mss;

import org.ops4j.pax.exam.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.Constants;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.repositories;

/**
 * This class contains Utility methods to configure PAX-EXAM container.
 *
 * @since 5.0.0
 */
public class OSGiTestUtils {
    private static final Logger log = LoggerFactory.getLogger(OSGiTestUtils.class);

    /**
     * Setup the test environment.
     * <p>
     * Includes setting the carbon.home system property, setting the required system properties,
     * setting the maven local repo directory, etc.
     */
    public static void setupOSGiTestEnvironment() {
        setCarbonHome();
        setRequiredSystemProperties();
        setupMavenLocalRepo();
        setStartupTime();
    }

    /**
     * Returns an array of default PAX-EXAM options.
     *
     * @return array of Options
     */
    public static Option[] getDefaultPaxOptions() {
        return options(
                repositories("http://maven.wso2.org/nexus/content/groups/wso2-public"),

                //must install the testng bundle
                mavenBundle().artifactId("testng").groupId("org.testng").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.osgi.services").groupId("org.wso2.eclipse.osgi").
                        versionAsInProject(),
                mavenBundle().artifactId("pax-logging-api").groupId("org.ops4j.pax.logging").
                        versionAsInProject(),
                mavenBundle().artifactId("pax-logging-log4j2").groupId("org.ops4j.pax.logging").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.simpleconfigurator").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("org.apache.felix.gogo.command").groupId("org.apache.felix").
                        versionAsInProject(),
                mavenBundle().artifactId("org.apache.felix.gogo.runtime").groupId("org.apache.felix").
                        versionAsInProject(),
                mavenBundle().artifactId("org.apache.felix.gogo.shell").groupId("org.apache.felix").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.app").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.common").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.concurrent").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.console").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.ds").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.frameworkadmin").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.frameworkadmin.equinox").
                        groupId("org.wso2.eclipse.equinox").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.launcher").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.preferences").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.registry").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.simpleconfigurator.manipulator").
                        groupId("org.wso2.eclipse.equinox").versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.util").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("org.eclipse.equinox.cm").groupId("org.wso2.eclipse.equinox").
                        versionAsInProject(),
                mavenBundle().artifactId("snakeyaml").groupId("org.wso2.orbit.org.yaml").
                        versionAsInProject(),
                mavenBundle().artifactId("org.wso2.carbon.core").groupId("org.wso2.carbon").versionAsInProject()
        );
    }

    /**
     * Returns a merged array of user specified options and default options.
     *
     * @param options custom options.
     * @return a merged array.
     */
    public static Option[] getDefaultPaxOptions(Option[] options) {
        return Stream.concat(Arrays.stream(getDefaultPaxOptions()), Arrays.stream(options))
                .toArray(Option[]::new);
    }

    /**
     * setting the maven local repo system property, important when running in jenkins.
     */
    private static void setupMavenLocalRepo() {
        String localRepo = System.getProperty("maven.repo.local");
        if (localRepo != null && !localRepo.equals("")) {
            System.setProperty("org.ops4j.pax.url.mvn.localRepository", localRepo);
        }
    }

    /**
     * Set the carbon home for executing tests.
     * Carbon home is set to /carbon-kernel/tests/osgi-tests/target/carbon-home
     */
    private static void setCarbonHome() {
        String currentDir = Paths.get("").toAbsolutePath().toString();
        Path carbonHome = Paths.get(currentDir, "target", "carbon-home");
        System.setProperty("carbon.home", carbonHome.toString());
    }

    private static void setRequiredSystemProperties() {
        System.setProperty("server.key", "wso2-mss");
        System.setProperty("server.name", "WSO2 MSS");
        System.setProperty("server.version", "1.0.0");
    }

    /**
     * Set the startup time to calculate the server startup time.
     */
    private static void setStartupTime() {
        if (System.getProperty(Constants.START_TIME) == null) {
            System.setProperty(Constants.START_TIME, System.currentTimeMillis() + "");
        }
    }
}

