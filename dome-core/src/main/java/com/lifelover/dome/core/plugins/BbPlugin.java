package com.lifelover.dome.core.plugins;

import net.bytebuddy.agent.builder.AgentBuilder;

public interface BbPlugin {

    /**
     * 
     * @param agentBuilder 代理构建器
     */
    AgentBuilder apply(AgentBuilder agentBuilder);

    /**
     * 
     * @return 插件名称
     */
    String getBpPluginName();
}
