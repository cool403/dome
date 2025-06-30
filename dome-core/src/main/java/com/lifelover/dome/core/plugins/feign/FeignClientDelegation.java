package com.lifelover.dome.core.plugins.feign;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.lifelover.dome.core.helpers.ClassloaderRegistry;
import com.lifelover.dome.core.helpers.MethodNames;
import com.lifelover.dome.core.helpers.ReflectMethods;
import com.lifelover.dome.core.helpers.StreamUtils;
import com.lifelover.dome.core.mock.ApiMockContext;
import com.lifelover.dome.core.mock.ApiMockInterceptor;
import com.lifelover.dome.core.report.EventReporterHolder;
import com.lifelover.dome.core.report.HttpMetricsData;
import com.lifelover.dome.core.report.MetricsEvent;
import com.lifelover.dome.db.entity.ApiRecords;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

public class FeignClientDelegation {
    public static final ThreadLocal<HttpMetricsData> httpMetricsDataThreadLocal = new ThreadLocal<>();

    /**
     * 拦截器，返回为true，就不会真实调用，为false就会真实调用，适合mock工具开发
     * @param request
     * @param options
     * @return
     * @throws Exception
     */
    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static Object onMethodEnter(
            @Advice.Argument(readOnly = false, value = 0, typing = Assigner.Typing.DYNAMIC) Object request,
            @Advice.Argument(readOnly = false, value = 1, typing = Assigner.Typing.DYNAMIC) Object options)
            throws Exception {
        try {
            // 清除threadLocal
            httpMetricsDataThreadLocal.remove();
            Class<?> reqClz = request.getClass();
            Class<?> resClz = ClassloaderRegistry.getTargetAppClassLoader().loadClass("feign.Response");
            // feign client 的 request 不存在重复读取流问题，直接读取字节数组即可
            // 获取 url
            String httpUrl = ReflectMethods.invokeMethod(reqClz, MethodNames.URL_METHOD, request);
            // 获取请求方法
            String httpMethod = ReflectMethods.invokeMethod(reqClz, MethodNames.METHOD_METHOD, request);
            // 获取请求体
            byte[] reqBytes = ReflectMethods.invokeMethod(reqClz, MethodNames.BODY_METHOD, request);
            // 判断是否需要mock
            ApiMockContext apiMockContext = new ApiMockContext();
            apiMockContext.setHttpMethod(httpMethod);
            apiMockContext.setHttpUrl(httpUrl);
            ApiRecords apiRecords = ApiMockInterceptor.mock(apiMockContext);
            if (apiRecords != null) {
                System.out.println("[dome agent] feign_url=" + httpUrl + ",http_method=" + httpMethod + "命中mock,直接返回mock数据");
                // 构造mock response
                Object responseBuilder = ReflectMethods.invokeMethod(resClz, "builder", null);
                Class<?> responseBuilderClz = responseBuilder.getClass();
                responseBuilder = ReflectMethods.invokeMethod(responseBuilderClz, "status",
                        new Class[] { int.class }, responseBuilder, 200);
                //需要塞入原始request
                responseBuilder = ReflectMethods.invokeMethod(responseBuilderClz, "request",
                        new Class[] { reqClz }, responseBuilder, request);
                responseBuilder = ReflectMethods.invokeMethod(responseBuilderClz, "body",
                        new Class[] { byte[].class }, responseBuilder, apiRecords.getResponseBody().getBytes(StandardCharsets.UTF_8));
                //塞入contentType 为application/json
                Map<String,Collection<String>> headers = new HashMap<>();
                headers.put("Content-Type", Collections.singletonList("application/json"));
                responseBuilder = ReflectMethods.invokeMethod(responseBuilderClz, "headers",
                        new Class[] { Map.class }, responseBuilder, headers);
                Object response = ReflectMethods.invokeMethod(responseBuilderClz, "build", responseBuilder);
                return response; // 返回mock response
            }
            // 如果没有命中mock，继续正常流程
            HttpMetricsData httpMetricsData = new HttpMetricsData();
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
            System.err.println("[dome agent] Failed to wrap Feign request: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // 如果没有命中mock，返回null继续正常流程
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onMethodExit(
            @Advice.Enter Object fixedValue,
            @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object response,
            @Advice.Thrown Throwable throwable)
            throws Exception {
        if (fixedValue != null){
            response = fixedValue;
            return;   
        }
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
            System.out.println("[dome agent] intercept on Feign response error: " + e.getMessage());
        }
    }

    public static byte[] readResponseBytes(Object response) throws Exception {
        // 获取inputStream
        Object body = ReflectMethods.invokeMethod(response.getClass(), MethodNames.BODY_METHOD, response);
        InputStream is  = ReflectMethods.invokeMethod(body.getClass(), "asInputStream", body);
        return StreamUtils.copyToByteArray(is);
    }
}