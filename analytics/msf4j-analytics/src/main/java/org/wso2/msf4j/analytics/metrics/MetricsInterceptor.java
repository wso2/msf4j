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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.metrics.core.Counter;
import org.wso2.carbon.metrics.core.Meter;
import org.wso2.carbon.metrics.core.MetricAnnotation;
import org.wso2.carbon.metrics.core.Timer;
import org.wso2.carbon.metrics.core.annotation.Counted;
import org.wso2.carbon.metrics.core.annotation.Metered;
import org.wso2.carbon.metrics.core.annotation.Timed;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Response;
import org.wso2.msf4j.interceptor.MSF4JRequestInterceptor;
import org.wso2.msf4j.interceptor.MSF4JResponseInterceptor;
import org.wso2.msf4j.internal.MSF4JConstants;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Collecting Metrics via annotations.
 */
public class MetricsInterceptor implements MSF4JRequestInterceptor, MSF4JResponseInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(MetricsInterceptor.class);

    private Map<Method, MethodInterceptors> map = new ConcurrentHashMap<>();

    public MetricsInterceptor() {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating Metrics Interceptor");
        }
    }

    private Timed getTimedAnnotation(Method method) {
        Timed annotation = method.getAnnotation(Timed.class);
        if (annotation == null) {
            annotation = method.getDeclaringClass().getAnnotation(Timed.class);
        }
        return annotation;
    }

    private Metered getMeteredAnnotation(Method method) {
        Metered annotation = method.getAnnotation(Metered.class);
        if (annotation == null) {
            annotation = method.getDeclaringClass().getAnnotation(Metered.class);
        }
        return annotation;
    }

    private Counted getCountedAnnotation(Method method) {
        Counted annotation = method.getAnnotation(Counted.class);
        if (annotation == null) {
            annotation = method.getDeclaringClass().getAnnotation(Counted.class);
        }
        return annotation;
    }

    @Override
    public boolean interceptRequest(Request request, Response response) throws Exception {
        Method method = (Method) request.getProperty(MSF4JConstants.METHOD_PROPERTY_NAME);
        MethodInterceptors methodInterceptors = map.get(method);
        if (methodInterceptors == null || !methodInterceptors.annotationScanned) {
            List<MSF4JRequestInterceptor> msf4JRequestInterceptors = new CopyOnWriteArrayList<>();
            List<MSF4JResponseInterceptor> msf4JResponseInterceptors = new CopyOnWriteArrayList<>();
            Timed timed = getTimedAnnotation(method);
            if (timed != null) {
                Timer timer = MetricAnnotation.timer(Metrics.getInstance().getMetricService(), timed, method);
                TimerInterceptor timerInterceptor = new TimerInterceptor(timer);
                msf4JRequestInterceptors.add(timerInterceptor);
                msf4JResponseInterceptors.add(timerInterceptor);
            }
            Metered metered = getMeteredAnnotation(method);
            if (metered != null) {
                Meter meter = MetricAnnotation.meter(Metrics.getInstance().getMetricService(), metered, method);
                MSF4JRequestInterceptor msf4JRequestInterceptor = new MeterInterceptor(meter);
                msf4JRequestInterceptors.add(msf4JRequestInterceptor);
            }
            Counted counted = getCountedAnnotation(method);
            if (counted != null) {
                Counter counter = MetricAnnotation.counter(Metrics.getInstance().getMetricService(), counted, method);
                CounterInterceptor counterInterceptor = new CounterInterceptor(counter, counted.monotonic());
                msf4JRequestInterceptors.add(counterInterceptor);
                msf4JResponseInterceptors.add(counterInterceptor);
            }

            methodInterceptors = new MethodInterceptors(true, msf4JRequestInterceptors, msf4JResponseInterceptors);
            map.put(method, methodInterceptors);
        }

        return methodInterceptors.interceptRequest(request, response);
    }

    @Override
    public boolean interceptResponse(Request request, Response response) throws Exception {
        Method method = (Method) request.getProperty(MSF4JConstants.METHOD_PROPERTY_NAME);
        MethodInterceptors methodInterceptors = map.get(method);
        return !(methodInterceptors != null && !methodInterceptors.interceptResponse(request, response));
    }

    private static class MethodInterceptors implements MSF4JRequestInterceptor, MSF4JResponseInterceptor {

        private final boolean annotationScanned;
        private MSF4JRequestInterceptor[] msf4JRequestInterceptors;
        private MSF4JResponseInterceptor[] msf4JResponseInterceptors;

        MethodInterceptors(boolean annotationScanned, List<MSF4JRequestInterceptor> requestInterceptors,
                           List<MSF4JResponseInterceptor> responseInterceptors) {
            this.annotationScanned = annotationScanned;
            if (!requestInterceptors.isEmpty()) {
                this.msf4JRequestInterceptors =
                        requestInterceptors.toArray(new MSF4JRequestInterceptor[requestInterceptors.size()]);
            }

            if (!responseInterceptors.isEmpty()) {
                this.msf4JResponseInterceptors =
                        responseInterceptors.toArray(new MSF4JResponseInterceptor[responseInterceptors.size()]);
            }
        }

        @Override
        public boolean interceptRequest(Request request, Response response) throws Exception {
            if (msf4JRequestInterceptors != null) {
                for (MSF4JRequestInterceptor interceptor : msf4JRequestInterceptors) {
                    interceptor.interceptRequest(request, response);
                }
            }
            return true;
        }

        @Override
        public boolean interceptResponse(Request request, Response response) throws Exception {
            if (msf4JResponseInterceptors != null) {
                for (MSF4JResponseInterceptor interceptor : msf4JResponseInterceptors) {
                    if (!interceptor.interceptResponse(request, response)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    private static class TimerInterceptor implements MSF4JRequestInterceptor, MSF4JResponseInterceptor {

        private final Timer timer;

        private static final String TIMER_CONTEXT = "TIMER_CONTEXT";

        private TimerInterceptor(Timer timer) {
            this.timer = timer;
        }

        @Override
        public boolean interceptRequest(Request request, Response response) throws Exception {
            Timer.Context context = timer.start();
            request.setProperty(TIMER_CONTEXT, context);
            return true;
        }

        @Override
        public boolean interceptResponse(Request request, Response response) throws Exception {
            Timer.Context context = (Timer.Context) request.getProperty(TIMER_CONTEXT);
            context.stop();
            return true;
        }
    }

    private static class MeterInterceptor implements MSF4JRequestInterceptor {

        private final Meter meter;

        private MeterInterceptor(Meter meter) {
            this.meter = meter;
        }

        @Override
        public boolean interceptRequest(Request request, Response response) throws Exception {
            meter.mark();
            return true;
        }
    }

    private static class CounterInterceptor implements MSF4JRequestInterceptor, MSF4JResponseInterceptor {

        private final Counter counter;
        private final boolean monotonic;

        private CounterInterceptor(Counter counter, boolean monotonic) {
            this.counter = counter;
            this.monotonic = monotonic;
        }

        @Override
        public boolean interceptRequest(Request request, Response response) throws Exception {
            counter.inc();
            return true;
        }

        @Override
        public boolean interceptResponse(Request request, Response response) throws Exception {
            if (!monotonic) {
                counter.dec();
            }
            return true;
        }
    }
}
