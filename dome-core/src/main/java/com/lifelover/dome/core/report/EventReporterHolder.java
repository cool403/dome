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
    }

    /**
     * 
     * @return 事件报告器实例
     */
    public static EventReporter getEventReporter() {
        String reporterType = ConfigLoader.getAgentConfig().getReporterType();
        if ("CONSOLE".equals(reporterType)) {
            return __EventReporterHolder.INSTANCE2;
        }
        return __EventReporterHolder.INSTANCE1;
    }
}
