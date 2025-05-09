package com.lifelover.dome.core.report;

public class EventReporterHolder {
    private EventReporterHolder(){
    }

    /**
     * 事件报告器持有者
     */
    private static class __EventReporterHolder{
        // private static final EventReporter INSTANCE = new HttpEventReporter();
        private static final EventReporter INSTANCE = new ConsoleEventReporter();
    }

    /**
     * 
     * @return 事件报告器实例
     */
    public static EventReporter getEventReporter(){
        return __EventReporterHolder.INSTANCE;
    }
}
