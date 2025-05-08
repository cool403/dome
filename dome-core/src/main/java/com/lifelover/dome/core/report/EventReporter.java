package com.lifelover.dome.core.report;

import java.util.List;

public interface EventReporter {
    
    /**
     * 上报采集到的事件
     * @param lst
     */
    void asyncReport(List<MetricsEvent> lst);


    /**
     * 
     * @param metricsEvent
     */
    void asyncReport(MetricsEvent metricsEvent);
}
