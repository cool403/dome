package com.lifelover.dome.core.plugins.okhttp;

import com.lifelover.dome.core.report.EventReporterHolder;
import com.lifelover.dome.core.report.HttpMetricsData;
import com.lifelover.dome.core.report.MetricsEvent;

public abstract class AbstractOkHttpAdapter implements OkhttpAdapter {
    private static ThreadLocal<HttpMetricsData> httpMetricsDataThreadLocal = new ThreadLocal<>();

    @Override
    public void beforeCall(Object call) {
        // 清空httpMetricsDataThreadLocal
        httpMetricsDataThreadLocal.remove();
        try {
            HttpMetricsData httpMetricsData = initHttpMetricsData(call);
            if (httpMetricsData == null) {
                return;
            }
            httpMetricsData.setMetricType("client");
            httpMetricsDataThreadLocal.set(httpMetricsData);
        } catch (Exception e) {
            System.err.println("[dome agent] Failed to process request: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public Object afterCall(Object response, Throwable throwable) {
        HttpMetricsData httpMetricsData = httpMetricsDataThreadLocal.get();
        if (httpMetricsData == null) {
            return null;
        }
        if (throwable != null) {
            httpMetricsData.setHttpStatus("ERR");
            httpMetricsData.setRespTime(System.currentTimeMillis());
            httpMetricsData.setResponseBody(throwable.getMessage());
            MetricsEvent<HttpMetricsData> event = new MetricsEvent<HttpMetricsData>();
            event.setEventData(httpMetricsData);
            EventReporterHolder.getEventReporter().asyncReport(event);
            return null;
        }
        try {
            Object newResponse = fullFillHttpMetricsData(httpMetricsData, response);
            // 填充两个时间
            final long now = System.currentTimeMillis();
            httpMetricsData.setRespTime(now);
            httpMetricsData.setMetricTime(now);
            MetricsEvent<HttpMetricsData> event = new MetricsEvent<HttpMetricsData>();
            event.setEventData(httpMetricsData);
            EventReporterHolder.getEventReporter().asyncReport(event);
            return newResponse;
        } catch (Exception e) {
            System.err.println("[dome agent] Failed to process response: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 
     * @param call
     * @return
     */
    protected abstract HttpMetricsData initHttpMetricsData(Object call) throws Exception;

    /**
     * 
     * @param httpMetricsData
     * @param response
     */
    protected abstract Object fullFillHttpMetricsData(HttpMetricsData httpMetricsData, Object response)
            throws Exception;
}
