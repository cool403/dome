package com.lifelover.dome.core.plugins.resttemplate;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.lifelover.dome.core.helpers.ClassNames;
import com.lifelover.dome.core.helpers.ClassloaderRegistry;
import com.lifelover.dome.core.helpers.TargetAppClassRegistry;

public class DynamicResponseInstanceBuilder {

    // 动态加载Spring的HttpStatus类
    public static Object loadHttpStatus(int statusCode) throws Exception {
        Class<?> httpStatusClass = TargetAppClassRegistry.getClass(ClassNames.RT_HTTP_STATUS_CLASS_NAME);
        Method valueOfMethod = httpStatusClass.getMethod("valueOf", int.class);
        return valueOfMethod.invoke(null, statusCode);
    }

    // 动态加载Spring的HttpHeaders类
    public static Object createHttpHeaders() throws Exception {
        Class<?> httpHeadersClass = TargetAppClassRegistry.getClass(ClassNames.RT_HTTP_HEADER_CLASS_NAME);
        return httpHeadersClass.getDeclaredConstructor().newInstance();
    }

    // 动态加载Spring的MediaType类
    public static Object getMediaType(String type) throws Exception {
        Class<?> mediaTypeClass = TargetAppClassRegistry.getClass(ClassNames.RT_MEDIA_TYPE_CLASS_NAME);
        Method valueOfMethod = mediaTypeClass.getMethod("valueOf", String.class);
        return valueOfMethod.invoke(null, type);
    }

    // 动态创建ClientHttpResponse实现
    public static Object createClientHttpResponse(String body, int statusCode, String contentType) throws Exception {
        // 加载必要的Spring类
        Object httpStatus = loadHttpStatus(statusCode);
        Object httpHeaders = createHttpHeaders();
        Object mediaType = getMediaType(contentType);

        // 设置headers
        Method setContentType = httpHeaders.getClass().getMethod("setContentType", mediaType.getClass());
        setContentType.invoke(httpHeaders, mediaType);

        // 创建动态代理实现ClientHttpResponse接口
        Class<?> clientHttpResponseClass = TargetAppClassRegistry.getClass(ClassNames.RT_BASIC_RESPONSE_CLASS_NAME);
        return Proxy.newProxyInstance(ClassloaderRegistry.getTargetAppClassLoader(),
                new Class[] { clientHttpResponseClass }, (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "getStatusCode":
                            return httpStatus;
                        case "getRawStatusCode":
                            return statusCode;
                        case "getStatusText":
                            return httpStatus.getClass().getMethod("getReasonPhrase").invoke(httpStatus);
                        case "getBody":
                            return new java.io.ByteArrayInputStream(body.getBytes());
                        case "getHeaders":
                            return httpHeaders;
                        case "close":
                            return null;
                        default:
                            throw new UnsupportedOperationException();
                    }
                });
    }
}