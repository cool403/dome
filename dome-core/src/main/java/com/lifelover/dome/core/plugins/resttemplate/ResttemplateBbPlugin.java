package com.lifelover.dome.core.plugins.resttemplate;

import com.lifelover.dome.core.helpers.ClassNames;
import com.lifelover.dome.core.helpers.MethodNames;
import com.lifelover.dome.core.plugins.AbstractBbPlugin;
import com.lifelover.dome.core.plugins.BbTransformer;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

public class ResttemplateBbPlugin extends AbstractBbPlugin {

    @Override
    public String getBpPluginName() {
        return "resttemplate";
    }

    @Override
    protected AgentBuilder wrap(AgentBuilder agentBuilder) {
        return agentBuilder.type(ElementMatchers.hasSuperType(ElementMatchers.named(ClassNames.RT_REQUEST_CLASS_NAME))
                .and(ElementMatchers.not(ElementMatchers.isAbstract())))
                .transform(new BbTransformer() {
                    @Override
                    protected Builder<?> build(Builder<?> builder, TypeDescription typeDescription,
                            ClassLoader classLoader,
                            JavaModule module) {
                        return builder.method(ElementMatchers.named(MethodNames.EXECUTE_METHOD)
                                .and(ElementMatchers.isDeclaredBy(typeDescription)))
                                .intercept(Advice.to(RequestClassDelegation.class).wrap(SuperMethodCall.INSTANCE));
                    }
                });
    }

}
