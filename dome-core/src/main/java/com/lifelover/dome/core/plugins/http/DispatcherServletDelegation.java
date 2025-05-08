package com.lifelover.dome.core.plugins.http;

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

public class DispatcherServletDelegation {

        @Advice.OnMethodEnter()
        public static void onMethodEnter(
                        @Advice.Argument(readOnly = false, value = 0, typing = Assigner.Typing.DYNAMIC) Object request,
                        @Advice.Argument(readOnly = false, value = 1, typing = Assigner.Typing.DYNAMIC) Object response)
                        throws Exception {
                // Check if response is not null and is an instance of HttpServletResponse
                try {
                        // 从目标应用加载ContentCachingResponseWrapper类、ContentCachingRequestWrapper类，避免agent依赖
                        Class<?> responseWrapperClass = TargetAppClassRegistry
                                        .getClass(ClassNames.CONTENT_CACHING_RESPONSE_WRAPPER_CLASS_NAME);
                        // Get the ContentCachingRequestWrapper class
                        Class<?> requestWrapperClass = TargetAppClassRegistry
                                        .getClass(ClassNames.CONTENT_CACHING_REQUEST_WRAPPER_CLASS_NAME);
                        if (responseWrapperClass == null || requestWrapperClass == null) {
                                return;
                        }
                        // 分别包装HttpServletRequest和HttpServletResponse
                        // 这里需要注意，有可能这个时候的request和response已经被包装过了，就不需要重复进行wrapper了
                        if (!request.getClass().isAssignableFrom(requestWrapperClass)) {
                                Object wrappedRequest = requestWrapperClass
                                                .getConstructor(TargetAppClassRegistry
                                                                .getClass(ClassNames.HTTP_REQUEST_CLASS_NAME))
                                                .newInstance(request);
                                request = wrappedRequest;
                        }
                        if (!response.getClass().isAssignableFrom(responseWrapperClass)) {
                                Object wrappedResponse = responseWrapperClass
                                                .getConstructor(TargetAppClassRegistry
                                                                .getClass(ClassNames.HTTP_RESPONSE_CLASS_NAME))
                                                .newInstance(response);
                                response = wrappedResponse;
                        }

                } catch (Exception e) {
                        // Log the error but do not interrupt execution
                        e.printStackTrace();
                        System.err.println("Failed to wrap response: " + e.getMessage());
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
                        // 获取请求路径
                        final String requestUri = (String) ReflectMethods
                                        .getMethod(request.getClass(), MethodNames.GET_REQUEST_URI_METHOD)
                                        .invoke(request);
                        String contentType = (String)ReflectMethods.getMethod(request.getClass(), MethodNames.GET_CONTENT_TYPE_METHOD).invoke(request);
                        // 可支持忽略哪些路径不采集,默认/error
                        if (agentConfig.getIgnoreUrls().stream().anyMatch(it -> requestUri.contains(it))) {
                                return;
                        }
                        // 忽略特定的方法
                        String httpMethod = (String) ReflectMethods
                                        .getMethod(request.getClass(), MethodNames.GET_METHOD).invoke(request);
                        if (!agentConfig.getSupportMethods().contains(httpMethod)) {
                                return;
                        }
                        final int httpStatus = (int) ReflectMethods
                                        .getMethod(response.getClass(), MethodNames.GET_STATUS_METHOD)
                                        .invoke(response);
                        if (httpStatus >= 400) {
                                System.out.println("request uri: " + requestUri + ", http status:" + httpStatus);
                                return;
                        }
                        //上传请求200 情况 下不做特殊处理
                        if (contentType.startsWith("multipart/")) {
                                return;
                        }
                        // Check if request is not null and is an instance of HttpServletRequest
                        byte[] content1 = (byte[]) ReflectMethods
                                        .getMethod(request.getClass(), MethodNames.GET_CONTENT_AS_BYTE_ARRAY_METHOD)
                                        .invoke(request);
                        requestBody = new String(content1);
                        // 这里考虑到有可能后面方法并没有读取inputStream,导致无法争取取道输入，这里再手工读取下
                        if (requestBody.isEmpty()) {
                                InputStream is = (InputStream) ReflectMethods
                                                .getMethod(request.getClass(), MethodNames.GET_IS_METHOD)
                                                .invoke(request);
                                requestBody = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
                        }
                        // Check if response is not null and is an instance of HttpServletResponse
                        byte[] content = (byte[]) ReflectMethods
                                        .getMethod(response.getClass(), MethodNames.GET_CONTENT_AS_BYTE_ARRAY_METHOD)
                                        .invoke(response);
                        responseBodyStr = new String(content);
                        System.out.println(
                                        "request uri: " + requestUri + ",request body:" + requestBody
                                                        + ",response data: "
                                                        + responseBodyStr);
                } catch (Exception e) {
                        System.out.println("intercept on doDispatch error." + e.getMessage());
                } finally {
                        // Copy the response body to the response
                        ReflectMethods.getMethod(response.getClass(), MethodNames.COPY_BODY_TO_RESPONSE_METHOD)
                                        .invoke(response);
                }
        }
}
