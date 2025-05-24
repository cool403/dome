package com.lifelover.dome.core.plugins.okhttp;

import com.lifelover.dome.core.helpers.TargetAppClassRegistry;

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
