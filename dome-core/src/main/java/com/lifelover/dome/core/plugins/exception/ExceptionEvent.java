package com.lifelover.dome.core.plugins.exception;

import java.util.Arrays;

public class ExceptionEvent {
    private final String methodName;
    private final Object[] arguments;
    private final Throwable exception;
    private final String threadName;
    private final long timestamp;
    
    public ExceptionEvent(String methodName, Object[] arguments, 
                         Throwable exception, String threadName, long timestamp) {
        this.methodName = methodName;
        this.arguments = arguments != null ? arguments.clone() : null;
        this.exception = exception;
        this.threadName = threadName;
        this.timestamp = timestamp;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public Object[] getArguments() {
        return arguments;
    }
    
    public Throwable getException() {
        return exception;
    }
    
    public String getThreadName() {
        return threadName;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return "ExceptionEvent{" +
                "methodName='" + methodName + '\'' +
                ", arguments=" + Arrays.toString(arguments) +
                ", exception=" + exception +
                ", threadName='" + threadName + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}