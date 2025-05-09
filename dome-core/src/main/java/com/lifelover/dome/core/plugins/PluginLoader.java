package com.lifelover.dome.core.plugins;

import java.util.ArrayList;
import java.util.List;

import com.lifelover.dome.core.plugins.feign.FeignPlugin;
import com.lifelover.dome.core.plugins.http.DispatcherServletBbPlugin;

public class PluginLoader {
    private PluginLoader(){

    }

    private static volatile List<BbPlugin> pluginLst = null;


    /**
     * 
     * @param args 代理启动参数
     * 形如:java -javaagent:my-agent.jar=debug=true,interval=5000 -jar app.jar
     */
    public static synchronized void loadPlugins(String args){
        if (pluginLst == null) {
            pluginLst = new ArrayList<>();
            BbPlugin dsbb = new DispatcherServletBbPlugin();
            BbPlugin feignPlugin = new FeignPlugin();
            pluginLst.add(feignPlugin);
            pluginLst.add(dsbb);
        }
    }


    public static List<BbPlugin> getPluginLst() {
        return pluginLst;
    }
}
