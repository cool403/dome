package com.lifelover.dome.core.report;

import com.lifelover.dome.core.config.ConfigLoader;

public class EventReporterHolder {

    private EventReporterHolder() {
    }

    /**
     * 事件报告器持有者
     */
    private static class __EventReporterHolder {
        private static final EventReporter INSTANCE1 = new HttpEventReporter();
        private static final EventReporter INSTANCE2 = new ConsoleEventReporter();
        private static final EventReporter INSTANCE3 = new DbEventReporter();
        private static final EventReporter INSTANCE4 = new ExceptionEventReporter();
    }

    /**
     * 
     * @return 事件报告器实例
     */
    public static EventReporter getEventReporter() {
        String reporterType = ConfigLoader.getAgentConfig().getReporterType();
        if (ReportType.CONSOLE.name().equals(reporterType)) {
            return __EventReporterHolder.INSTANCE2;
        }
        if (ReportType.DB.name().equals(reporterType)) {
            return __EventReporterHolder.INSTANCE3;
        }
        if (ReportType.HTTP.name().equals(reporterType)) {
            return __EventReporterHolder.INSTANCE1;
        }
        // 默认使用异常专用报告器
        return __EventReporterHolder.INSTANCE4;
    }
    
    /**
     * 获取异常专用报告器
     */
    public static EventReporter getExceptionReporter() {
        return __EventReporterHolder.INSTANCE4;
    }
}
