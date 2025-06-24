package com.lifelover.dome.core.plugins.okhttp;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

public class RealCallDelegation {

    //okhttp3.internal.Version.userAgent
    @Advice.OnMethodEnter()
    public static void onMethodEnter(@Advice.This Object call) {
        OkhttpAdapter adapter = OkhttpAdapterHolder.getOkhttpAdapter();
        if (adapter == null) {
            return;
        }
        adapter.beforeCall(call);
    }

    @Advice.OnMethodExit(onThrowable = Throwable.class)
    public static void onMethodExit(@Advice.This Object call,
            @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object response,
            @Advice.Thrown Throwable throwable) {
        OkhttpAdapter adapter = OkhttpAdapterHolder.getOkhttpAdapter();
        if (adapter == null) {
            return;
        }
       Object newResponse = adapter.afterCall(response, throwable);
       if (newResponse != null) {
           response = newResponse;
       }
    }

}
