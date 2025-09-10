package com.lifelover.dome.core.report;

import com.lifelover.dome.core.plugins.exception.ExceptionEvent;

@SuppressWarnings("rawtypes")
public class ExceptionEventReporter extends AbstractEventReporter {

    @Override
    protected void handle(MetricsEvent metricsEvent) {
        if (metricsEvent.getEventData() instanceof ExceptionEvent) {
            ExceptionEvent exceptionEvent = (ExceptionEvent) metricsEvent.getEventData();
            
            // 格式化异常信息输出
            System.err.println("=== 方法调用异常报告 ===");
            System.err.println("时间: " + new java.util.Date(exceptionEvent.getTimestamp()));
            System.err.println("线程: " + exceptionEvent.getThreadName());
            System.err.println("方法: " + exceptionEvent.getMethodName());
            System.err.println("参数: " + java.util.Arrays.toString(exceptionEvent.getArguments()));
            System.err.println("异常: " + exceptionEvent.getException().getClass().getName());
            System.err.println("异常信息: " + exceptionEvent.getException().getMessage());
            System.err.println("堆栈跟踪:");
            exceptionEvent.getException().printStackTrace();
            System.err.println("========================");
        }
    }
}