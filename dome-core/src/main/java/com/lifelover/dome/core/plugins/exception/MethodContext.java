package com.lifelover.dome.core.plugins.exception;

public class MethodContext {

    private final String methodName;
    private final Object[] arguments;

    public MethodContext(String methodName, Object[] arguments) {
        this.methodName = methodName;
        this.arguments = arguments != null ? arguments.clone() : null;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getArguments() {
        return arguments;
    }
}
