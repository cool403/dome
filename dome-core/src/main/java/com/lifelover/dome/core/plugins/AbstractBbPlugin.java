package com.lifelover.dome.core.plugins;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.utility.JavaModule;

public abstract class AbstractBbPlugin implements BbPlugin {

    @Override
    public AgentBuilder apply(AgentBuilder agentBuilder) {
        //每一个都添加一个失败的监听器
        agentBuilder = agentBuilder.with(new AgentBuilder.Listener.Adapter() {
            @Override
            public void onError(String typeName, ClassLoader loader, JavaModule module, boolean loaded, Throwable throwable) {
                System.err.println("[dome agent] [" + getBpPluginName() + "]" + " ERROR on: " + typeName);
                throwable.printStackTrace();
            }
        });
        return wrap(agentBuilder);
    }

    protected abstract AgentBuilder wrap(AgentBuilder agentBuilder);
}
