/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.mss.internal;

import co.cask.http.HandlerHook;
import co.cask.http.HttpHandler;
import co.cask.http.HttpResourceHandler;
import co.cask.http.RequestRouter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.transport.http.netty.listener.CarbonNettyServerInitializer;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component(
        name = "org.wso2.carbon.mss.internal.MicroServicesServerSC",
        immediate = true
)
@SuppressWarnings("unused")
public class MicroServicesServerSC {
    private static final Logger LOG = LoggerFactory.getLogger(MicroServicesServerSC.class);
    public static final String CHANNEL_ID_KEY = "channel.id";

    private final DataHolder dataHolder = DataHolder.getInstance();

    private BundleContext bundleContext;
    private int jaxRsServiceCount;

    @Activate
    protected void start(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        try {
            countJaxrsServices();

            new Thread(new Runnable() {
//                private List<NettyHttpService> nettyHttpServices = new ArrayList<NettyHttpService>();
//                private List<NettyHttpService.Builder> builders = new ArrayList<NettyHttpService.Builder>();

                public void run() {
                    while (true) {
                        if (dataHolder.getHttpServices().size() == jaxRsServiceCount) {
                            LOG.info("Starting micro services server...");

                            // Create an OSGi services (HTTP/HTTPS) & register it with the relevant CHANNEL_ID_KEY

                            Hashtable<String, String> httpInitParams = new Hashtable<>();
                            httpInitParams.put(CHANNEL_ID_KEY, "netty-jaxrs-http");
                            bundleContext.registerService(CarbonNettyServerInitializer.class,
                                    new JaxrsCarbonNettyInitializer(), httpInitParams);

                            Hashtable<String, String> httpsInitParams = new Hashtable<>();
                            httpsInitParams.put(CHANNEL_ID_KEY, "netty-jaxrs-https");
                            bundleContext.registerService(CarbonNettyServerInitializer.class,
                                    new JaxrsCarbonNettyInitializer(), httpsInitParams);

                            LOG.info("Micro services server started");
                            break;
                        } else {
                            try {
                                TimeUnit.MILLISECONDS.sleep(10);
                            } catch (InterruptedException ignored) {
                            }
                        }
                    }
                }
            }).start();
        } catch (Throwable e) {
            LOG.error("Could not start MicroServicesServerSC", e);
        }
    }

    private class JaxrsCarbonNettyInitializer implements CarbonNettyServerInitializer {

        private DefaultEventExecutorGroup eventExecutorGroup;

        @Override
        public void setup(Map<String, String> map) {
            eventExecutorGroup = new DefaultEventExecutorGroup(200);
        }

        public void initChannel(SocketChannel channel) {
            ChannelPipeline pipeline = channel.pipeline();
            //        pipeline.addLast("tracker", connectionTracker);
            pipeline.addLast("decoder", new HttpRequestDecoder());
            pipeline.addLast("aggregator", new HttpObjectAggregator(Integer.MAX_VALUE));  //TODO: fix
            pipeline.addLast("encoder", new HttpResponseEncoder());
            pipeline.addLast("compressor", new HttpContentCompressor());

            HttpResourceHandler resourceHandler = new HttpResourceHandler(dataHolder.getHttpServices(),
                    new ArrayList<HandlerHook>(), null, null);
            pipeline.addLast(eventExecutorGroup, "router", new RequestRouter(resourceHandler, 0)); //TODO: remove limit

            //TODO: see what can be done
            /*if (pipelineModifier != null) {
                pipelineModifier.apply(pipeline);
            }*/
        }
    }

    private void countJaxrsServices() {
        Bundle[] bundles = bundleContext.getBundles();
        for (Bundle bundle : bundles) {
            String jaxRsServices = bundle.getHeaders().get("JAXRS-Services");
            if (jaxRsServices != null) {
                jaxRsServiceCount += Integer.parseInt(jaxRsServices);
            }
        }
    }

    @Reference(
            name = "netty-http.handler",
            service = HttpHandler.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "removeHttpService"
    )
    protected void addHttpService(HttpHandler httpService) {
        try {
            dataHolder.addHttpService(httpService);
            /*if (nettyHttpService != null && nettyHttpService.isRunning()) {
                nettyHttpService.addHttpHandler(httpService);
            }*/   // FIXME
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    protected void removeHttpService(HttpHandler httpService) {
        dataHolder.removeHttpService(httpService);
        //TODO: handle removing HttpService from NettyHttpService
    }
}
