package com.lifelover.dome.core.report;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 高性能HTTP工具类（基于JDK 1.8 HttpURLConnection）
 * 支持GET/POST请求，默认JSON格式
 */
public class HttpUtil {
    // 默认读取超时时间（毫秒）
    private static final int DEFAULT_TIMEOUT = 150;
    // 默认内容类型
    private static final String DEFAULT_CONTENT_TYPE = "application/json; charset=utf-8";
    // 请求头缓存
    private static final Map<String, String> DEFAULT_HEADERS = new ConcurrentHashMap<>();

    static {
        // 初始化默认请求头
        DEFAULT_HEADERS.put("Accept", "application/json");
        DEFAULT_HEADERS.put("User-Agent", "JDK-HTTP-CLIENT/1.0");
    }

    /**
     * 发送GET请求
     * 
     * @param url 请求地址
     * @return 响应内容
     * @throws IOException 如果发生I/O错误
     */
    public static String get(String url) throws IOException {
        return get(url, null, DEFAULT_TIMEOUT);
    }

    /**
     * 发送GET请求
     * 
     * @param url    请求地址
     * @param params 请求参数
     * @return 响应内容
     * @throws IOException 如果发生I/O错误
     */
    public static String get(String url, Map<String, String> params) throws IOException {
        return get(url, params, DEFAULT_TIMEOUT);
    }

    /**
     * 发送GET请求
     * 
     * @param url            请求地址
     * @param params         请求参数
     * @param connectTimeout 连接超时时间（毫秒）
     * @param readTimeout    读取超时时间（毫秒）
     * @return 响应内容
     * @throws IOException 如果发生I/O错误
     */
    public static String get(String url, Map<String, String> params, int timeout) throws IOException {
        // 构建带参数的URL
        String fullUrl = buildUrlWithParams(url, params);
        HttpURLConnection connection = null;
        try {
            connection = createConnection(fullUrl, "GET", timeout, timeout);
            return readResponse(connection);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 发送POST请求
     * 
     * @param url  请求地址
     * @param body 请求体内容
     * @return 响应内容
     * @throws IOException 如果发生I/O错误
     */
    public static String post(String url, String body) throws IOException {
        return post(url, body, DEFAULT_TIMEOUT);
    }

    /**
     * 发送POST请求
     * 
     * @param url            请求地址
     * @param body           请求体内容
     * @param connectTimeout 连接超时时间（毫秒）
     * @param readTimeout    读取超时时间（毫秒）
     * @return 响应内容
     * @throws IOException 如果发生I/O错误
     */
    public static String post(String url, String body, int timeout) throws IOException {
        HttpURLConnection connection = null;
        OutputStream out = null;
        try {
            connection = createConnection(url, "POST", timeout, timeout);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", DEFAULT_CONTENT_TYPE);
            // 写入请求体
            if (body != null && !body.isEmpty()) {
                out = connection.getOutputStream();
                out.write(body.getBytes(StandardCharsets.UTF_8));
                out.flush();
            }
            return readResponse(connection);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // 忽略关闭异常
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * 创建HTTP连接
     */
    @SuppressWarnings("deprecation")
    private static HttpURLConnection createConnection(String url, String method,
            int connectTimeout, int readTimeout) throws IOException {
        URL urlObj = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
        // 设置默认请求头
        DEFAULT_HEADERS.forEach(connection::setRequestProperty);
        return connection;
    }

    /**
     * 读取响应内容
     */
    private static String readResponse(HttpURLConnection connection) throws IOException {
        int status = connection.getResponseCode();
        if (status >= 200 && status < 300) {
            try (InputStream in = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(in, StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        }
        // 读取错误流
        try (InputStream err = connection.getErrorStream();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(err != null ? err : connection.getInputStream(),
                                StandardCharsets.UTF_8))) {
            StringBuilder errorResponse = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                errorResponse.append(line);
            }
            throw new IOException("HTTP请求失败，状态码: " + status + ", 响应: " + errorResponse.toString());
        }
    }

    /**
     * 构建带参数的URL
     */
    private static String buildUrlWithParams(String url, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return url;
        }
        StringBuilder sb = new StringBuilder(url);
        boolean first = true;
        if (!url.contains("?")) {
            sb.append("?");
            first = true;
        } else {
            first = false;
        }
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
            first = false;
        }
        return sb.toString();
    }

    /**
     * 添加全局请求头
     * 
     * @param key   请求头键
     * @param value 请求头值
     */
    public static void addDefaultHeader(String key, String value) {
        DEFAULT_HEADERS.put(key, value);
    }

    /**
     * 移除全局请求头
     * 
     * @param key 请求头键
     */
    public static void removeDefaultHeader(String key) {
        DEFAULT_HEADERS.remove(key);
    }
}
