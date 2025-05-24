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
            httpMetricsDataThreadLocal.set(httpMetricsData);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to process request: " + e.getMessage());
        }
    }

    @Override
    public Object afterCall(Object response, Throwable throwable) {
        HttpMetricsData httpMetricsData = httpMetricsDataThreadLocal.get();
        if (httpMetricsData == null) {
            return null;
        }
        if (throwable != null) {
            httpMetricsData.setHttpStatus("client error");
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
            e.printStackTrace();
            System.err.println("Failed to process response: " + e.getMessage());
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
