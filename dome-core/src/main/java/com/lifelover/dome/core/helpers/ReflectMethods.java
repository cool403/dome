package com.lifelover.dome.core.helpers;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectMethods {
    private static final Map<String, Method> methodCache = new ConcurrentHashMap<>();


    public static Method getMethod(Class<?> clazz, String methodName) {
        if (clazz == null) {
            return null;
        }
        final String key = clazz.getName() + ":" + methodName;
        final Method cachedMethod = methodCache.get(key);
        if (cachedMethod != null) {
            return cachedMethod;
        }
        try {
            final Method method = clazz.getMethod(methodName);
            methodCache.put(key, method);
            return method;
        } catch (Exception e) {
            System.err.println("Failed to get method: " + clazz.getName() + ", " + methodName);
            e.printStackTrace();
        }
        return null;
    }
}
