/*
 * Copyright © 2014 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.http;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.Service;
import org.junit.Assert;
import org.junit.BeforeClass;

import java.io.File;
import java.net.URI;
import java.util.List;

/**
 * Test the HttpsServer with mutual authentication.
 */
public class MutualAuthServerTest extends HttpsServerTest {

  @BeforeClass
  public static void setup() throws Exception {
    List<HttpHandler> handlers = Lists.newArrayList();
    handlers.add(new TestHandler());

    NettyHttpService.Builder builder = createBaseNettyHttpServiceBuilder();

    File keyStore = tmpFolder.newFile();
    ByteStreams.copy(Resources.newInputStreamSupplier(Resources.getResource("cert.jks")),
                     Files.newOutputStreamSupplier(keyStore));
    File trustKeyStore = tmpFolder.newFile();
    ByteStreams.copy(Resources.newInputStreamSupplier(Resources.getResource("client.jks")),
                     Files.newOutputStreamSupplier(trustKeyStore));

    String keyStorePassword = "secret";
    String trustKeyStorePassword = "password";
    builder.enableSSL(SSLConfig.builder(keyStore, keyStorePassword).setTrustKeyStore(trustKeyStore)
                        .setTrustKeyStorePassword(trustKeyStorePassword)
                        .build());

    setSslClientContext(new SSLClientContext(trustKeyStore, trustKeyStorePassword));
    service = builder.build();
    service.startAndWait();
    Service.State state = service.state();
    Assert.assertEquals(Service.State.RUNNING, state);

    int port = service.getBindAddress().getPort();
    baseURI = URI.create(String.format("https://localhost:%d", port));
  }
}
