package com.lifelover.dome.core.report;

public class ConsoleEventReporter extends AbstractEventReporter{

    @Override
    protected void handle(MetricsEvent metricsEvent) {
        System.out.println(metricsEvent.jsonStr());
    }
    
}
