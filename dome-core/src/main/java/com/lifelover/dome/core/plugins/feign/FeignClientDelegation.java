package com.lifelover.dome.core.plugins.feign;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.lifelover.dome.core.config.AgentConfig;
import com.lifelover.dome.core.config.ConfigLoader;
import com.lifelover.dome.core.helpers.ClassNames;
import com.lifelover.dome.core.helpers.MethodNames;
import com.lifelover.dome.core.helpers.ReflectMethods;
import com.lifelover.dome.core.helpers.StreamUtils;
import com.lifelover.dome.core.helpers.TargetAppClassRegistry;

public class FeignClientDelegation {

    @Advice.OnMethodEnter()
    public static void onMethodEnter(
            @Advice.Argument(readOnly = false, value = 0, typing = Assigner.Typing.DYNAMIC) Object request,
            @Advice.Argument(readOnly = false, value = 1, typing = Assigner.Typing.DYNAMIC) Object response)
            throws Exception {
        try {
            // 从目标应用加载包装类
            Class<?> responseWrapperClass = TargetAppClassRegistry
                    .getClass(ClassNames.CONTENT_CACHING_RESPONSE_WRAPPER_CLASS_NAME);
            Class<?> requestWrapperClass = TargetAppClassRegistry
                    .getClass(ClassNames.CONTENT_CACHING_REQUEST_WRAPPER_CLASS_NAME);
            if (responseWrapperClass == null || requestWrapperClass == null) {
                return;
            }

            // 包装请求和响应
            if (!request.getClass().isAssignableFrom(requestWrapperClass)) {
                Object wrappedRequest = requestWrapperClass
                        .getConstructor(TargetAppClassRegistry.getClass(ClassNames.HTTP_REQUEST_CLASS_NAME))
                        .newInstance(request);
                request = wrappedRequest;
            }
            if (!response.getClass().isAssignableFrom(responseWrapperClass)) {
                Object wrappedResponse = responseWrapperClass
                        .getConstructor(TargetAppClassRegistry.getClass(ClassNames.HTTP_RESPONSE_CLASS_NAME))
                        .newInstance(response);
                response = wrappedResponse;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to wrap Feign request/response: " + e.getMessage());
        }
    }

    @Advice.OnMethodExit()
    public static void onMethodExit(
            @Advice.Argument(readOnly = false, value = 0, typing = Assigner.Typing.DYNAMIC) Object request,
            @Advice.Argument(readOnly = false, value = 1, typing = Assigner.Typing.DYNAMIC) Object response)
            throws Exception {
        AgentConfig agentConfig = ConfigLoader.getAgentConfig();
        String requestBody = null;
        String responseBodyStr = null;
        try {
            // 获取请求信息
            final String requestUri = (String) ReflectMethods
                    .getMethod(request.getClass(), MethodNames.GET_REQUEST_URI_METHOD)
                    .invoke(request);
            String contentType = (String) ReflectMethods
                    .getMethod(request.getClass(), MethodNames.GET_CONTENT_TYPE_METHOD)
                    .invoke(request);

            // 检查是否需要忽略
            boolean shouldIgnore = false;
            for (String ignoreUrl : agentConfig.getIgnoreUrls()) {
                if (requestUri.contains(ignoreUrl)) {
                    shouldIgnore = true;
                    break;
                }
            }
            if (shouldIgnore) {
                return;
            }

            // 检查 HTTP 方法
            String httpMethod = (String) ReflectMethods
                    .getMethod(request.getClass(), MethodNames.GET_METHOD).invoke(request);
            if (!agentConfig.getSupportMethods().contains(httpMethod)) {
                return;
            }

            // 检查响应状态
            final int httpStatus = (int) ReflectMethods
                    .getMethod(response.getClass(), MethodNames.GET_STATUS_METHOD)
                    .invoke(response);
            if (httpStatus >= 400) {
                System.out.println("Feign request uri: " + requestUri + ", http status:" + httpStatus);
                return;
            }

            // 处理文件上传请求
            if (contentType != null && contentType.startsWith("multipart/")) {
                return;
            }

            // 获取请求体
            byte[] content1 = (byte[]) ReflectMethods
                    .getMethod(request.getClass(), MethodNames.GET_CONTENT_AS_BYTE_ARRAY_METHOD)
                    .invoke(request);
            requestBody = new String(content1);
            if (requestBody.isEmpty()) {
                InputStream is = (InputStream) ReflectMethods
                        .getMethod(request.getClass(), MethodNames.GET_IS_METHOD)
                        .invoke(request);
                requestBody = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
            }

            // 获取响应体
            byte[] content = (byte[]) ReflectMethods
                    .getMethod(response.getClass(), MethodNames.GET_CONTENT_AS_BYTE_ARRAY_METHOD)
                    .invoke(response);
            responseBodyStr = new String(content);

            // 输出请求和响应信息
            System.out.println(
                    "Feign request uri: " + requestUri + ", request body:" + requestBody
                            + ", response data: " + responseBodyStr);
        } catch (Exception e) {
            System.out.println("intercept on Feign request error: " + e.getMessage());
        } finally {
            // 确保响应体被复制到响应中
            ReflectMethods.getMethod(response.getClass(), MethodNames.COPY_BODY_TO_RESPONSE_METHOD)
                    .invoke(response);
        }
    }
} 