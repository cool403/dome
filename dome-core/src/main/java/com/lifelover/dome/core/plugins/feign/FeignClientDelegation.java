package com.lifelover.dome.core.plugins.feign;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

public class FeignClientDelegation {

    @Advice.OnMethodEnter()
    public static void onMethodEnter(
            @Advice.Argument(readOnly = false, value = 0, typing = Assigner.Typing.DYNAMIC) Object request,
            @Advice.Argument(readOnly = false, value = 1, typing = Assigner.Typing.DYNAMIC) Object response)
            throws Exception {
        try {

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to wrap Feign request/response: " + e.getMessage());
        }
    }

    @Advice.OnMethodExit()
    public static void onMethodExit(
            @Advice.Argument(readOnly = false, value = 0, typing = Assigner.Typing.DYNAMIC) Object request,
            @Advice.Argument(readOnly = false, value = 1, typing = Assigner.Typing.DYNAMIC) Object response)
            throws Exception {
        try {

        } catch (Exception e) {
            System.out.println("intercept on Feign request error: " + e.getMessage());
        }
    }
}