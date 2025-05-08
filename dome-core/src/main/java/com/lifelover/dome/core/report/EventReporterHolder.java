package com.lifelover.dome.core.report;

public class EventReporterHolder {
    private EventReporterHolder(){
    }

    /**
     * 
     */
    private static class __EventReporterHolder{
        // private static final EventReporter INSTANCE = new HttpEventReporter();
        private static final EventReporter INSTANCE = new ConsoleEventReporter();
    }

    /**
     * 
     * @return
     */
    public static EventReporter getEventReporter(){
        return __EventReporterHolder.INSTANCE;
    }
}
