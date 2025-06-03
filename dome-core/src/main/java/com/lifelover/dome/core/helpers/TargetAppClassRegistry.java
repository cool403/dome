package com.lifelover.dome.core.helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TargetAppClassRegistry {
    private TargetAppClassRegistry() {
    }

    private static final Map<String, Class<?>> targetAppClassMap = new HashMap<>();

    /**
     * 
     * @param className
     * @return
     */
    public static synchronized Class<?> getClass(String className) {
        if (targetAppClassMap.containsKey(className)) {
            return targetAppClassMap.get(className);
        }
        try {
            final ClassLoader targetAppClassLoader = ClassloaderRegistry.getTargetAppClassLoader();
            if (targetAppClassLoader == null) {
                return null;
            }
            final Class<?> clazz = targetAppClassLoader.loadClass(className);
            if (clazz != null) {
                targetAppClassMap.put(className, clazz);
            }
            return clazz;
        } catch (Exception e) {
            //判断是否是ClassNotFoundException
            if (e instanceof ClassNotFoundException) {
                targetAppClassMap.put(className, null);
                return null;
            }
            e.printStackTrace();
        }
        return null;
    }
}
