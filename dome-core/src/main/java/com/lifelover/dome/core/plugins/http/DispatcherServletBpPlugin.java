package com.lifelover.dome.core.plugins.http;

import com.lifelover.dome.core.helpers.ClassNames;
import com.lifelover.dome.core.helpers.MethodNames;
import com.lifelover.dome.core.plugins.BbPlugin;
import com.lifelover.dome.core.plugins.BpTransformer;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

public class DispatcherServletBpPlugin implements BbPlugin {

    @Override
    public void apply(AgentBuilder agentBuilder) {
        agentBuilder.type(ElementMatchers.named(ClassNames.DISPATCHER_SERVLET_CLASS_NAME))
                .transform(new BpTransformer() {
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
        return "DSBP";
    }

}
