package com.lifelover.dome.core.callstack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import net.bytebuddy.asm.Advice.AllArguments;
import net.bytebuddy.asm.Advice.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

public class TraceInterceptor {
    public static final List<String> IGNORE_METHODS = new ArrayList<>();

    static {
        IGNORE_METHODS.add("equals");
        IGNORE_METHODS.add("toString");
        IGNORE_METHODS.add("hashCode");
        IGNORE_METHODS.add("getClass");
        IGNORE_METHODS.add("wait");
        IGNORE_METHODS.add("notify");
        IGNORE_METHODS.add("notifyAll");
        IGNORE_METHODS.add("finalize");
    }

    @RuntimeType
    public static Object intercept(@Origin Method method, @AllArguments Object[] args,
            @SuperCall Callable<?> callable) throws Exception {
        final String className = method.getDeclaringClass().getName();
        final String methodName = method.getName();
        // 忽略get方法,不带参数
        if (args.length == 0 && methodName.startsWith("get")) {
            return callable.call();
        }
        // 忽略set方法,带一个参数
        if (args.length == 1 && methodName.startsWith("set")) {
            return callable.call();
        }
        // 忽略常见的方法
        if (IGNORE_METHODS.contains(methodName)) {
            return callable.call();
        }
        // 启动方法，默认是controller接口
        final boolean isStartMethod = className.contains("Controller");
        if (isStartMethod) {
            TraceContext.startTrace();
        }
        //未开启trace,直接返回
        if (TraceContext.isTracingActive() == false) {
            return callable.call();
        }
        //记录方法调用
        final TraceNode node = TraceContext.enterMethod(className, methodName, args);
        try {
            Object result = callable.call();
            node.setResult(result);
            return result;
        } catch (Exception e) {
            TraceContext.catchException(e);
            throw e;
        } finally {
            TraceContext.exitMethod(node);
        }
        
    }
}
