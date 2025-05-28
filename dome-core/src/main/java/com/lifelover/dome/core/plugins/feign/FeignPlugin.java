package com.lifelover.dome.core.plugins.feign;

import com.lifelover.dome.core.helpers.ClassNames;
import com.lifelover.dome.core.helpers.MethodNames;
import com.lifelover.dome.core.plugins.AbstractBbPlugin;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.asm.Advice;

public class FeignPlugin extends AbstractBbPlugin {
    @Override
    protected AgentBuilder wrap(AgentBuilder agentBuilder) {
        return agentBuilder
                .type(createTypeMatcher())
                .transform((builder, typeDescription, classLoader, module) -> builder
                        .method(ElementMatchers.named(MethodNames.EXECUTE_METHOD))
                        .intercept(Advice.to(FeignClientDelegation.class)));
    }

    @Override
    public String getBpPluginName() {
        return "feign-plugin";
    }

    private ElementMatcher<? super TypeDescription> createTypeMatcher() {
        return ElementMatchers.named(ClassNames.FEIGN_CLIENT_CLASS_NAME);
    }
}