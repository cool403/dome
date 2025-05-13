package com.lifelover.dome.core.plugins.http;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.lifelover.dome.core.config.AgentConfig;
import com.lifelover.dome.core.config.ConfigLoader;
import com.lifelover.dome.core.helpers.ClassNames;
import com.lifelover.dome.core.helpers.HeaderNames;
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
                        // 记录请求时间
                        long reqTime = System.currentTimeMillis();
                        ReflectMethods.invokeMethod(request.getClass(), MethodNames.SET_ATTR_METHOD,
                                        new Class[] { String.class, Object.class }, request, HeaderNames.X_REQUEST_TIME,
                                        reqTime);
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
                Class<?> requestClass = request.getClass();
                Class<?> responseClass = response.getClass();

                // 先这样写，后续根据不同应用的赋值方式区
                String traceId = UUID.randomUUID().toString();
                try {
                        // 获取请求时间
                        Long reqTime = ReflectMethods.invokeMethod(requestClass, MethodNames.GET_ATTR_METHOD,
                                        new Class[] { String.class }, request, HeaderNames.X_REQUEST_TIME);
                        // 获取请求路径
                        String requestUri = ReflectMethods.invokeMethod(requestClass,
                                        MethodNames.GET_REQUEST_URI_METHOD, request);
                        String contentType = ReflectMethods.invokeMethod(requestClass,
                                        MethodNames.GET_CONTENT_TYPE_METHOD, request);
                        // 可支持忽略哪些路径不采集,默认/error
                        boolean shouldIgnore = false;
                        for (String ignoreUrl : agentConfig.getIgnoreUrls()) {
                                if (requestUri.contains(ignoreUrl)) {
                                        shouldIgnore = true;
                                        break;
                                }
                        }
                        // 忽略特定的方法
                        String httpMethod = ReflectMethods.invokeMethod(requestClass, MethodNames.GET_METHOD, request);
                        shouldIgnore = shouldIgnore || !agentConfig.getSupportMethods().contains(httpMethod);
                        if (shouldIgnore) {
                                return;
                        }
                        int httpStatus = ReflectMethods.invokeMethod(responseClass, MethodNames.GET_STATUS_METHOD,
                                        response);
                        // 获取queryString
                        String queryStringParams = ReflectMethods.invokeMethod(requestClass,
                                        MethodNames.GET_QUERY_STRING_METHOD, request);
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
                                metricsEvent.setReqTime(reqTime);
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
                        metricsEvent.setReqTime(reqTime);
                        metricsEvent.setQueryParams(queryStringParams);
                        metricsEvent.setRequestBody(requestBody);
                        metricsEvent.setResponseBody(responseBodyStr);
                        metricsEvent.setTraceId(traceId);
                        EventReporterHolder.getEventReporter().asyncReport(metricsEvent);
                } catch (Exception e) {
                        System.out.println("intercept on doDispatch error." + e.getMessage());
                } finally {
                        // Copy the response body to the response
                        ReflectMethods.invokeMethod(responseClass, MethodNames.COPY_BODY_TO_RESPONSE_METHOD, response);
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
                // Check if request is not null and is an instance of HttpServletRequest
                byte[] bytes = ReflectMethods.invokeMethod(request.getClass(),
                                MethodNames.GET_CONTENT_AS_BYTE_ARRAY_METHOD, request);
                if (bytes != null && bytes.length > 0) {
                        return new String(bytes);
                }
                // 这里考虑到有可能后面方法并没有读取inputStream,导致无法争取取道输入，这里再手工读取下
                InputStream is = ReflectMethods.invokeMethod(request.getClass(), MethodNames.GET_IS_METHOD,
                                request);
                return StreamUtils.copyToString(is, StandardCharsets.UTF_8);
        }

        /**
         * 
         * @param response
         * @return
         * @throws Exception
         */
        public static String getResponseBody(Object response) throws Exception {
                // Check if response is not null and is an instance of HttpServletResponse
                byte[] content = ReflectMethods.invokeMethod(response.getClass(),
                                MethodNames.GET_CONTENT_AS_BYTE_ARRAY_METHOD, response);
                if (content == null || content.length == 0) {
                        return null;
                }
                return new String(content);
        }
}
