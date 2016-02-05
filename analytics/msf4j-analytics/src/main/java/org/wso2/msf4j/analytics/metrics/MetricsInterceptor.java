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
package org.wso2.msf4j.analytics.metrics;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.annotation.Counted;
import org.wso2.carbon.metrics.annotation.Level;
import org.wso2.carbon.metrics.annotation.Metered;
import org.wso2.carbon.metrics.annotation.Timed;
import org.wso2.carbon.metrics.manager.Counter;
import org.wso2.carbon.metrics.manager.Meter;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.Timer;
import org.wso2.carbon.metrics.manager.Timer.Context;
import org.wso2.msf4j.HttpResponder;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.ServiceMethodInfo;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collecting Metrics via annotations.
 */
@Component(
    name = "org.wso2.msf4j.analytics.metrics.MetricsInterceptor",
    service = Interceptor.class,
    immediate = true)
public class MetricsInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(MetricsInterceptor.class);

    private Map<Method, Set<Interceptor>> map = new ConcurrentHashMap<>();

    public MetricsInterceptor() {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating Metrics Interceptor");
        }
    }

    /**
     * Initialize the Metrics Service.
     * 
     * @param metricReporters Specifiy {@link MetricReporter} types to initialize
     * @return This {@link MetricsInterceptor} instance
     */
    public MetricsInterceptor init(MetricReporter... metricReporters) {
        Metrics.init(metricReporters);
        // Destroy the Metric Service at shutdown
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                Metrics.destroy();
            }
        });
        return this;
    }

    @Override
    public boolean preCall(HttpRequest request, HttpResponder responder, ServiceMethodInfo serviceMethodInfo) {
        Method method = serviceMethodInfo.getMethod();
        Set<Interceptor> interceptors = map.get(method);
        if (interceptors == null) {
            if (method.isAnnotationPresent(Timed.class)) {
                Timed annotation = method.getAnnotation(Timed.class);
                String name = buildName(annotation.name(), annotation.absolute(), method);
                Level level = annotation.level();
                Timer timer = MetricManager.timer(toLevel(level), name);
                Interceptor interceptor = new TimerInterceptor(timer);
                interceptors = new HashSet<>();
                interceptors.add(interceptor);
            }

            if (method.isAnnotationPresent(Metered.class)) {
                Metered annotation = method.getAnnotation(Metered.class);
                String name = buildName(annotation.name(), annotation.absolute(), method);
                Level level = annotation.level();
                Meter meter = MetricManager.meter(toLevel(level), name);
                Interceptor interceptor = new MeterInterceptor(meter);
                if (interceptors == null) {
                    interceptors = new HashSet<>();
                }
                interceptors.add(interceptor);
            }

            if (method.isAnnotationPresent(Counted.class)) {
                Counted annotation = method.getAnnotation(Counted.class);
                String name = buildName(annotation.name(), annotation.absolute(), method);
                Level level = annotation.level();
                Counter counter = MetricManager.counter(toLevel(level), name);
                Interceptor interceptor = new CounterInterceptor(counter, annotation.monotonic());
                if (interceptors == null) {
                    interceptors = new HashSet<>();
                }
                interceptors.add(interceptor);
            }

            if (interceptors != null && !interceptors.isEmpty()) {
                map.put(method, interceptors);
            }
        }

        if (interceptors != null) {
            for (Interceptor interceptor : interceptors) {
                interceptor.preCall(request, responder, serviceMethodInfo);
            }
        }

        return true;
    }

    @Override
    public void postCall(HttpRequest request, HttpResponseStatus status, ServiceMethodInfo serviceMethodInfo) {
        Method method = serviceMethodInfo.getMethod();
        Set<Interceptor> interceptors = map.get(method);
        if (interceptors != null) {
            for (Interceptor interceptor : interceptors) {
                interceptor.postCall(request, status, serviceMethodInfo);
            }
        }
    }

    private String buildName(String explicitName, boolean absolute, Method method) {
        if (explicitName != null && !explicitName.isEmpty()) {
            if (absolute) {
                return explicitName;
            }
            return MetricManager.name(method.getDeclaringClass().getName(), method.getName(), explicitName);
        }
        return MetricManager.name(method.getDeclaringClass().getName(), method.getName());
    }

    private org.wso2.carbon.metrics.manager.Level toLevel(Level level) {
        switch (level) {
        case OFF:
            return org.wso2.carbon.metrics.manager.Level.OFF;
        case INFO:
            return org.wso2.carbon.metrics.manager.Level.INFO;
        case DEBUG:
            return org.wso2.carbon.metrics.manager.Level.DEBUG;
        case TRACE:
            return org.wso2.carbon.metrics.manager.Level.TRACE;
        case ALL:
            return org.wso2.carbon.metrics.manager.Level.ALL;
        }
        return org.wso2.carbon.metrics.manager.Level.INFO;
    }

    private static class TimerInterceptor implements Interceptor {

        private final Timer timer;

        private static final String TIMER_CONTEXT = "TIMER_CONTEXT";

        private TimerInterceptor(Timer timer) {
            this.timer = timer;
        }

        @Override
        public boolean preCall(HttpRequest request, HttpResponder responder, ServiceMethodInfo serviceMethodInfo) {
            Context context = timer.start();
            serviceMethodInfo.setAttribute(TIMER_CONTEXT, context);
            return true;
        }

        @Override
        public void postCall(HttpRequest request, HttpResponseStatus status, ServiceMethodInfo serviceMethodInfo) {
            Context context = (Context) serviceMethodInfo.getAttribute(TIMER_CONTEXT);
            context.stop();
        }
    }

    private static class MeterInterceptor implements Interceptor {

        private final Meter meter;

        private MeterInterceptor(Meter meter) {
            this.meter = meter;
        }

        @Override
        public boolean preCall(HttpRequest request, HttpResponder responder, ServiceMethodInfo serviceMethodInfo) {
            meter.mark();
            return true;
        }

        @Override
        public void postCall(HttpRequest request, HttpResponseStatus status, ServiceMethodInfo serviceMethodInfo) {
        }
    }

    private static class CounterInterceptor implements Interceptor {

        private final Counter counter;
        private final boolean monotonic;

        private CounterInterceptor(Counter counter, boolean monotonic) {
            this.counter = counter;
            this.monotonic = monotonic;
        }

        @Override
        public boolean preCall(HttpRequest request, HttpResponder responder, ServiceMethodInfo serviceMethodInfo) {
            counter.inc();
            return true;
        }

        @Override
        public void postCall(HttpRequest request, HttpResponseStatus status, ServiceMethodInfo serviceMethodInfo) {
            if (!monotonic) {
                counter.dec();
            }
        }
    }
}
