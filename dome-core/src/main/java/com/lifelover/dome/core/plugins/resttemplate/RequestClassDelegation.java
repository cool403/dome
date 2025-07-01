package com.lifelover.dome.core.plugins.resttemplate;

import java.io.InputStream;
import java.lang.reflect.Constructor;

import com.lifelover.dome.core.helpers.ClassNames;
import com.lifelover.dome.core.helpers.MethodNames;
import com.lifelover.dome.core.helpers.ReflectMethods;
import com.lifelover.dome.core.helpers.StreamUtils;
import com.lifelover.dome.core.helpers.TargetAppClassRegistry;
import com.lifelover.dome.core.report.EventReporterHolder;
import com.lifelover.dome.core.report.HttpMetricsData;
import com.lifelover.dome.core.report.MetricsEvent;
import com.lifelover.dome.core.mock.ApiMockContext;
import com.lifelover.dome.core.mock.ApiMockInterceptor;
import com.lifelover.dome.db.entity.ApiRecords;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

public class RequestClassDelegation {
    public static final ThreadLocal<HttpMetricsData> httpMetricsDataThreadLocal = new ThreadLocal<>();

    public static volatile Constructor<?> privateConstructor = null;

    /**
     * 拦截器，返回为true，就不会真实调用，为false就会真实调用，适合mock工具开发
     * @param call
     * @param httpHeaders
     * @param reqBytes
     * @return
     * @throws Exception
     */
    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static Object onMethodEnter(@Advice.This Object call,
            @Advice.Argument(value = 0, typing = Assigner.Typing.DYNAMIC) Object httpHeaders,
            @Advice.Argument(value = 1, typing = Assigner.Typing.DYNAMIC) byte[] reqBytes) {
        try {
            // 清除threadLocal
            httpMetricsDataThreadLocal.remove();
            
            // 获取请求信息
            HttpMetricsData httpMetricsData = new HttpMetricsData();
            final Long now = System.currentTimeMillis();
            httpMetricsData.setReqTime(now);
            httpMetricsData.setMetricType("client");
            
            // 获取请求信息
            Class<?> reqClz = call.getClass();
            String httpUrl = ReflectMethods.invokeMethod(reqClz, MethodNames.GET_URI_METHOD, call).toString();
            String httpMethod = ReflectMethods.invokeMethod(reqClz, MethodNames.GET_METHOD_METHOD, call).toString();
            //获取headers
            Object headers = ReflectMethods.invokeMethod(reqClz, MethodNames.GET_HEADERS_METHOD, call);
            //获取contentType
            Object mediaType = ReflectMethods.invokeMethod(headers.getClass(), "getContentType", headers);
            String contentType = mediaType.toString();

            // 判断是否需要mock
            ApiMockContext apiMockContext = new ApiMockContext();
            apiMockContext.setHttpUrl(httpUrl);
            apiMockContext.setHttpMethod(httpMethod);
            apiMockContext.setContentType(contentType);
            
            // 判断是否需要mock
            ApiRecords apiRecords = ApiMockInterceptor.mock(apiMockContext);
            if (apiRecords != null) {
                System.out.println("[dome agent] resttemplate_url=" + httpUrl + ",http_method=" + httpMethod + "命中mock,直接返回mock数据");
                // 构建mock响应
                try {
                    Object mockResponse = DynamicResponseInstanceBuilder.createClientHttpResponse(apiRecords.getResponseBody(), 200, "application/json");
                    return mockResponse;
                } catch (Exception e) {
                    System.err.println("[dome agent] Failed to create mock response: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }
            
            // 加载缓存 HttpHeader
            Class<?> httpHeaderClz = TargetAppClassRegistry.getClass(ClassNames.RT_HTTP_HEADER_CLASS_NAME);
            // 调用 toSingleValueMap
            ReflectMethods.invokeMethod(httpHeaderClz,
                    MethodNames.TO_SINGLE_VALUE_MAP_METHOD, httpHeaders);
            // 获取请求体
            //判断是否已经被BufferingClientHttpRequestWrapper 包装过
            if (reqBytes != null) {
                httpMetricsData.setRequestBody(new String(reqBytes));
            }
            // 继续处理正常请求
            httpMetricsData.setHttpUrl(httpUrl);
            httpMetricsData.setHttpMethod(httpMethod);
            httpMetricsDataThreadLocal.set(httpMetricsData);
            return null;
        } catch (Exception e) {
            System.err.println("[dome agent] Failed to wrap Resttemplate request: " + e.getMessage());
            e.printStackTrace();
            return null;
        }

    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onMethodExit(@Advice.This Object call,
            @Advice.Enter Object fixedValue,
            @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object response,
            @Advice.Argument(value = 0, typing = Assigner.Typing.DYNAMIC) Object httpHeaders,
            @Advice.Argument(value = 1, typing = Assigner.Typing.DYNAMIC) byte[] reqBytes,
            @Advice.Thrown Throwable throwable) {
        if (fixedValue != null){
            response = fixedValue;
            return;   
        }
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
            if(!responseClz.isAssignableFrom(TargetAppClassRegistry.getClass(ClassNames.RT_BUFFER_RESPONSE_CLASS_NAME))) {
                //转换成 BufferingClientHttpResponseWrapper
                if (privateConstructor == null) {
                    privateConstructor = TargetAppClassRegistry.getClass(ClassNames.RT_BUFFER_RESPONSE_CLASS_NAME)
                    .getDeclaredConstructor(TargetAppClassRegistry.getClass(ClassNames.RT_BASIC_RESPONSE_CLASS_NAME));
                    privateConstructor.setAccessible(true);
                }
                response = privateConstructor.newInstance(response);
            }
            //直接获取 body
            InputStream is = ReflectMethods.invokeMethod(response.getClass(), MethodNames.GET_BODY_METHOD, response);
            byte[] bodyBytes = StreamUtils.copyToByteArray(is);
            httpMetricsData.setResponseBody(new String(bodyBytes));
            httpMetricsData.setHttpStatus(httpStatus + "");
            MetricsEvent<HttpMetricsData> event = new MetricsEvent<HttpMetricsData>();
            event.setEventData(httpMetricsData);
            EventReporterHolder.getEventReporter().asyncReport(event); 
            httpMetricsDataThreadLocal.remove();
        } catch (Exception e) {
            System.err.println("[dome agent] Failed to wrap Resttemplate request: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
