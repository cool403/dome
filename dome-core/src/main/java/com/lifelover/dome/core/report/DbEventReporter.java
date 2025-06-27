package com.lifelover.dome.core.report;

@SuppressWarnings("rawtypes")
public class DbEventReporter extends AbstractEventReporter {
    @Override
    protected void handle(MetricsEvent metricsEvent) {
        //只保存http事件
        if (metricsEvent == null || !"HTTP".equals(metricsEvent.getEventType())) {
            return;
        }
        HttpMetricsData httpMetricsData =(HttpMetricsData) metricsEvent.getEventData();
    }
}
