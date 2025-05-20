package com.lifelover.dome.core.plugins.okhttp;

import net.bytebuddy.asm.Advice;

public class RealCallDelegation {

    private static final long MAX_BODY_SIZE = 1024 * 1024; // 1MB

    @Advice.OnMethodEnter()
    public static void onMethodEnter(@Advice.This Object call) {
        System.out.println("111111111");
        try {
            Object originalRequest = call.getClass().getMethod("request").invoke(call);

            // 处理请求体并创建新请求
            Object newRequest = handleRequestBody(originalRequest);

            // 将新请求设置回call对象
            call.getClass().getMethod("request", originalRequest.getClass()).invoke(call, newRequest);

            logRequestInfo(newRequest);
        } catch (Exception e) {
            System.err.println("Failed to process request: " + e.getMessage());
        }
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onMethodExit(@Advice.This Object call,
            @Advice.Return Object response,
            @Advice.Thrown Throwable throwable) {
        System.out.println("111111111");
        if (throwable != null) {
            System.out.println("[OkHttp] Call failed: " + throwable.getMessage());
            return;
        }

        try {
            Object newResponse = handleResponseBody(response);
            logResponseInfo(newResponse);

            // 返回修改后的response
            response = newResponse;
        } catch (Exception e) {
            System.err.println("Failed to process response: " + e.getMessage());
        }
    }

    private static Object handleRequestBody(Object originalRequest) throws Exception {
        Object requestBody = originalRequest.getClass().getMethod("body").invoke(originalRequest);
        if (requestBody == null) {
            return originalRequest;
        }

        // 获取请求体元数据
        Object contentType = requestBody.getClass().getMethod("contentType").invoke(requestBody);
        long contentLength = (long) requestBody.getClass().getMethod("contentLength").invoke(requestBody);

        // 跳过过大请求体
        if (contentLength > MAX_BODY_SIZE) {
            System.out.println("  Request Body - Skipped: too large (" + contentLength + " bytes)");
            return originalRequest;
        }

        // 读取原始请求体内容
        Object source = requestBody.getClass().getMethod("source").invoke(requestBody);
        Class<?> bufferClass = Class.forName("okio.Buffer");
        Object buffer = bufferClass.newInstance();
        source.getClass().getMethod("readAll", bufferClass).invoke(source, buffer);
        String content = buffer.getClass().getMethod("readUtf8").invoke(buffer).toString();

        // 重建请求体
        Class<?> requestBodyClass = Class.forName("okhttp3.RequestBody");
        Object newRequestBody = requestBodyClass.getMethod("create",
                Class.forName("okhttp3.MediaType"),
                String.class)
                .invoke(null, contentType, content);

        // 重建请求
        return originalRequest.getClass().getMethod("newBuilder").invoke(originalRequest)
                .getClass().getMethod("method", String.class, requestBodyClass)
                .invoke(null,
                        originalRequest.getClass().getMethod("method").invoke(originalRequest),
                        newRequestBody);
    }

    private static Object handleResponseBody(Object originalResponse) throws Exception {
        Object responseBody = originalResponse.getClass().getMethod("body").invoke(originalResponse);
        if (responseBody == null) {
            return originalResponse;
        }

        // 获取响应体元数据
        Object contentType = responseBody.getClass().getMethod("contentType").invoke(responseBody);
        long contentLength = (long) responseBody.getClass().getMethod("contentLength").invoke(responseBody);

        // 跳过过大响应体
        if (contentLength > MAX_BODY_SIZE) {
            System.out.println("  Response Body - Skipped: too large (" + contentLength + " bytes)");
            return originalResponse;
        }

        // 读取原始响应体内容
        Object source = responseBody.getClass().getMethod("source").invoke(responseBody);
        Class<?> bufferClass = Class.forName("okio.Buffer");
        Object buffer = bufferClass.newInstance();
        source.getClass().getMethod("readAll", bufferClass).invoke(source, buffer);
        String content = buffer.getClass().getMethod("readUtf8").invoke(buffer).toString();

        // 重建响应体
        Class<?> responseBodyClass = Class.forName("okhttp3.ResponseBody");
        Object newResponseBody = responseBodyClass.getMethod("create",
                Class.forName("okhttp3.MediaType"),
                long.class,
                Class.forName("okio.BufferedSource"))
                .invoke(null, contentType, contentLength, buffer);

        // 重建响应
        return originalResponse.getClass().getMethod("newBuilder").invoke(originalResponse)
                .getClass().getMethod("body", responseBodyClass)
                .invoke(newResponseBody);
    }

    private static void logRequestInfo(Object request) throws Exception {
        System.out.println("[OkHttp] Request:");
        System.out.println("  URL: " + invokeMethod(request, "url"));
        System.out.println("  Method: " + invokeMethod(request, "method"));
        System.out.println("  Headers: " + headersToString(invokeMethod(request, "headers")));

        Object body = invokeMethod(request, "body");
        if (body != null) {
            System.out.println("  Body - Type: " + invokeMethod(body, "contentType"));
            System.out.println("  Body - Length: " + invokeMethod(body, "contentLength"));

            // 对于文本内容直接打印
            if (invokeMethod(body, "contentType").toString().contains("text") ||
                    invokeMethod(body, "contentType").toString().contains("json") ||
                    invokeMethod(body, "contentType").toString().contains("xml")) {
                String content = (String) body.getClass().getMethod("content").invoke(body);
                System.out.println("  Body - Content: " + content);
            }
        }
    }

    private static void logResponseInfo(Object response) throws Exception {
        System.out.println("[OkHttp] Response:");
        System.out.println("  Code: " + invokeMethod(response, "code"));
        System.out.println("  Message: " + invokeMethod(response, "message"));
        System.out.println("  Headers: " + headersToString(invokeMethod(response, "headers")));

        Object body = invokeMethod(response, "body");
        if (body != null) {
            System.out.println("  Body - Type: " + invokeMethod(body, "contentType"));
            System.out.println("  Body - Length: " + invokeMethod(body, "contentLength"));

            // 对于文本内容直接打印
            if (invokeMethod(body, "contentType").toString().contains("text") ||
                    invokeMethod(body, "contentType").toString().contains("json") ||
                    invokeMethod(body, "contentType").toString().contains("xml")) {
                Object source = body.getClass().getMethod("source").invoke(body);
                String content = source.getClass().getMethod("readUtf8").invoke(source).toString();
                System.out.println("  Body - Content: " + content);
            }
        }
    }

    private static Object invokeMethod(Object obj, String methodName) throws Exception {
        if (obj == null)
            return null;
        return obj.getClass().getMethod(methodName).invoke(obj);
    }

    private static String headersToString(Object headers) throws Exception {
        if (headers == null)
            return "null";

        StringBuilder sb = new StringBuilder("[");
        int size = (int) headers.getClass().getMethod("size").invoke(headers);

        for (int i = 0; i < size; i++) {
            String name = (String) headers.getClass().getMethod("name", int.class).invoke(headers, i);
            String value = (String) headers.getClass().getMethod("value", int.class).invoke(headers, i);
            if (i > 0)
                sb.append(", ");
            sb.append(name).append(": ").append(value);
        }

        return sb.append("]").toString();
    }

}
