package com.lifelover.dome.core.report;

import java.util.Date;

import com.lifelover.dome.core.config.ConfigLoader;
import com.lifelover.dome.db.core.DbAccess;
import com.lifelover.dome.db.entity.ApiRecords;

@SuppressWarnings("rawtypes")
public class DbEventReporter extends AbstractEventReporter {
    @Override
    protected void handle(MetricsEvent metricsEvent) {
        //只保存http事件
        if (metricsEvent == null || !ReportType.HTTP.name().equals(metricsEvent.getEventType())) {
            return;
        }
        HttpMetricsData httpMetricsData =(HttpMetricsData) metricsEvent.getEventData();
        DbAccess dbAccess = ConfigLoader.getAgentConfig().getDbAccess();
        if (dbAccess == null) {
            System.err.println("[dome agent] dbAccess is null");
            return;
        }
        ApiRecords apiRecords = assembleApiRecords(httpMetricsData);
        dbAccess.addApiRecords(apiRecords);
    }

    private ApiRecords assembleApiRecords(HttpMetricsData httpMetricsData) {
        final Date now = new Date();
        ApiRecords apiRecords = new ApiRecords();
        apiRecords.setHttpMethod(httpMetricsData.getHttpMethod());
        apiRecords.setHttpUrl(httpMetricsData.getHttpUrl());
        apiRecords.setQueryParams(httpMetricsData.getQueryParams());
        apiRecords.setRequestBody(httpMetricsData.getRequestBody());
        apiRecords.setResponseBody(httpMetricsData.getResponseBody());
        apiRecords.setHttpStatus(httpMetricsData.getHttpStatus());
        apiRecords.setReqTime(new Date(httpMetricsData.getReqTime()));
        apiRecords.setResTime(new Date(httpMetricsData.getRespTime()));
        // apiRecords.setMetricTime(httpMetricsData.getMetricTime());
        // apiRecords.setMetricType(httpMetricsData.getMetricType());
        apiRecords.setTraceId(httpMetricsData.getTraceId());
        apiRecords.setCreatedAt(now);
        return apiRecords;
    }
}
