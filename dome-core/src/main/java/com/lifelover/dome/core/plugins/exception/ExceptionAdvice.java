package com.lifelover.dome.core.plugins.exception;

import com.lifelover.dome.core.report.EventReporter;
import com.lifelover.dome.core.report.EventReporterHolder;
import com.lifelover.dome.core.report.MetricsEvent;
import com.lifelover.dome.core.report.ReportType;
import net.bytebuddy.asm.Advice;

public class ExceptionAdvice {
    
    @Advice.OnMethodEnter
    public static void onEnter(@Advice.Origin String method,
                             @Advice.AllArguments Object[] args) {
        // 保存方法入口信息到线程上下文
        MethodContext context = new MethodContext(method, args);
        ThreadLocalContext.set(context);
    }
    
    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onExit(@Advice.Thrown Throwable throwable) {
        if (throwable != null) {
            MethodContext context = ThreadLocalContext.get();
            if (context != null) {
                ExceptionEvent event = new ExceptionEvent(
                    context.getMethodName(),
                    context.getArguments(),
                    throwable,
                    Thread.currentThread().getName(),
                    System.currentTimeMillis()
                );
                
                EventReporter reporter = EventReporterHolder.getExceptionReporter();
                if (reporter != null) {
                    final MetricsEvent<ExceptionEvent> metricsEvent = new MetricsEvent<>();
                    metricsEvent.setEventData(event);
                    metricsEvent.setEventType(ReportType.EX.name());
                    reporter.asyncReport(metricsEvent);
                }
            }
        }
        ThreadLocalContext.clear();
    }
    
    private static class MethodContext {
        private final String methodName;
        private final Object[] arguments;
        
        public MethodContext(String methodName, Object[] arguments) {
            this.methodName = methodName;
            this.arguments = arguments != null ? arguments.clone() : null;
        }
        
        public String getMethodName() {
            return methodName;
        }
        
        public Object[] getArguments() {
            return arguments;
        }
    }
    
    private static class ThreadLocalContext {
        private static final ThreadLocal<MethodContext> CONTEXT = new ThreadLocal<>();
        
        public static void set(MethodContext context) {
            CONTEXT.set(context);
        }
        
        public static MethodContext get() {
            return CONTEXT.get();
        }
        
        public static void clear() {
            CONTEXT.remove();
        }
    }
}