package com.lifelover.dome.core.plugins.exception;

import com.lifelover.dome.core.plugins.BbTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

public class ExceptionTransformer extends BbTransformer {
    
    @Override
    protected Builder<?> build(Builder<?> builder, TypeDescription typeDescription, 
                             ClassLoader classLoader, JavaModule module) {
        return builder
            .method(ElementMatchers.isPublic()
                .and(ElementMatchers.not(ElementMatchers.isStatic()))
                .and(ElementMatchers.not(ElementMatchers.isConstructor()))
                .and(ElementMatchers.not(ElementMatchers.nameStartsWith("get")))
                .and(ElementMatchers.not(ElementMatchers.nameStartsWith("set")))
                .and(ElementMatchers.not(ElementMatchers.nameStartsWith("is")))
                .and(ElementMatchers.not(ElementMatchers.returns(void.class)))
            )
            .intercept(Advice.to(ExceptionAdvice.class));
    }
}