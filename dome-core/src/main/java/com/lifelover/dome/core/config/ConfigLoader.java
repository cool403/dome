package com.lifelover.dome.core.config;

import com.lifelover.dome.core.helpers.EnvNames;

public class ConfigLoader {
    private ConfigLoader(){

    }


    private static volatile AgentConfig agentConfig = null;


    /**
     * 
     * @param args agent启动参数
     * 形如:java -javaagent:my-agent.jar=debug=true,interval=5000 -jar app.jar
     */
    public static synchronized void loadAgentConfig(String args){
        if (agentConfig == null) {
            //从环境变量中获取collector地址,不是启动参数
            agentConfig  = new AgentConfig();
            String collectorAddr = System.getenv(EnvNames.ENV_COLLECTOR_ADDR);
            agentConfig.setCollectorAddr(collectorAddr);
        }
    }


    public static AgentConfig getAgentConfig() {
        return agentConfig;
    }

    
}
