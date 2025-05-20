package com.lifelover.dome.core.plugins.okhttp;

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

public class OkhttpBbPlugin implements BbPlugin {

    @Override
    public AgentBuilder apply(AgentBuilder agentBuilder) {
        return agentBuilder.type(ElementMatchers.named(ClassNames.REAL_CALL_CLASS_NAME))
                .transform(new BbTransformer() {
                    @Override
                    protected Builder<?> build(Builder<?> builder, TypeDescription typeDescription,
                            ClassLoader classLoader,
                            JavaModule module) {
                        System.out.println("Trying to transform: " + typeDescription + " loaded by: " + classLoader);
                        return builder.method(ElementMatchers.named(MethodNames.EXECUTE_METHOD))
                                .intercept(Advice.to(RealCallDelegation.class));
                    }
                });
    }

    @Override
    public String getBpPluginName() {
        return "Okhttp-plugin";
    }

}
