package org.wso2.carbon.mss.example.hook;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

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
import org.wso2.carbon.mss.HttpResponder;
import org.wso2.carbon.mss.internal.router.HandlerInfo;
import org.wso2.carbon.mss.internal.router.Interceptor;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Collecting Metrics via annotations
 */
public class MetricsInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(MetricsInterceptor.class);

    private Map<Method, Set<Interceptor>> map = new ConcurrentHashMap<>();

    public MetricsInterceptor() {
    }

    @Override
    public boolean preCall(HttpRequest request, HttpResponder responder, HandlerInfo handlerInfo) {
        Method method = handlerInfo.getMethod();
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
                interceptor.preCall(request, responder, handlerInfo);
            }
        }

        return true;
    }

    @Override
    public void postCall(HttpRequest request, HttpResponseStatus status, HandlerInfo handlerInfo) {
        Method method = handlerInfo.getMethod();
        Set<Interceptor> interceptors = map.get(method);
        if (interceptors != null) {
            for (Interceptor interceptor : interceptors) {
                interceptor.postCall(request, status, handlerInfo);
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

        private final TimerContextThreadLocal timerContextThreadLocal;

        private TimerInterceptor(Timer timer) {
            this.timer = timer;
            this.timerContextThreadLocal = new TimerContextThreadLocal();
        }

        @Override
        public boolean preCall(HttpRequest request, HttpResponder responder, HandlerInfo handlerInfo) {
            Context context = timer.start();
            timerContextThreadLocal.set(context);
            return true;
        }

        @Override
        public void postCall(HttpRequest request, HttpResponseStatus status, HandlerInfo handlerInfo) {
            Context context = timerContextThreadLocal.get();
            context.stop();
        }
    }

    private static class TimerContextThreadLocal extends ThreadLocal<Context> {
    }

    private static class MeterInterceptor implements Interceptor {

        private final Meter meter;

        private MeterInterceptor(Meter meter) {
            this.meter = meter;
        }

        @Override
        public boolean preCall(HttpRequest request, HttpResponder responder, HandlerInfo handlerInfo) {
            meter.mark();
            return true;
        }

        @Override
        public void postCall(HttpRequest request, HttpResponseStatus status, HandlerInfo handlerInfo) {
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
        public boolean preCall(HttpRequest request, HttpResponder responder, HandlerInfo handlerInfo) {
            counter.inc();
            return true;
        }

        @Override
        public void postCall(HttpRequest request, HttpResponseStatus status, HandlerInfo handlerInfo) {
            if (!monotonic) {
                counter.dec();
            }
        }
    }
}
