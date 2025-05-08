package com.lifelover.dome.core.report;

import java.util.UUID;

public interface MetricsEvent {
    
    default String getEventId(){
        return UUID.randomUUID().toString();
    }

    /**
     * to json
     * @return
     */
    String jsonStr();
}
