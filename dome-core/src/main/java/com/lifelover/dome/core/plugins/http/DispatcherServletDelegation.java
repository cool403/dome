package com.lifelover.dome.core.plugins.http;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.lifelover.dome.core.config.AgentConfig;
import com.lifelover.dome.core.config.ConfigLoader;
import com.lifelover.dome.core.helpers.ClassNames;
import com.lifelover.dome.core.helpers.MethodNames;
import com.lifelover.dome.core.helpers.ReflectMethods;
import com.lifelover.dome.core.helpers.StreamUtils;
import com.lifelover.dome.core.helpers.TargetAppClassRegistry;
import com.lifelover.dome.core.report.EventReporterHolder;
import com.lifelover.dome.core.report.HttpMetricsEvent;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

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
                                request = requestWrapperClass
                                                .getConstructor(TargetAppClassRegistry
                                                                .getClass(ClassNames.HTTP_REQUEST_CLASS_NAME))
                                                .newInstance(request);
                        }
                        if (!response.getClass().isAssignableFrom(responseWrapperClass)) {
                                response = responseWrapperClass
                                                .getConstructor(TargetAppClassRegistry
                                                                .getClass(ClassNames.HTTP_RESPONSE_CLASS_NAME))
                                                .newInstance(response);
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
                // 先这样写，后续根据不同应用的赋值方式区
                String traceId = UUID.randomUUID().toString();
                try {
                        // 获取请求路径
                        final String requestUri = (String) ReflectMethods
                                        .getMethod(request.getClass(), MethodNames.GET_REQUEST_URI_METHOD)
                                        .invoke(request);
                        String contentType = (String) ReflectMethods
                                        .getMethod(request.getClass(), MethodNames.GET_CONTENT_TYPE_METHOD)
                                        .invoke(request);
                        // 可支持忽略哪些路径不采集,默认/error
                        boolean shouldIgnore = false;
                        for (String ignoreUrl : agentConfig.getIgnoreUrls()) {
                                if (requestUri.contains(ignoreUrl)) {
                                        shouldIgnore = true;
                                        break;
                                }
                        }
                        // 忽略特定的方法
                        String httpMethod = (String) ReflectMethods
                                        .getMethod(request.getClass(), MethodNames.GET_METHOD).invoke(request);
                        shouldIgnore = shouldIgnore || !agentConfig.getSupportMethods().contains(httpMethod);
                        if (shouldIgnore) {
                                return;
                        }
                        final int httpStatus = (int) ReflectMethods
                                        .getMethod(response.getClass(), MethodNames.GET_STATUS_METHOD)
                                        .invoke(response);
                        // 获取queryString
                        final String queryStringParams = (String) ReflectMethods
                                        .getMethod(request.getClass(), MethodNames.GET_QUERY_STRING_METHOD)
                                        .invoke(request);
                        if (httpStatus >= 400) {
                                // System.out.println("request uri: " + requestUri + ", http status:" +
                                // httpStatus);
                                // 状态码不对的也需要记录输入内容
                                requestBody = getRequestBody(request, httpMethod);
                                HttpMetricsEvent metricsEvent = new HttpMetricsEvent();
                                metricsEvent.setHttpStatus(httpStatus + "");
                                metricsEvent.setHttpMethod(httpMethod);
                                metricsEvent.setHttpUrl(requestUri);
                                metricsEvent.setQueryParams(queryStringParams);
                                metricsEvent.setReqTime(System.currentTimeMillis());
                                metricsEvent.setRequestBody(requestBody);
                                metricsEvent.setTraceId(traceId);
                                EventReporterHolder.getEventReporter().asyncReport(metricsEvent);
                                return;
                        }
                        // 上传请求200 情况 下不做特殊处理
                        // 部分情况下contentType可能为空
                        if (contentType != null && contentType.startsWith("multipart/")) {
                                return;
                        }
                        responseBodyStr = getResponseBody(response);
                        requestBody = getRequestBody(request, httpMethod);
                        HttpMetricsEvent metricsEvent = new HttpMetricsEvent();
                        metricsEvent.setHttpStatus(httpStatus + "");
                        metricsEvent.setHttpMethod(httpMethod);
                        metricsEvent.setHttpUrl(requestUri);
                        metricsEvent.setReqTime(System.currentTimeMillis());
                        metricsEvent.setQueryParams(queryStringParams);
                        metricsEvent.setRequestBody(requestBody);
                        metricsEvent.setResponseBody(responseBodyStr);
                        metricsEvent.setTraceId(traceId);
                        EventReporterHolder.getEventReporter().asyncReport(metricsEvent);
                } catch (Exception e) {
                        System.out.println("intercept on doDispatch error." + e.getMessage());
                } finally {
                        // Copy the response body to the response
                        ReflectMethods.getMethod(response.getClass(), MethodNames.COPY_BODY_TO_RESPONSE_METHOD)
                                        .invoke(response);
                }
        }

        /**
         * 
         * @param request
         * @param httpMethod
         * @return
         * @throws Exception
         */
        public static String getRequestBody(Object request, String httpMethod) throws Exception {
                // post才有输入
                if (!"POST".equals(httpMethod)) {
                        return "";
                }
                String requestBody = null;
                // Check if request is not null and is an instance of HttpServletRequest
                byte[] content1 = (byte[]) ReflectMethods
                                .getMethod(request.getClass(), MethodNames.GET_CONTENT_AS_BYTE_ARRAY_METHOD)
                                .invoke(request);
                if (content1 == null || content1.length == 0) {
                        return null;
                }
                requestBody = new String(content1);
                // 这里考虑到有可能后面方法并没有读取inputStream,导致无法争取取道输入，这里再手工读取下
                if (requestBody.isEmpty()) {
                        InputStream is = (InputStream) ReflectMethods
                                        .getMethod(request.getClass(), MethodNames.GET_IS_METHOD)
                                        .invoke(request);
                        requestBody = StreamUtils.copyToString(is, StandardCharsets.UTF_8);
                }
                return requestBody;
        }

        /**
         * 
         * @param response
         * @return
         * @throws Exception
         */
        public static String getResponseBody(Object response) throws Exception {
                // Check if response is not null and is an instance of HttpServletResponse
                byte[] content = (byte[]) ReflectMethods
                                .getMethod(response.getClass(), MethodNames.GET_CONTENT_AS_BYTE_ARRAY_METHOD)
                                .invoke(response);
                if (content == null || content.length == 0) {
                        return null;
                }
                return new String(content);
        }
}
