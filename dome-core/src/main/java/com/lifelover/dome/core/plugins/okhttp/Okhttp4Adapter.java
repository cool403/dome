package com.lifelover.dome.core.plugins.okhttp;

import java.lang.reflect.Method;

import com.lifelover.dome.core.helpers.ClassNames;
import com.lifelover.dome.core.helpers.MethodNames;
import com.lifelover.dome.core.helpers.ReflectMethods;
import com.lifelover.dome.core.helpers.TargetAppClassRegistry;
import com.lifelover.dome.core.report.HttpMetricsData;

public class Okhttp4Adapter extends AbstractOkHttpAdapter {

    @Override
    protected HttpMetricsData initHttpMetricsData(Object call) throws Exception {
        Class<?> callClz = call.getClass();
        Class<?> bufferClz = TargetAppClassRegistry.getClass(ClassNames.BUFFER_CLASS_NAME);
        // 获取请求
        Object originalRequest = ReflectMethods.invokeMethod(callClz, MethodNames.REQUEST_METHOD, call);
        if (originalRequest == null) {
            return null;
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
        if ("POST".equals(method)) {
            Object requestBody = ReflectMethods.invokeMethod(originalRequestClz, MethodNames.BODY_METHOD,
                    originalRequest);
            Class<?> bufferSinkClz = TargetAppClassRegistry.getClass(ClassNames.OKIO_BUFFERED_SINK_CLASS_NAME);

            if (requestBody != null && bufferClz != null) {
                Class<?> requestBodyClz = requestBody.getClass();
                // 获取contentType
                Object contentType = ReflectMethods.invokeMethod(requestBodyClz, MethodNames.CONTENT_TYPE_METHOD,
                        requestBody);
                Object buffer = bufferClz.getConstructor().newInstance();
                ReflectMethods.invokeMethod(requestBodyClz, MethodNames.WRITE_TO_METHOD, new Class[] { bufferSinkClz },
                        requestBody, buffer);
                // 获取字节数据，并且复制
                byte[] bytes = ReflectMethods.invokeMethod(bufferClz, MethodNames.READ_BYTES_ARRAY_METHOD, buffer);
                // 记录请求体
                httpMetricsData.setRequestBody(new String(bytes));
                Class<?> mediaTypeClz = TargetAppClassRegistry.getClass(ClassNames.MEDIA_TYPE_CLASS_NAME);
                Method createMethod = ReflectMethods.getMethod(requestBodyClz, MethodNames.CREATE_METHOD,
                        mediaTypeClz, byte[].class);
                Object newRequestBody = createMethod.invoke(null, contentType, bytes);
                Object newRequestBuilder = ReflectMethods.invokeMethod(originalRequestClz,
                        MethodNames.NEW_BUILDER_METHOD, originalRequest);
                Class<?> newRequestBuilderClz = newRequestBuilder.getClass();
                newRequestBuilder = ReflectMethods.invokeMethod(newRequestBuilderClz, MethodNames.METHOD_METHOD,
                        new Class[] { String.class, requestBodyClz.getSuperclass() },
                        newRequestBuilder, method, newRequestBody);
                Object newRequest = ReflectMethods.invokeMethod(newRequestBuilderClz, MethodNames.BUILD_METHOD,
                        newRequestBuilder);
                // 3. 替换原始Request（通过反射修改字段）
                ReflectMethods.setFieldValue(call, "originalRequest", newRequest);
            }
        }
        httpMetricsData.setHttpMethod(method);
        httpMetricsData.setHttpUrl(url);
        httpMetricsData.setHttpStatus("200");
        httpMetricsData.setReqTime(System.currentTimeMillis());
        httpMetricsData.setTraceId(null);
        return httpMetricsData;
    }

    @Override
    protected Object fullFillHttpMetricsData(HttpMetricsData httpMetricsData, Object response) {
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
        System.out.println("[dome agent] Response body: " + new String(bytes));

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
        return ReflectMethods.invokeMethod(newResponseBuilderClz, MethodNames.BUILD_METHOD, newResponseBuilder);
    }

}
