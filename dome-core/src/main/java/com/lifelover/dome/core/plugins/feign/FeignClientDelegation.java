package com.lifelover.dome.core.plugins.feign;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.lifelover.dome.core.helpers.MethodNames;
import com.lifelover.dome.core.helpers.ReflectMethods;
import com.lifelover.dome.core.helpers.StreamUtils;
import com.lifelover.dome.core.report.EventReporterHolder;
import com.lifelover.dome.core.report.HttpMetricsData;
import com.lifelover.dome.core.report.MetricsEvent;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

public class FeignClientDelegation {
    public static final ThreadLocal<HttpMetricsData> httpMetricsDataThreadLocal = new ThreadLocal<>();

    @Advice.OnMethodEnter()
    public static void onMethodEnter(
            @Advice.Argument(readOnly = false, value = 0, typing = Assigner.Typing.DYNAMIC) Object request,
            @Advice.Argument(readOnly = false, value = 1, typing = Assigner.Typing.DYNAMIC) Object options)
            throws Exception {
        try {
            Class<?> reqClz = request.getClass();
            // feign client 的 request 不存在重复读取流问题，直接读取字节数组即可
            // 清除threadLocal
            httpMetricsDataThreadLocal.remove();
            HttpMetricsData httpMetricsData = new HttpMetricsData();
            // 获取请求头

            // 获取 url
            String httpUrl = ReflectMethods.invokeMethod(reqClz, MethodNames.URL_METHOD, request);
            // 获取请求方法
            String httpMethod = ReflectMethods.invokeMethod(reqClz, MethodNames.METHOD_METHOD, request);
            // 获取请求体
            byte[] reqBytes = ReflectMethods.invokeMethod(reqClz, MethodNames.BODY_METHOD, request);
            final Long now = System.currentTimeMillis();
            // 设置httpMetricsData
            httpMetricsData.setHttpUrl(httpUrl);
            httpMetricsData.setHttpMethod(httpMethod);
            httpMetricsData.setReqTime(now);
            httpMetricsData.setMetricType("client");
            if(reqBytes != null) {
                httpMetricsData.setRequestBody(new String(reqBytes, StandardCharsets.UTF_8));
            }
            httpMetricsDataThreadLocal.set(httpMetricsData);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to wrap Feign request: " + e.getMessage());
        }
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onMethodExit(
            @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object response,
            @Advice.Thrown Throwable throwable)
            throws Exception {
        try {
            // 如果threadLocal 没有，什么都不做
            HttpMetricsData httpMetricsData = httpMetricsDataThreadLocal.get();
            if (httpMetricsData == null) {
                return;
            }
            final Long now = System.currentTimeMillis();
            httpMetricsData.setMetricTime(now);
            httpMetricsData.setRespTime(now);
            // 调用直接报错
            if (throwable != null) {
                httpMetricsData.setHttpStatus("ERR");
                httpMetricsData.setResponseBody(throwable.getMessage());
                MetricsEvent<HttpMetricsData> event = new MetricsEvent<HttpMetricsData>();
                event.setEventData(httpMetricsData);
                EventReporterHolder.getEventReporter().asyncReport(event);
                return;
            }

            Class<?> resClz = response.getClass();
            final int httpStatus = ReflectMethods.invokeMethod(resClz, MethodNames.FEIGN_STATUS_METHOD, response);
            httpMetricsData.setHttpStatus(httpStatus + "");
            // 构造新的 response
            byte[] bodyBytes = readResponseBytes(response);
            // 构造新的 response，流重复读
            Object responseBuilder = ReflectMethods.invokeMethod(resClz, "toBuilder", response);
            // body 塞入字节数组
            responseBuilder = ReflectMethods.invokeMethod(responseBuilder.getClass(), MethodNames.BODY_METHOD,
                    new Class[] { byte[].class }, responseBuilder, bodyBytes);
            response = ReflectMethods.invokeMethod(responseBuilder.getClass(), MethodNames.BUILD_METHOD, responseBuilder);
            MetricsEvent<HttpMetricsData> event = new MetricsEvent<HttpMetricsData>();
            event.setEventData(httpMetricsData);
            EventReporterHolder.getEventReporter().asyncReport(event);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("intercept on Feign response error: " + e.getMessage());
        }
    }

    public static byte[] readResponseBytes(Object response) throws Exception {
        // 获取inputStream
        Object body = ReflectMethods.invokeMethod(response.getClass(), MethodNames.BODY_METHOD, response);
        InputStream is  = ReflectMethods.invokeMethod(body.getClass(), "asInputStream", body);
        return StreamUtils.copyToByteArray(is);
    }
}