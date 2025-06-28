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
import com.lifelover.dome.core.mock.ApiMockContext;
import com.lifelover.dome.core.mock.ApiMockInterceptor;
import com.lifelover.dome.core.report.EventReporterHolder;
import com.lifelover.dome.core.report.HttpMetricsData;
import com.lifelover.dome.core.report.MetricsEvent;

import com.lifelover.dome.db.entity.ApiRecords;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

public class DispatcherServletDelegation {

    public static final ThreadLocal<HttpMetricsData> httpMetricsDataThreadLocal = new ThreadLocal<>();


    /**
     * 拦截器，返回为true，就不会真实调用，为false就会真实调用，适合mock工具开发
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Advice.OnMethodEnter(skipOn = Advice.OnNonDefaultValue.class)
    public static Object onMethodEnter(
            @Advice.Argument(readOnly = false, value = 0, typing = Assigner.Typing.DYNAMIC) Object request,
            @Advice.Argument(readOnly = false, value = 1, typing = Assigner.Typing.DYNAMIC) Object response)
            throws Exception {
        // Check if response is not null and is an instance of HttpServletResponse
        try {
            // 清除threadLocal
            httpMetricsDataThreadLocal.remove();
            Class<?> requestClass = request.getClass();
            // 从目标应用加载ContentCachingResponseWrapper类、ContentCachingRequestWrapper类，避免agent依赖
            Class<?> responseWrapperClass = TargetAppClassRegistry.getClass(ClassNames.CONTENT_CACHING_RESPONSE_WRAPPER_CLASS_NAME);
            // Get the ContentCachingRequestWrapper class
            Class<?> requestWrapperClass = TargetAppClassRegistry.getClass(ClassNames.CONTENT_CACHING_REQUEST_WRAPPER_CLASS_NAME);
            if (responseWrapperClass == null || requestWrapperClass == null) {
                return null;
            }
            String contentType = ReflectMethods.invokeMethod(requestClass, MethodNames.GET_CONTENT_TYPE_METHOD, request);
            String httpMethod = ReflectMethods.invokeMethod(requestClass, MethodNames.GET_METHOD, request);
            // 部分情况下contentType可能为空
            if (contentType != null && contentType.startsWith("multipart/")) {
                return null;
            }
            // 获取请求路径
            String requestUri = ReflectMethods.invokeMethod(requestClass, MethodNames.GET_REQUEST_URI_METHOD, request);
            AgentConfig agentConfig = ConfigLoader.getAgentConfig();
            for (String ignoreUrl : agentConfig.getIgnoreUrls()) {
                if (requestUri.contains(ignoreUrl)) {
                    return null;
                }
            }
            if (!agentConfig.getSupportMethods().contains(httpMethod)) {
                return null;
            }
            //判断是否需要mock
            ApiMockContext apiMockContext = new ApiMockContext();
            apiMockContext.setContentType(contentType);
            apiMockContext.setHttpMethod(httpMethod);
            apiMockContext.setHttpUrl(requestUri);
            ApiRecords apiRecords = ApiMockInterceptor.mock(apiMockContext);
            //只考虑返回也是json
            if (apiRecords != null) {
                System.out.println("[dome agent] http_url="+requestUri+",http_method="+httpMethod+"命中mock,直接返回mock数据");
                Object writer = ReflectMethods.invokeMethod(response.getClass(), "getWriter", response);
                // 返回200
                ReflectMethods.invokeMethod(response.getClass(),"setContentType", new Class[]{String.class}, response,"application/json");
                ReflectMethods.invokeMethod(response.getClass(), "setStatus", new Class[] { int.class }, response, 200);
                // 写入mock succ!!
                ReflectMethods.invokeMethod(writer.getClass(), "write", new Class[] { String.class }, writer,
                        apiRecords.getResponseBody());
                // flush
                ReflectMethods.invokeMethod(writer.getClass(), "flush", writer);
                return true;
            }
            // 分别包装HttpServletRequest和HttpServletResponse
            // 这里需要注意，有可能这个时候的request和response已经被包装过了，就不需要重复进行wrapper了
            if (!requestClass.isAssignableFrom(requestWrapperClass)) {
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

            HttpMetricsData httpMetricsData = new HttpMetricsData();
            httpMetricsData.setReqTime(System.currentTimeMillis());
            httpMetricsData.setHttpUrl(requestUri);
            httpMetricsData.setHttpMethod(httpMethod);
            httpMetricsDataThreadLocal.set(httpMetricsData);
        } catch (Exception e) {
            // Log the error but do not interrupt execution
            System.err.println("[dome agent] Failed to wrap response: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Advice.OnMethodExit()
    public static void onMethodExit(
            @Advice.Enter Object fixedValue,
            @Advice.Argument(readOnly = false, value = 0, typing = Assigner.Typing.DYNAMIC) Object request,
            @Advice.Argument(readOnly = false, value = 1, typing = Assigner.Typing.DYNAMIC) Object response)
            throws Exception {
        if (fixedValue != null) {
            return;
        }
        // 如果threadLocal 没有，什么都不做
        HttpMetricsData metricsData = httpMetricsDataThreadLocal.get();
        if (metricsData == null) {
            return;
        }
        String requestBody;
        String responseBodyStr;
        Class<?> requestClass = request.getClass();
        Class<?> responseClass = response.getClass();

        // 先这样写，后续根据不同应用的赋值方式区
        String traceId = UUID.randomUUID().toString();
        try {
            int httpStatus = ReflectMethods.invokeMethod(responseClass, MethodNames.GET_STATUS_METHOD, response);
            // 获取queryString
            metricsData.setHttpStatus(httpStatus + "");
            metricsData.setMetricType("server");
            metricsData.setTraceId(traceId);
            metricsData.setRespTime(System.currentTimeMillis());
            String queryStringParams = ReflectMethods.invokeMethod(requestClass, MethodNames.GET_QUERY_STRING_METHOD, request);
            metricsData.setQueryParams(queryStringParams);
            if (httpStatus >= 400) {
                // System.out.println("request uri: " + requestUri + ", http status:" +
                // httpStatus);
                // 状态码不对的也需要记录输入内容
                requestBody = getRequestBody(request, metricsData.getHttpMethod());
                metricsData.setRequestBody(requestBody);
                MetricsEvent<HttpMetricsData> event = new MetricsEvent<HttpMetricsData>();
                event.setEventData(metricsData);
                EventReporterHolder.getEventReporter().asyncReport(event);
                return;
            }
            // 上传请求200 情况 下不做特殊处理
            responseBodyStr = getResponseBody(response);
            requestBody = getRequestBody(request, metricsData.getHttpMethod());
            metricsData.setRequestBody(requestBody);
            metricsData.setResponseBody(responseBodyStr);
            MetricsEvent<HttpMetricsData> event = new MetricsEvent<>();
            event.setEventData(metricsData);
            EventReporterHolder.getEventReporter().asyncReport(event);
        } catch (Exception e) {
            System.out.println("[dome agent] intercept on doDispatch error." + e.getMessage());
        } finally {
            // Copy the response body to the response
            ReflectMethods.invokeMethod(responseClass, MethodNames.COPY_BODY_TO_RESPONSE_METHOD, response);
        }
    }

    /**
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
