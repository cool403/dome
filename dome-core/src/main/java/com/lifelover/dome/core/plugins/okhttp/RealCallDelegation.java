package com.lifelover.dome.core.plugins.okhttp;

import java.lang.reflect.Method;

import com.lifelover.dome.core.helpers.ClassNames;
import com.lifelover.dome.core.helpers.MethodNames;
import com.lifelover.dome.core.helpers.ReflectMethods;
import com.lifelover.dome.core.helpers.TargetAppClassRegistry;
import com.lifelover.dome.core.report.EventReporterHolder;
import com.lifelover.dome.core.report.HttpMetricsData;
import com.lifelover.dome.core.report.MetricsEvent;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

public class RealCallDelegation {

    public static final ThreadLocal<HttpMetricsData> httpMetricsDataThreadLocal = new ThreadLocal<>();

    //okhttp3.internal.Version.userAgent
    @Advice.OnMethodEnter()
    public static void onMethodEnter(@Advice.This Object call) {
        Class<?> callClz = call.getClass();
        Class<?> bufferClz = TargetAppClassRegistry.getClass(ClassNames.BUFFER_CLASS_NAME);
        try {
            // 清理当前线程的httpMetricsDataThreadLocal
            httpMetricsDataThreadLocal.remove();
            // 获取请求
            Object originalRequest = ReflectMethods.invokeMethod(callClz, MethodNames.REQUEST_METHOD, call);
            System.out.println("originalRequest: " + originalRequest);
            if (originalRequest == null) {
                return;
            }
            Class<?> originalRequestClz = originalRequest.getClass();

            // 获取请求头
            Object headers = ReflectMethods.invokeMethod(originalRequestClz, MethodNames.HEADERS_METHOD,
                    originalRequest);
            // 获取请求方法
            String method = ReflectMethods.invokeMethod(originalRequestClz, MethodNames.METHOD_METHOD, originalRequest);
            // 获取请求url
            String url = ReflectMethods.invokeMethod(originalRequestClz, MethodNames.URL_METHOD, originalRequest)
                    .toString();
            HttpMetricsData httpMetricsData = new HttpMetricsData();
            // post 记录请求体
            if (null != method && "POST".equals(method)) {
                Object requestBody = ReflectMethods.invokeMethod(originalRequestClz, MethodNames.BODY_METHOD, originalRequest);
                Class<?> bufferSinkClz = TargetAppClassRegistry.getClass(ClassNames.OKIO_BUFFERED_SINK_CLASS_NAME);

                if (requestBody != null && bufferClz != null) {
                    Class<?> requestBodyClz = requestBody.getClass();
                    // 获取contentType
                    Object contentType = ReflectMethods.invokeMethod(requestBodyClz, MethodNames.CONTENT_TYPE_METHOD,
                            requestBody);
                    Object buffer = bufferClz.getConstructor().newInstance();
                    ReflectMethods.invokeMethod(requestBodyClz, MethodNames.WRITE_TO_METHOD,new Class[] { bufferSinkClz }, requestBody, buffer);
                    // 获取字节数据，并且复制
                    byte[] bytes = ReflectMethods.invokeMethod(bufferClz, MethodNames.READ_BYTES_ARRAY_METHOD, buffer);
                    // 记录请求体
                    httpMetricsData.setRequestBody(new String(bytes));
                    Class<?> mediaTypeClz = TargetAppClassRegistry.getClass(ClassNames.MEDIA_TYPE_CLASS_NAME);
                    Method createMethod = ReflectMethods.getMethod(requestBodyClz, MethodNames.CREATE_METHOD,
                            mediaTypeClz, byte[].class);
                    Object newRequestBody = createMethod.invoke(null, contentType, bytes);
                    Object newRequestBuilder = ReflectMethods.invokeMethod(originalRequestClz,MethodNames.NEW_BUILDER_METHOD, originalRequest);
                    Class<?> newRequestBuilderClz = newRequestBuilder.getClass();
                    newRequestBuilder = ReflectMethods.invokeMethod(newRequestBuilderClz, MethodNames.METHOD_METHOD,
                            new Class[] {String.class, requestBodyClz.getSuperclass() },
                            newRequestBuilder,method, newRequestBody);
                    Object newRequest = ReflectMethods.invokeMethod(newRequestBuilderClz, MethodNames.BUILD_METHOD,
                            newRequestBuilder);
                    // 3. 替换原始Request（通过反射修改字段）
                    ReflectMethods.setFieldValue(call, "originalRequest", newRequest);
                }
            }
            httpMetricsData.setHttpMethod(method);
            httpMetricsData.setHttpUrl(url);
            httpMetricsData.setHttpStatus("200");
            httpMetricsData.setMetricTime(System.currentTimeMillis());
            httpMetricsData.setReqTime(System.currentTimeMillis());
            httpMetricsData.setRespTime(System.currentTimeMillis());
            httpMetricsData.setTraceId(null);
            httpMetricsDataThreadLocal.set(httpMetricsData);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to process request: " + e.getMessage());
        }
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onMethodExit(@Advice.This Object call,
            @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object response,
            @Advice.Thrown Throwable throwable) {
        HttpMetricsData httpMetricsData = httpMetricsDataThreadLocal.get();
        if (httpMetricsData == null) {
            return;
        }
        // 调用call.execute()方法抛出异常
        if (throwable != null) {
            httpMetricsData.setHttpStatus("client error");
            httpMetricsData.setRespTime(System.currentTimeMillis());
            httpMetricsData.setResponseBody(throwable.getMessage());
            MetricsEvent<HttpMetricsData> event = new MetricsEvent<HttpMetricsData>();
            event.setEventData(httpMetricsData);
            EventReporterHolder.getEventReporter().asyncReport(event);
            return;
        }
        try {
            Class<?> responseClz = response.getClass();
            // 获取响应状态码
            Object code = ReflectMethods.invokeMethod(responseClz, MethodNames.CODE_METHOD, response);
            httpMetricsData.setHttpStatus(code.toString());
            httpMetricsData.setRespTime(System.currentTimeMillis());
            // 获取响应体ResponseBody
            Object responseBody = ReflectMethods.invokeMethod(responseClz, MethodNames.BODY_METHOD, response);
            Class<?> responseBodyClz = responseBody.getClass();

            // 获取mediaType
            Object mediaType = ReflectMethods.invokeMethod(responseBodyClz, MethodNames.CONTENT_TYPE_METHOD,
                    responseBody);
            // 获取字节数据
            byte[] bytes = ReflectMethods.invokeMethod(responseBodyClz, MethodNames.BYTES_METHOD, responseBody);
            httpMetricsData.setResponseBody(new String(bytes));
            // 打印日志
            System.out.println("Response body: " + new String(bytes));

            // 由于流是单次读取，所以创建新的response
            Object newResponseBuilder = ReflectMethods.invokeMethod(responseClz, MethodNames.NEW_BUILDER_METHOD,
                    response);
            Class<?> newResponseBuilderClz = newResponseBuilder.getClass();

            // 创建新的ResponseBody ;; 3 和 4 不一样
            Object newResponseBody = ReflectMethods.invokeMethod(responseBodyClz, MethodNames.CREATE_METHOD,
                    new Class[] { TargetAppClassRegistry.getClass(ClassNames.MEDIA_TYPE_CLASS_NAME), byte[].class },
                    null, mediaType, bytes);
            // 设置新的ResponseBody
            newResponseBuilder = ReflectMethods.invokeMethod(newResponseBuilderClz, MethodNames.BODY_METHOD,
                    new Class[] { responseBodyClz.getSuperclass() }, newResponseBuilder, newResponseBody);
            // 创建新的Response
            Object newResponse = ReflectMethods.invokeMethod(newResponseBuilderClz, MethodNames.BUILD_METHOD,
                    newResponseBuilder);

            // 记录metrics
            httpMetricsData.setRespTime(System.currentTimeMillis());
            httpMetricsData.setMetricTime(System.currentTimeMillis());
            MetricsEvent<HttpMetricsData> event = new MetricsEvent<HttpMetricsData>();
            event.setEventData(httpMetricsData);
            EventReporterHolder.getEventReporter().asyncReport(event);

            response = newResponse;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to process response: " + e.getMessage());
        }
    }

}
