package com.yammer.metrics.guice;

import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * A method interceptor which creates a timer for the declaring class with the given name (or the
 * method's name, if none was provided), and which times the execution of the annotated method.
 */
class TimedInterceptor implements MethodInterceptor {
    static MethodInterceptor forMethod(MetricsRegistry metricsRegistry, Class<?> klass, Method method) {
        final Timed annotation = method.getAnnotation(Timed.class);
        if (annotation != null) {
            final String name = annotation.name().isEmpty() ? method.getName() : annotation.name();
            final Timer timer = metricsRegistry.newTimer(klass,
                                                               name,
                                                               annotation.durationUnit(),
                                                               annotation.rateUnit());
            return new TimedInterceptor(timer);
        }
        return null;
    }


    private final Timer timer;

    private TimedInterceptor(Timer timer) {
        this.timer = timer;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final long startTime = System.nanoTime();
        try {
            return invocation.proceed();
        } finally {
            timer.update(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
        }
    }
}
