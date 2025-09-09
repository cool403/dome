package com.lifelover.dome.core.plugins.exception;

import com.lifelover.dome.core.config.ConfigLoader;
import com.lifelover.dome.core.plugins.AbstractBbPlugin;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatchers;

import java.util.List;

public class ExceptionBbPlugin extends AbstractBbPlugin {
    
    private final List<String> targetPackages;
    
    public ExceptionBbPlugin() {
        this.targetPackages = ConfigLoader.getAgentConfig().getExceptionMonitorPackages();
    }
    
    @Override
    protected AgentBuilder wrap(AgentBuilder agentBuilder) {
        if (targetPackages == null || targetPackages.isEmpty()) {
            return agentBuilder;
        }
        
        for (String pkg : targetPackages) {
            agentBuilder = agentBuilder
                .type(ElementMatchers.nameStartsWith(pkg))
                .transform(new ExceptionTransformer());
        }
        
        return agentBuilder;
    }
    
    @Override
    public String getBpPluginName() {
        return "ExceptionMonitorPlugin";
    }
}