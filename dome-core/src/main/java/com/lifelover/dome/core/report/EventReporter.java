package com.lifelover.dome.core.report;

import java.util.List;

public interface EventReporter {
    
    /**
     * 上报采集到的事件
     * @param lst
     */
    void report(List<MetricsEvent> lst);
}
