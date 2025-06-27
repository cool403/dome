package com.lifelover.dome.core;

import java.lang.instrument.Instrumentation;
import java.util.List;

import com.lifelover.dome.core.plugins.BbPlugin;
import com.lifelover.dome.core.plugins.PluginLoader;

import net.bytebuddy.agent.builder.AgentBuilder;

/**
 * Dome Core应用程序的主入口点。
 */
public class App {

    /**
     * 
     * @param args 代理参数
     * @param inst 插桩工具
     */
    public static void premain(String args, Instrumentation inst){
        System.out.println("[dome agent] start to premain");
        //初始化
        Initializer.init(args);
        //获取加载好的插件列表
        List<BbPlugin> plugins = PluginLoader.getPluginLst();
        if (plugins == null) {
            return;
        }
        AgentBuilder agentBuilder = new AgentBuilder.Default();
        //装载插件
        for (BbPlugin plugin : plugins) {
            agentBuilder = plugin.apply(agentBuilder);
        }
        agentBuilder.installOn(inst);
    }
}
