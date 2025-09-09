package com.lifelover.dome.core.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.lifelover.dome.core.plugins.exception.ExceptionBbPlugin;
import com.lifelover.dome.core.plugins.feign.FeignPlugin;
import com.lifelover.dome.core.plugins.http.DispatcherServletBbPlugin;
import com.lifelover.dome.core.plugins.okhttp.OkhttpBbPlugin;
import com.lifelover.dome.core.plugins.resttemplate.ResttemplateBbPlugin;

public class PluginLoader {
    private PluginLoader(){

    }

    private static volatile List<BbPlugin> pluginLst = null;


    /**
     * 
     * @param paramMap 代理启动参数
     * 形如:java -javaagent:my-agent.jar=debug=true,interval=5000 -jar app.jar
     */
    public static synchronized void loadPlugins(Map<String,String> paramMap){
        if (pluginLst == null) {
            pluginLst = new ArrayList<>();
            BbPlugin dsbb = new DispatcherServletBbPlugin();
            BbPlugin feignPlugin = new FeignPlugin();
            pluginLst.add(feignPlugin);
            pluginLst.add(new OkhttpBbPlugin());
            pluginLst.add(dsbb);
            pluginLst.add(new ResttemplateBbPlugin());
            
            // 添加异常监控插件
            pluginLst.add(new ExceptionBbPlugin());
        }
    }


    public static List<BbPlugin> getPluginLst() {
        return pluginLst;
    }
}
