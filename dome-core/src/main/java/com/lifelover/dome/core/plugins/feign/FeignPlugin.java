package com.lifelover.dome.core.plugins.feign;

import com.lifelover.dome.core.helpers.ClassNames;
import com.lifelover.dome.core.helpers.MethodNames;
import com.lifelover.dome.core.plugins.AbstractBbPlugin;
import com.lifelover.dome.core.plugins.BbTransformer;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import net.bytebuddy.asm.Advice;

public class FeignPlugin extends AbstractBbPlugin {
    @Override
    protected AgentBuilder wrap(AgentBuilder agentBuilder) {
        return agentBuilder
                .type(ElementMatchers.hasSuperType(ElementMatchers.named(ClassNames.FEIGN_CLIENT_CLASS_NAME))
                        .and(ElementMatchers.not(ElementMatchers.isAbstract())))
                .transform(new BbTransformer() {

                    @Override
                    protected Builder<?> build(Builder<?> builder, TypeDescription typeDescription,
                            ClassLoader classLoader, JavaModule module) {
                        //判断是否加载了feign-okhttp
                        //如果加载了feign-okhttp，那么feign的http请求是通过okhttp实现的，
                        //否则feign的http请求是通过httpclient实现的
                        if (hasFeignOkHttp(classLoader)) {
                            return builder;
                        }
                        return builder.method(ElementMatchers.named(MethodNames.EXECUTE_METHOD))
                                .intercept(Advice.to(FeignClientDelegation.class));
                    }

                });
    }

    @Override
    public String getBpPluginName() {
        return "feign-plugin";
    }

    /**
     * 判断是否加载了feign-okhttp
     */
    private static boolean hasFeignOkHttp(ClassLoader classLoader) {
        try {
            classLoader.loadClass(ClassNames.FEIGN_OKHTTP_CLIENT_CLASS_NAME);
            System.out.println("[dome agent] 加载了feign-okhttp，不会进行feign的http请求拦截");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}