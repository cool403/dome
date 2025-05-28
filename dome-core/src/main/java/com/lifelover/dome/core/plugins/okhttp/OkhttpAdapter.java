package com.lifelover.dome.core.plugins.okhttp;


public interface OkhttpAdapter {
    
    /**
     * 
     * @param call
     */
    void beforeCall(Object call);

    /**
     * 
     * @param response
     */
    Object afterCall(Object response, Throwable throwable);

}
