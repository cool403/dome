package com.lifelover.dome.core.helpers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TargetAppClassRegistry {
    private TargetAppClassRegistry() {
    }

    private static final Map<String, Class<?>> targetAppClassMap = new ConcurrentHashMap<>();


    public static Class<?> getClass(String className) {
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
            e.printStackTrace();
        }
        return null;
    }
}
