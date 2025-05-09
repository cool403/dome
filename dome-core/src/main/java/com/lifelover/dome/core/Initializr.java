package com.lifelover.dome.core;

import java.util.List;

import com.lifelover.dome.core.config.ConfigLoader;
import com.lifelover.dome.core.plugins.BbPlugin;
import com.lifelover.dome.core.plugins.PluginLoader;

public class Initializr {
    private Initializr(){

    }

    
    /**
     * 
     * @param args 代理启动参数
     * 形如:java -javaagent:my-agent.jar=debug=true,interval=5000 -jar app.jar
     */
    public static void init(String args){
        //初始化环境配置
        ConfigLoader.loadAgentConfig(args);
        //初始化Plugin列表
        PluginLoader.loadPlugins(args);
    }
}
