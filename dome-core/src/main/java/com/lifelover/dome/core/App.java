package com.lifelover.dome.core;

import java.lang.instrument.Instrumentation;

import net.bytebuddy.agent.builder.AgentBuilder;

/**
 * Main entry point for the Dome Core application.
 */
public class App {

    /**
     * 
     * @param args
     * @param inst
     */
    public static void premain(String args, Instrumentation inst){
        System.out.println("[dome agent] start to premain");
        AgentBuilder agentBuilder = new AgentBuilder.Default();
        
    }
}
