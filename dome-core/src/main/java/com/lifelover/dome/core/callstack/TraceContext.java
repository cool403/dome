package com.lifelover.dome.core.callstack;

import java.util.Stack;
import java.util.logging.Logger;

public class TraceContext {
    private static final Logger log = Logger.getLogger(TraceContext.class.getName());

    private static final ThreadLocal<Stack<TraceNode>> TRACE_STACK = new ThreadLocal<>();

    private static final ThreadLocal<TraceNode> ROOT_NODE = new ThreadLocal<>();

    private static final ThreadLocal<Boolean> TRACING_ACTIVE = ThreadLocal.withInitial(()-> false);

    public static void startTrace(){
        TRACING_ACTIVE.set(true);
        Stack<TraceNode> stack = new Stack<>();
        TRACE_STACK.set(stack);
    }

    public static TraceNode enterMethod(String className, String methodName, Object[] args){
        final Stack<TraceNode> stack = TRACE_STACK.get();
        final TraceNode node = new TraceNode(className, methodName, args);
        if (stack.isEmpty()) {
            ROOT_NODE.set(node);
        }else{
            final TraceNode parent = stack.peek();
            parent.addChild(node);
            node.setParent(parent);
        }
        stack.push(node);
        return node;
    }


    public static void exitMethod(TraceNode node){
        final Stack<TraceNode> stack = TRACE_STACK.get();
        if (stack == null) {
            return;
        }
        if (!stack.isEmpty() || stack.peek() == node) {
            stack.pop();
            node.setEndTime(System.currentTimeMillis());
            if (stack.isEmpty()) {
                ROOT_NODE.remove();
                TRACE_STACK.remove();
                TRACING_ACTIVE.remove();
            }
        }
    }

    public static void catchException(Throwable throwable){
        final Stack<TraceNode> stack = TRACE_STACK.get();
        if (stack == null) {
            return;
        }
        if (!stack.isEmpty() && stack.peek() != null) {
            final TraceNode node = stack.peek();
            node.setException(throwable);
        }
    }


    public static boolean isTracingActive(){
        return TRACING_ACTIVE.get();
    }

    private static void processTraceResult(TraceNode root){
        log.info("===== 调用树结果 =====");   
        printTraceTree(root, 0);
        log.info("===== 调用树结果 =====\n");   
    }

    private static void printTraceTree(TraceNode node, int depth){
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            sb.append("  ");
        }
    }
}
