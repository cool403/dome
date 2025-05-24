package com.lifelover.dome.core.plugins.okhttp;

import com.lifelover.dome.core.helpers.TargetAppClassRegistry;

public class OKhttpAdapterHolder {
    private static volatile OkhttpAdapter okhttpAdapter;

    public static OkhttpAdapter getOkhttpAdapter() {
        if (okhttpAdapter != null) {
            return okhttpAdapter;
        }
        synchronized (OKhttpAdapterHolder.class) {
            if (okhttpAdapter == null) {
                // 获取okhttp版本
                // String okHttpVersion = getOkHttpVersion();
                String okHttpVersion = "4.x";
                System.out.println("okHttpVersion: " + okHttpVersion);
                // 默认是4.x
                okhttpAdapter = new Okhttp4Adapter();
            }
            return okhttpAdapter;
        }
    }

    /**
     * 获取 okHttp 版本,默认返回 4.x
     * 
     * @return
     */
    public static String getOkHttpVersion() {
        try {
            Class<?> verClass = TargetAppClassRegistry.getClass("okhttp3.internal.Version");
            return (String) verClass.getMethod("userAgent").invoke(null);
        } catch (Exception e) {
            return null;
        }
    }
}
