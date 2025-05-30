package com.lifelover.dome.core.plugins.resttemplate;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.Map;

import com.lifelover.dome.core.helpers.ClassNames;
import com.lifelover.dome.core.helpers.MethodNames;
import com.lifelover.dome.core.helpers.ReflectMethods;
import com.lifelover.dome.core.helpers.StreamUtils;
import com.lifelover.dome.core.helpers.TargetAppClassRegistry;
import com.lifelover.dome.core.report.EventReporterHolder;
import com.lifelover.dome.core.report.HttpMetricsData;
import com.lifelover.dome.core.report.MetricsEvent;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

public class RequestClassDelegation {
    public static final ThreadLocal<HttpMetricsData> httpMetricsDataThreadLocal = new ThreadLocal<>();

    @Advice.OnMethodEnter()
    public static void onMethodEnter(@Advice.This Object call,
            @Advice.Argument(value = 0, typing = Assigner.Typing.DYNAMIC) Object httpHeaders,
            @Advice.Argument(value = 1, typing = Assigner.Typing.DYNAMIC) byte[] reqBytes) {
        try {
            // 清除threadLocal
            httpMetricsDataThreadLocal.remove();
            HttpMetricsData httpMetricsData = new HttpMetricsData();
            final Long now = System.currentTimeMillis();
            httpMetricsData.setReqTime(now);
            httpMetricsData.setMetricType("client");
            Class<?> reqClz = call.getClass();
            // 获取 url
            String httpUrl = ReflectMethods.invokeMethod(reqClz, MethodNames.GET_URI_METHOD, call).toString();
            // 获取请求方法
            String httpMethod = ReflectMethods.invokeMethod(reqClz, MethodNames.GET_METHOD_METHOD, call).toString();
            // 加载缓存 HttpHeader
            Class<?> httpHeaderClz = TargetAppClassRegistry.getClass(ClassNames.RT_HTTP_HEADER_CLASS_NAME);
            // 调用 toSingleValueMap
            Map<String, String> headerMap = ReflectMethods.invokeMethod(httpHeaderClz,
                    MethodNames.TO_SINGLE_VALUE_MAP_METHOD, httpHeaders);
            // 获取请求体
            //判断是否已经被BufferingClientHttpRequestWrapper 包装过
            if (reqBytes != null) {
                httpMetricsData.setRequestBody(new String(reqBytes));
            }
            httpMetricsData.setHttpUrl(httpUrl);
            httpMetricsData.setHttpMethod(httpMethod);
            httpMetricsDataThreadLocal.set(httpMetricsData);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to wrap Resttemplate request: " + e.getMessage());
        }

    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onMethodExit(@Advice.This Object call,
            @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object response,
            @Advice.Argument(value = 0, typing = Assigner.Typing.DYNAMIC) Object httpHeaders,
            @Advice.Argument(value = 1, typing = Assigner.Typing.DYNAMIC) byte[] reqBytes,
            @Advice.Thrown Throwable throwable) {
        try {
            HttpMetricsData httpMetricsData = httpMetricsDataThreadLocal.get();
            if (httpMetricsData == null) {
                return;
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
                return;
            }
            // 获取响应状态码
            Class<?> responseClz = response.getClass();
            int httpStatus = ReflectMethods.invokeMethod(responseClz, MethodNames.GET_RAW_STATUS_METHOD, response);
            httpMetricsData.setHttpStatus(httpStatus + "");
            // 获取响应体
            // 首先判断是否已经被BufferingClientHttpResponseWrapper 包装过
            if(responseClz.isAssignableFrom(TargetAppClassRegistry.getClass(ClassNames.RT_BUFFER_RESPONSE_CLASS_NAME)) == false) {
                //转换成 BufferingClientHttpResponseWrapper
                Constructor<?> iniConstructor = TargetAppClassRegistry.getClass(ClassNames.RT_BUFFER_RESPONSE_CLASS_NAME)
                .getConstructor(TargetAppClassRegistry.getClass(ClassNames.RT_BASIC_RESPONSE_CLASS_NAME));
                Object bufferResponse = iniConstructor.newInstance(response);
                response = bufferResponse;
                // 调用 copyBodyToResponse
            }
            //直接获取 body
            InputStream is = ReflectMethods.invokeMethod(responseClz, MethodNames.GET_BODY_METHOD, response);
            byte[] bodyBytes = StreamUtils.copyToByteArray(is);
            httpMetricsData.setResponseBody(new String(bodyBytes));
            httpMetricsData.setHttpMethod(httpStatus + "");
            MetricsEvent<HttpMetricsData> event = new MetricsEvent<HttpMetricsData>();
            event.setEventData(httpMetricsData);
            EventReporterHolder.getEventReporter().asyncReport(event); 
            httpMetricsDataThreadLocal.remove();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to wrap Resttemplate request: " + e.getMessage());
        }
    }
}
