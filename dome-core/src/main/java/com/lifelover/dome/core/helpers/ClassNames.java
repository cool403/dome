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
    public static final String REAL_CALL_CLASS_NAME = "okhttp3.Call";
    public static final String BUFFER_CLASS_NAME = "okio.Buffer";
    public static final String MEDIA_TYPE_CLASS_NAME = "okhttp3.MediaType";
}
