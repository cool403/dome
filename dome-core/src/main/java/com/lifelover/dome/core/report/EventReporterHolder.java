package com.lifelover.dome.core.report;

public class EventReporterHolder {
    private EventReporterHolder(){
    }

    /**
     * 
     */
    private static class __EventReporterHolder{
        private static final EventReporter INSTANCE = new HttpEventReporter();
    }

    /**
     * 
     * @return
     */
    public static EventReporter getEventReporter(){
        return __EventReporterHolder.INSTANCE;
    }
}
