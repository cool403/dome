package com.lifelover.dome.core.config;

import java.util.Map;

import com.lifelover.dome.core.helpers.EnvNames;

public class ConfigLoader {
    private ConfigLoader() {

    }

    private static volatile AgentConfig agentConfig = null;

    /**
     * 
     * @param paramMap 代理启动参数
     *                 形如:java -javaagent:my-agent.jar=debug=true,interval=5000 -jar
     *                 app.jar
     */
    public static synchronized void loadAgentConfig(Map<String, String> paramMap) {
        if (agentConfig == null) {
            // 从环境变量中获取collector地址,不是启动参数
            agentConfig = new AgentConfig();
            String collectorAddr = getEnvValue(paramMap, EnvNames.ENV_COLLECTOR_ADDR);
            agentConfig.setCollectorUrl(collectorAddr);
            //获取reporterType
            String reporterType = getEnvValue(paramMap, EnvNames.ENV_REPORTER);
            if (reporterType != null && reporterType != "") {
                agentConfig.setReporterType(reporterType);
            }
        }
    }

    public static AgentConfig getAgentConfig() {
        return agentConfig;
    }

    /**
     * 获取环境变量，优先从java 启动参数取，没有再从环境变量取
     * 
     * @param envKey
     * @param paramMap
     * @return
     */
    public static String getEnvValue(Map<String, String> paramMap, String envKey) {
        if (paramMap.containsKey(envKey)) {
            return paramMap.get(envKey);
        }
        return System.getenv(envKey);
    }
}
