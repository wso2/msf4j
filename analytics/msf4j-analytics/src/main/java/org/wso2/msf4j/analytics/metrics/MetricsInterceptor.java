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

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.Counter;
import org.wso2.carbon.metrics.core.Meter;
import org.wso2.carbon.metrics.core.MetricAnnotation;
import org.wso2.carbon.metrics.core.Timer;
import org.wso2.carbon.metrics.core.annotation.Counted;
import org.wso2.carbon.metrics.core.annotation.Metered;
import org.wso2.carbon.metrics.core.annotation.Timed;
import org.wso2.msf4j.Interceptor;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.ServiceMethodInfo;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Collecting Metrics via annotations.
 */
@Component(
        name = "org.wso2.msf4j.analytics.metrics.MetricsInterceptor",
        service = Interceptor.class
)
public class MetricsInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(MetricsInterceptor.class);

    private Map<Method, MethodInterceptors> map = new ConcurrentHashMap<>();

    public MetricsInterceptor() {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating Metrics Interceptor");
        }
    }

    @Override
    public boolean preCall(Request request, Response responder, ServiceMethodInfo serviceMethodInfo) throws Exception {
        Method method = serviceMethodInfo.getMethod();
        MethodInterceptors methodInterceptors = map.get(method);
        if (methodInterceptors == null || !methodInterceptors.isAnnotationScanned()) {
            List<Interceptor> interceptors = new CopyOnWriteArrayList<>();
            if (method.isAnnotationPresent(Timed.class)) {
                Timed annotation = method.getAnnotation(Timed.class);
                Timer timer = MetricAnnotation.timer(Metrics.getInstance().getMetricService(), annotation, method);
                Interceptor interceptor = new TimerInterceptor(timer);
                interceptors.add(interceptor);
            }

            if (method.isAnnotationPresent(Metered.class)) {
                Metered annotation = method.getAnnotation(Metered.class);
                Meter meter = MetricAnnotation.meter(Metrics.getInstance().getMetricService(), annotation, method);
                Interceptor interceptor = new MeterInterceptor(meter);
                interceptors.add(interceptor);
            }

            if (method.isAnnotationPresent(Counted.class)) {
                Counted annotation = method.getAnnotation(Counted.class);
                Counter counter = MetricAnnotation.counter(Metrics.getInstance().getMetricService(), annotation,
                        method);
                Interceptor interceptor = new CounterInterceptor(counter, annotation.monotonic());
                interceptors.add(interceptor);
            }

            methodInterceptors = new MethodInterceptors(true, interceptors);
            map.put(method, methodInterceptors);
        }

        return methodInterceptors.preCall(request, responder, serviceMethodInfo);
    }

    @Override
    public void postCall(Request request, int status, ServiceMethodInfo serviceMethodInfo) throws Exception {
        Method method = serviceMethodInfo.getMethod();
        MethodInterceptors methodInterceptors = map.get(method);
        if (methodInterceptors != null) {
            methodInterceptors.postCall(request, status, serviceMethodInfo);
        }
    }

    private static class MethodInterceptors implements Interceptor {

        private final boolean annotationScanned;

        private Interceptor[] interceptors;

        MethodInterceptors(boolean annotationScanned, List<Interceptor> interceptors) {
            this.annotationScanned = annotationScanned;
            if (!interceptors.isEmpty()) {
                this.interceptors = interceptors.toArray(new Interceptor[interceptors.size()]);
            }
        }

        private boolean isAnnotationScanned() {
            return annotationScanned;
        }

        @Override
        public boolean preCall(Request request, Response responder, ServiceMethodInfo serviceMethodInfo)
                throws Exception {
            if (interceptors != null) {
                for (Interceptor interceptor : interceptors) {
                    interceptor.preCall(request, responder, serviceMethodInfo);
                }
            }
            return true;
        }

        @Override
        public void postCall(Request request, int status, ServiceMethodInfo serviceMethodInfo) throws Exception {
            if (interceptors != null) {
                for (Interceptor interceptor : interceptors) {
                    interceptor.postCall(request, status, serviceMethodInfo);
                }
            }
        }
    }

    private static class TimerInterceptor implements Interceptor {

        private final Timer timer;

        private static final String TIMER_CONTEXT = "TIMER_CONTEXT";

        private TimerInterceptor(Timer timer) {
            this.timer = timer;
        }

        @Override
        public boolean preCall(Request request, Response responder, ServiceMethodInfo serviceMethodInfo) {
            Timer.Context context = timer.start();
            serviceMethodInfo.setAttribute(TIMER_CONTEXT, context);
            return true;
        }

        @Override
        public void postCall(Request request, int status, ServiceMethodInfo serviceMethodInfo) {
            Timer.Context context = (Timer.Context) serviceMethodInfo.getAttribute(TIMER_CONTEXT);
            context.stop();
        }
    }

    private static class MeterInterceptor implements Interceptor {

        private final Meter meter;

        private MeterInterceptor(Meter meter) {
            this.meter = meter;
        }

        @Override
        public boolean preCall(Request request, Response responder, ServiceMethodInfo serviceMethodInfo) {
            meter.mark();
            return true;
        }

        @Override
        public void postCall(Request request, int status, ServiceMethodInfo serviceMethodInfo) {
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
        public boolean preCall(Request request, Response responder, ServiceMethodInfo serviceMethodInfo) {
            counter.inc();
            return true;
        }

        @Override
        public void postCall(Request request, int status, ServiceMethodInfo serviceMethodInfo) {
            if (!monotonic) {
                counter.dec();
            }
        }
    }
}
