package com.lifelover.dome.core.plugins.okhttp;

import java.nio.charset.StandardCharsets;

import com.lifelover.dome.core.helpers.ClassNames;
import com.lifelover.dome.core.helpers.MethodNames;
import com.lifelover.dome.core.helpers.ReflectMethods;
import com.lifelover.dome.core.helpers.TargetAppClassRegistry;
import com.lifelover.dome.core.mock.ApiMockContext;
import com.lifelover.dome.core.mock.ApiMockInterceptor;
import com.lifelover.dome.core.report.EventReporterHolder;
import com.lifelover.dome.core.report.HttpMetricsData;
import com.lifelover.dome.core.report.MetricsEvent;
import com.lifelover.dome.db.entity.ApiRecords;

public abstract class AbstractOkHttpAdapter implements OkhttpAdapter {
    private static final ThreadLocal<HttpMetricsData> httpMetricsDataThreadLocal = new ThreadLocal<>();

    @Override
    public Object beforeCall(Object call) {
        // 清空httpMetricsDataThreadLocal
        httpMetricsDataThreadLocal.remove();
        try {
            Class<?> callClz = call.getClass();
            // 获取请求
            Object originalRequest = ReflectMethods.invokeMethod(callClz, MethodNames.REQUEST_METHOD, call);
            if (originalRequest == null) {
                return null;
            }
            HttpMetricsData httpMetricsData = initHttpMetricsData(call, originalRequest);
            if (httpMetricsData == null) {
                return null;
            }
            httpMetricsData.setApiType("EXT");
            httpMetricsDataThreadLocal.set(httpMetricsData);
            return getMockResponse(httpMetricsData, originalRequest);
        } catch (Exception e) {
            System.err.println("[dome agent] 构造mock response失败: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 
     * @param httpMetricsData
     * @return
     */
    private Object getMockResponse(HttpMetricsData httpMetricsData, Object originalRequest) {
        String httpMethod = httpMetricsData.getHttpMethod();
        String httpUrl = httpMetricsData.getHttpUrl();
        ApiMockContext apiMockContext = new ApiMockContext();
        apiMockContext.setHttpMethod(httpMethod);
        apiMockContext.setHttpUrl(httpUrl);
        apiMockContext.setApiType("EXT");
        ApiRecords apiRecords = ApiMockInterceptor.mock(apiMockContext);
        if (apiRecords != null) {
            try {
                System.out.println(
                        "[dome agent] okhttp_url=" + httpUrl + ",http_method=" + httpMethod + "命中mock,直接返回mock数据");
                Class<?> responseBuilderClz = TargetAppClassRegistry
                        .getClass(ClassNames.OK_RESPONSE_BUILDER_CLASS_NAME);
                Class<?> requestClz = TargetAppClassRegistry.getClass(ClassNames.OK_REQUEST_CLASS_NAME);
                Object responseBuilder = responseBuilderClz.getConstructor().newInstance();
                // 初始化responseBody
                Class<?> responseBodyClz = TargetAppClassRegistry.getClass(ClassNames.RESPONSE_BODY_CLASS_NAME);
                Class<?> mediaTypeClz = TargetAppClassRegistry.getClass(ClassNames.MEDIA_TYPE_CLASS_NAME);
                // 默认是json
                Object mediaType = ReflectMethods.invokeMethod(mediaTypeClz, "parse", new Class[] { String.class },
                        null, "application/json");
                Object newResponseBody = ReflectMethods.invokeMethod(responseBodyClz, MethodNames.CREATE_METHOD,
                        new Class[] { byte[].class, mediaTypeClz },
                        null, apiRecords.getResponseBody().getBytes(StandardCharsets.UTF_8), mediaType);
                // 调用setBody$okhttp
                ReflectMethods.invokeMethod(responseBuilderClz, "setBody$okhttp", new Class[] { responseBodyClz },
                        responseBuilder, newResponseBody);
                ReflectMethods.invokeMethod(responseBuilderClz, "setCode$okhttp", new Class[] { int.class },
                        responseBuilder, 200);
                // 设置message
                ReflectMethods.invokeMethod(responseBuilderClz, "setMessage$okhttp", new Class[] { String.class },
                        responseBuilder, "OK");
                // 添加header
                ReflectMethods.invokeMethod(responseBuilderClz, "addHeader", new Class[] { String.class, String.class },
                        responseBuilder, "Content-Type", "application/json");
                // 设置request
                ReflectMethods.invokeMethod(responseBuilderClz, "setRequest$okhttp", new Class[] { requestClz },
                        responseBuilder, originalRequest);
                // 设置Protocol
                Class<?> protocolClz = TargetAppClassRegistry.getClass(ClassNames.OK_PROTOCOL_CLASS_NAME);
                Object protocol = ReflectMethods.invokeMethod(protocolClz, "get", new Class[] { String.class }, null,
                        "http/1.1");
                ReflectMethods.invokeMethod(responseBuilderClz, "setProtocol$okhttp", new Class[] { protocolClz },
                        responseBuilder, protocol);
                return ReflectMethods.invokeMethod(responseBuilderClz, "build", responseBuilder);
            } catch (Exception e) {
                System.err.println("[dome agent] 构造mock response失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public Object afterCall(Object response, Throwable throwable) {
        HttpMetricsData httpMetricsData = httpMetricsDataThreadLocal.get();
        if (httpMetricsData == null) {
            return null;
        }
        final long now = System.currentTimeMillis();
        httpMetricsData.setMetricTime(now);
        httpMetricsData.setRespTime(now);
        if (throwable != null) {
            httpMetricsData.setHttpStatus("ERR");
            httpMetricsData.setResponseBody(throwable.getMessage());
            MetricsEvent<HttpMetricsData> event = new MetricsEvent<HttpMetricsData>();
            event.setEventData(httpMetricsData);
            EventReporterHolder.getEventReporter().asyncReport(event);
            return null;
        }
        try {
            Object newResponse = fullFillHttpMetricsData(httpMetricsData, response);
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
    protected abstract HttpMetricsData initHttpMetricsData(Object call, Object originalRequest) throws Exception;

    /**
     * 
     * @param httpMetricsData
     * @param response
     */
    protected abstract Object fullFillHttpMetricsData(HttpMetricsData httpMetricsData, Object response)
            throws Exception;
}
