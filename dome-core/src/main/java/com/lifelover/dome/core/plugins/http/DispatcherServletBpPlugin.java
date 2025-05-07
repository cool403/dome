package com.lifelover.dome.core.plugins.http;

import com.lifelover.dome.core.plugins.BbPlugin;

import net.bytebuddy.agent.builder.AgentBuilder;

public class DispatcherServletBpPlugin implements BbPlugin{

    @Override
    public void apply(AgentBuilder agentBuilder) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'apply'");
    }

    @Override
    public String getBpPluginName() {
        return "DSBP";
    }
    
}
