package com.lifelover.dome.core.report;

import java.io.IOException;

import com.lifelover.dome.core.config.ConfigLoader;

public class HttpEventReporter extends AbstractEventReporter{

    private static final int REQUEST_TIMEOUT_MS = 150;



    @Override
    protected void handle(MetricsEvent metricsEvent) {
        String reportUrl = ConfigLoader.getAgentConfig().getCollectorAddr();
        if (reportUrl == null || reportUrl.isBlank()) {
            System.out.println("收集器地址为空");
            return;
        }
        try {
            HttpUtil.post(reportUrl, metricsEvent.jsonStr(), REQUEST_TIMEOUT_MS);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    
}
