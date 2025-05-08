package com.lifelover.dome.core.report;

import java.util.List;

public class LocalQueueAsyncEventReporter implements EventReporter{

    @Override
    public void report(List<MetricsEvent> lst) {
        if (lst == null || lst.isEmpty()) {
            return;
        }
        
    }
    
}
