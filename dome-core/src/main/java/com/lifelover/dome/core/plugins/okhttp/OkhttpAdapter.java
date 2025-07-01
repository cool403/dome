package com.lifelover.dome.core.plugins.okhttp;


public interface OkhttpAdapter {
    
    /**
     * 
     * @param call
     * @return 
     */
    Object beforeCall(Object call);

    /**
     * 
     * @param response
     */
    Object afterCall(Object response, Throwable throwable);

}
