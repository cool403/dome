package com.lifelover.dome.core.plugins.exception;

import com.lifelover.dome.core.report.EventReporter;
import com.lifelover.dome.core.report.EventReporterHolder;
import com.lifelover.dome.core.report.MetricsEvent;
import com.lifelover.dome.core.report.ReportType;
import net.bytebuddy.asm.Advice;

public class ExceptionAdvice {
    public static final ThreadLocal<MethodContext> CONTEXT = new ThreadLocal<>();

    @Advice.OnMethodEnter
    public static void onEnter(@Advice.Origin String method,
            @Advice.AllArguments Object[] args) {
        CONTEXT.remove();
        // 保存方法入口信息到线程上下文
        MethodContext context = new MethodContext(method, args);
        CONTEXT.set(context);
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(@Advice.Thrown Throwable throwable) {
        if (throwable == null) {
            return;
        }
        MethodContext context = CONTEXT.get();
        if (context == null) {
            return;
        }
        System.out.print("222222222222222222222222222");
        ExceptionEvent event = new ExceptionEvent(
                context.getMethodName(),
                context.getArguments(),
                throwable,
                Thread.currentThread().getName(),
                System.currentTimeMillis());
        EventReporter reporter = EventReporterHolder.getExceptionReporter();
        if (reporter != null) {
            final MetricsEvent<ExceptionEvent> metricsEvent = new MetricsEvent<>();
            metricsEvent.setEventData(event);
            metricsEvent.setEventType(ReportType.EX.name());
            reporter.asyncReport(metricsEvent);
        }
        CONTEXT.remove();
    }

}