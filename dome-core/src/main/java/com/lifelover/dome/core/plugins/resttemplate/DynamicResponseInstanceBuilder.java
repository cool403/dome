package com.lifelover.dome.core.plugins.resttemplate;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.lifelover.dome.core.helpers.ClassloaderRegistry;

public class DynamicResponseInstanceBuilder {

        // 动态加载Spring的HttpStatus类
    public static Object loadHttpStatus(int statusCode) throws Exception {
        Class<?> httpStatusClass = Class.forName("org.springframework.http.HttpStatus");
        Method valueOfMethod = httpStatusClass.getMethod("valueOf", int.class);
        return valueOfMethod.invoke(null, statusCode);
    }

    // 动态加载Spring的HttpHeaders类
    public static Object createHttpHeaders() throws Exception {
        Class<?> httpHeadersClass = Class.forName("org.springframework.http.HttpHeaders");
        return httpHeadersClass.getDeclaredConstructor().newInstance();
    }

    // 动态加载Spring的MediaType类
    public static Object getMediaType(String type) throws Exception {
        Class<?> mediaTypeClass = Class.forName("org.springframework.http.MediaType");
        Method valueOfMethod = mediaTypeClass.getMethod("valueOf", String.class);
        return valueOfMethod.invoke(null, type);
    }

    // 动态创建ClientHttpResponse实现
    public static Object createClientHttpResponse(
            String body, 
            int statusCode,
            String contentType) throws Exception {
        
        // 加载必要的Spring类
        Object httpStatus = loadHttpStatus(statusCode);
        Object httpHeaders = createHttpHeaders();
        Object mediaType = getMediaType(contentType);
        
        // 设置headers
        Method setContentType = httpHeaders.getClass()
            .getMethod("setContentType", mediaType.getClass());
        setContentType.invoke(httpHeaders, mediaType);
        
        // 创建动态代理实现ClientHttpResponse接口
        Class<?> clientHttpResponseClass = Class.forName(
            "org.springframework.http.client.ClientHttpResponse");
        
        return Proxy.newProxyInstance(
            ClassloaderRegistry.getTargetAppClassLoader(),
            new Class[]{clientHttpResponseClass},
            (proxy, method, args) -> {
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
            }
        );
    }
}