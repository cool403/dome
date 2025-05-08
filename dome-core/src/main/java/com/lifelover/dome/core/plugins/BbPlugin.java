package com.lifelover.dome.core.plugins;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.DynamicType;

public interface BbPlugin {
    
    /**
     * 
     * @param agentBuilder
     */
    AgentBuilder apply(AgentBuilder agentBuilder);


    /**
     * 
     * @return
     */
    String getBpPluginName();
}
