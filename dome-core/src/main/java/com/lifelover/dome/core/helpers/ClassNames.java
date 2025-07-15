package com.lifelover.dome.core.helpers;

public class ClassNames {
    private ClassNames() {
    }

    /////////////spring//////////
    public static final String DISPATCHER_SERVLET_CLASS_NAME = "org.springframework.web.servlet.DispatcherServlet";
    public static final String HTTP_REQUEST_CLASS_NAME = "javax.servlet.http.HttpServletRequest";
    public static final String HTTP_RESPONSE_CLASS_NAME = "javax.servlet.http.HttpServletResponse";
    public static final String SERVLET_OUTPUT_STREAM_CLASS_NAME = "javax.servlet.ServletOutputStream";

    public static final String CONTENT_CACHING_RESPONSE_WRAPPER_CLASS_NAME = "org.springframework.web.util.ContentCachingResponseWrapper";
    public static final String CONTENT_CACHING_REQUEST_WRAPPER_CLASS_NAME = "org.springframework.web.util.ContentCachingRequestWrapper";


    ////////////okhttp////////
    public static final String REAL_CALL_CLASS_NAME = "okhttp3.internal.connection.RealCall";
    public static final String BUFFER_CLASS_NAME = "okio.Buffer";
    public static final String MEDIA_TYPE_CLASS_NAME = "okhttp3.MediaType";
    public static final String RESPONSE_CLASS_NAME = "okhttp3.Response";
    public static final String RESPONSE_BODY_CLASS_NAME = "okhttp3.ResponseBody";
    public static final String OKIO_BUFFERED_SINK_CLASS_NAME = "okio.BufferedSink";
    public static final String OK_RESPONSE_BUILDER_CLASS_NAME = "okhttp3.Response$Builder";
    public static final String OK_REQUEST_CLASS_NAME = "okhttp3.Request";
    public static final String OK_PROTOCOL_CLASS_NAME = "okhttp3.Protocol";


    /////////feign////////////
    public static final String FEIGN_CLIENT_CLASS_NAME="feign.Client";
    public static final String FEIGN_RESPONSE_BUILDER_CLASS_NAME = "feign.Response$Builder";
    public static final String FEIGN_OKHTTP_CLIENT_CLASS_NAME = "feign.okhttp.OkHttpClient";



    ////////resttemplate//////
    public static final String RT_REQUEST_CLASS_NAME="org.springframework.http.client.AbstractBufferingClientHttpRequest";
    public static final String RT_HTTP_HEADER_CLASS_NAME="org.springframework.http.HttpHeaders";
    public static final String RT_BUFFER_REQUEST_CLASS_NAME="org.springframework.http.client.BufferingClientHttpRequestWrapper";
    public static final String RT_BUFFER_RESPONSE_CLASS_NAME="org.springframework.http.client.BufferingClientHttpResponseWrapper";
    public static final String RT_BASIC_RESPONSE_CLASS_NAME="org.springframework.http.client.ClientHttpResponse";
    public static final String RT_MOCK_RESPONSE_CLASS_NAME="org.springframework.mock.http.client.MockClientHttpResponse";
    public static final String RT_HTTP_STATUS_CLASS_NAME="org.springframework.http.HttpStatus";
    public static final String RT_MEDIA_TYPE_CLASS_NAME="org.springframework.http.MediaType";
}
