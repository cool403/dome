package com.lifelover.dome.core.report;

import com.lifelover.dome.core.helpers.JsonUtil;

@SuppressWarnings("rawtypes")
public class ConsoleEventReporter extends AbstractEventReporter{

    @Override
    protected void handle(MetricsEvent metricsEvent) {
        System.out.println(JsonUtil.toJson(metricsEvent));
    }
    
}
