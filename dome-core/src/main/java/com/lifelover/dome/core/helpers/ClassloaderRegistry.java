package com.lifelover.dome.core.helpers;


public class ClassloaderRegistry {
    private ClassloaderRegistry() {
    }

    private static volatile ClassLoader targetAppClassLoader = null;

    public static void setTargetAppClassLoader(ClassLoader appClassLoader) {
        if (targetAppClassLoader == null && appClassLoader != null) {
            targetAppClassLoader = appClassLoader;
        }
    }

    public static ClassLoader getTargetAppClassLoader() {
        return targetAppClassLoader;
    }
}
