package com.lifelover.dome.core;

import java.util.HashMap;
import java.util.Map;

import com.lifelover.dome.core.config.ConfigLoader;
import com.lifelover.dome.core.plugins.PluginLoader;

public class Initializer {
    private Initializer() {

    }

    /**
     * 
     * @param args 代理启动参数
     *             形如:java -javaagent:my-agent.jar=debug=true,interval=5000 -jar
     *             app.jar
     */
    public static void init(String args) {
        // args 转成map
        Map<String, String> paramMap = toEnvMap(args);
        // System.out.println(args);
        // System.out.println(paramMap);
        // 初始化环境配置
        ConfigLoader.loadAgentConfig(paramMap);
        // 初始化Plugin列表
        PluginLoader.loadPlugins(paramMap);
    }

    /**
     * 参数转换
     * 
     * @param args
     * @return
     */
    public static Map<String, String> toEnvMap(String args) {
        if (args == null || args.isEmpty()) {
            return new HashMap<>();
        }
        final Map<String, String> paramMap = new HashMap<>(16);
        // 首先用"","分割，再用"=""分割
        String[] paramStrArr = args.split(",");
        for (String paramStr : paramStrArr) {
            String[] arr = paramStr.split("=");
            if (arr.length != 2) {
                continue;
            }
            paramMap.put(arr[0], arr[1]);
        }
        return paramMap;
    }
}
