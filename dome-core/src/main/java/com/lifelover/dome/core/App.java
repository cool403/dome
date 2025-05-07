package com.lifelover.dome.core;

import java.lang.instrument.Instrumentation;
import java.util.List;

import com.lifelover.dome.core.plugins.BbPlugin;
import com.lifelover.dome.core.plugins.PluginLoader;

import net.bytebuddy.agent.builder.AgentBuilder;

/**
 * Main entry point for the Dome Core application.
 */
public class App {

    /**
     * 
     * @param args
     * @param inst
     */
    public static void premain(String args, Instrumentation inst){
        System.out.println("[dome agent] start to premain");
        //初始化
        Initializr.init(args);
        //获取加载好的插件列表
        List<BbPlugin> plugins = PluginLoader.getPluginLst();
        if (plugins == null) {
            return;
        }
        AgentBuilder agentBuilder = new AgentBuilder.Default();
        agentBuilder.installOn(inst);
    }
}
