package com.lifelover.dome.core.plugins.http;

import com.lifelover.dome.core.helpers.ClassNames;
import com.lifelover.dome.core.helpers.MethodNames;
import com.lifelover.dome.core.plugins.BbPlugin;
import com.lifelover.dome.core.plugins.BbTransformer;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

public class DispatcherServletBbPlugin implements BbPlugin {

    @Override
    public AgentBuilder apply(AgentBuilder agentBuilder) {
        return agentBuilder.type(ElementMatchers.named(ClassNames.DISPATCHER_SERVLET_CLASS_NAME))
                .transform(new BbTransformer() {
                    @Override
                    protected Builder<?> build(Builder<?> builder, TypeDescription typeDescription,
                            ClassLoader classLoader,
                            JavaModule module) {
                        return builder.method(ElementMatchers.named(MethodNames.DO_DISPATCH_METHOD))
                                .intercept(Advice.to(DispatcherServletDelegation.class));
                    }

                });
    }

    @Override
    public String getBpPluginName() {
        return "DSBB";
    }

}
