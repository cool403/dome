package com.lifelover.dome.core.report;

import java.io.IOException;

import com.lifelover.dome.core.config.ConfigLoader;
import com.lifelover.dome.core.helpers.JsonUtil;

@SuppressWarnings("rawtypes")
public class HttpEventReporter extends AbstractEventReporter{

    private static final int REQUEST_TIMEOUT_MS = 150;



    @Override
    protected void handle(MetricsEvent metricsEvent) {
        String reportUrl = ConfigLoader.getAgentConfig().getCollectorUrl();
        // String reportUrl = "http://127.0.0.1:5000/items";
        if (reportUrl == null || reportUrl.isEmpty()) {
            System.out.println("[dome agent] 收集器地址为空");
            return;
        }
        try {
            HttpUtil.post(reportUrl, JsonUtil.toJson(metricsEvent), REQUEST_TIMEOUT_MS);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


//    public static void main(String[] args) throws Exception{
//        String reportUrl = "http://127.0.0.1:5000/items";
//        HttpUtil.post(reportUrl, "{\"name\":\"陈小儿\",\"age\":34}", REQUEST_TIMEOUT_MS);
//    }

}
