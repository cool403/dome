package com.lifelover.dome.core.helpers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReflectMethods {
    private static final Map<String, Method> methodCache = new ConcurrentHashMap<>();

    /**
     * 
     * @param clazz
     * @param methodName
     * @return
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        if (clazz == null) {
            return null;
        }
        String key = clazz.getName() + ":" + methodName;
        if (parameterTypes != null) {
            for (Class<?> parameterTypeClz : parameterTypes) {
                key = key + ":" + parameterTypeClz.getName();
            }
        }
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

    /**
     * 
     * @param method
     * @param obj
     * @param args
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(Method method, Object obj, Object... args) {
        if (method == null) {
            return null;
        }
        try {
            Object result = method.invoke(obj, args);
            if (result == null) {
                return null;
            }
            return (T) result;
        } catch (Exception ex) {
            System.err.println("invoke method=" + method.getName() + " error:" + ex.getStackTrace());
        }
        return null;
    }

    /**
     * 
     * @param clazz
     * @param methodName
     * @param parameterTypes
     * @param obj
     * @param args
     * @return
     */
    public static <T> T invokeMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object obj,
            Object... args) {
        Method method = getMethod(clazz, methodName, parameterTypes);
        return invokeMethod(method, obj, args);
    }

    /**
     * 
     * @param clazz
     * @param methodName
     * @param obj
     * @param args
     * @return
     */
    public static <T> T invokeMethod(Class<?> clazz, String methodName, Object obj, Object... args) {
        return invokeMethod(clazz, methodName, null, obj, args);
    }
}
