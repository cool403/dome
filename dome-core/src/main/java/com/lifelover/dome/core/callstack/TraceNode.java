package com.lifelover.dome.core.callstack;

import java.util.ArrayList;
import java.util.List;

public class TraceNode {
    private final String className;

    private final String methodName;

    private final Object[] args;

    private final long startTime;

    private final List<TraceNode> children = new ArrayList<>();

    private Object result;

    private Throwable exception;

    private long endTime;

    private TraceNode parent;

    public TraceNode(String className, String methodName, Object[] args) {
        this.className = className;
        this.methodName = methodName;
        this.args = args;
        this.startTime = System.currentTimeMillis();
    }

    public String getMethodSignature() {
        return className + "." + methodName + "()";
    }

    public long getDuration() {
        return endTime - startTime;
    }

    public void addChild(TraceNode child) {
        children.add(child);
        // child.parent = this;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public Object[] getArgs() {
        return args;
    }

    public List<TraceNode> getChildren() {
        return children;
    }

    public Throwable getException() {
        return exception;
    }

    public TraceNode getParent() {
        return parent;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    public void setParent(TraceNode parent) {
        this.parent = parent;
    }

    


    

}
