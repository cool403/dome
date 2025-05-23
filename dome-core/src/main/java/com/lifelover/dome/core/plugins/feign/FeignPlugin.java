package com.lifelover.dome.core.plugins.feign;

import com.lifelover.dome.core.plugins.AbstractBbPlugin;
import com.lifelover.dome.core.plugins.BbPlugin;
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
                        .method(ElementMatchers.any())
                        .intercept(Advice.to(FeignClientDelegation.class)));
    }

    @Override
    public String getBpPluginName() {
        return "feign-plugin";
    }

    private ElementMatcher<? super TypeDescription> createTypeMatcher() {
        return ElementMatchers.named("feign.Client$Default")
                .or(ElementMatchers.named("org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient"))
                .or(ElementMatchers.named("org.springframework.cloud.openfeign.FeignClient"));
    }
}