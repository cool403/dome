package com.lifelover.dome.core.callstack;

import com.lifelover.dome.core.plugins.AbstractBbPlugin;
import com.lifelover.dome.core.plugins.BbTransformer;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.MethodDelegation;;

public class MethodCallStackBbPlugin extends AbstractBbPlugin {

    @Override
    public String getBpPluginName() {
        return "method-call-stack";
    }

    @Override
    protected AgentBuilder wrap(AgentBuilder agentBuilder) {
        return agentBuilder.type(getTypeMatcher())
                .transform(new BbTransformer() {
                    @Override
                    protected Builder<?> build(Builder<?> builder, TypeDescription typeDescription,
                            ClassLoader classLoader,
                            JavaModule module) {
                        return builder.method(getMethodMatcher())
                                .intercept(MethodDelegation.to(TraceInterceptor.class));
                    }
                });

    }

    public static ElementMatcher<? super TypeDescription> getTypeMatcher() {
        return ElementMatchers.not(ElementMatchers.nameContainsIgnoreCase("$$FastClassBySpringCGLIB$$"))
        .and(ElementMatchers.nameMatches("com.lifelover.dome.*"))
        .and(ElementMatchers.not(ElementMatchers.nameEndsWithIgnoreCase("VO")))
        .and(ElementMatchers.not(ElementMatchers.nameEndsWithIgnoreCase("PO")))
        .and(ElementMatchers.not(ElementMatchers.nameEndsWithIgnoreCase("DTO")))
        .and(ElementMatchers.not(ElementMatchers.nameEndsWithIgnoreCase("BO")))
        .and(ElementMatchers.not(ElementMatchers.nameContainsIgnoreCase(".pojo.")))
        .and(ElementMatchers.not(ElementMatchers.nameContainsIgnoreCase(".valueobject.")))
        .and(ElementMatchers.not(ElementMatchers.nameEndsWith("Builder")));
    }

    public static ElementMatcher<? super MethodDescription> getMethodMatcher() {
        return ElementMatchers.not(ElementMatchers.isGetter())
        .and(ElementMatchers.not(ElementMatchers.isSetter()))
        .and(ElementMatchers.not(ElementMatchers.isClone()));
    }
}
