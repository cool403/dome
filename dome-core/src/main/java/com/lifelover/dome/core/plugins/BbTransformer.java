package com.lifelover.dome.core.plugins;

import com.lifelover.dome.core.helpers.ClassloaderRegistry;

import net.bytebuddy.agent.builder.AgentBuilder.Transformer;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.utility.JavaModule;

public abstract class BbTransformer implements Transformer {

    @Override
    public Builder<?> transform(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader,
            JavaModule module) {
        // 保存目标应用的 classloader 后面加载 agent 没有的类依赖会用到
        if (classLoader != null) {
            ClassloaderRegistry.setTargetAppClassLoader(classLoader);
        }
        return build(builder, typeDescription, classLoader, module);
    }

    protected abstract Builder<?> build(Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader,
            JavaModule module);

}
