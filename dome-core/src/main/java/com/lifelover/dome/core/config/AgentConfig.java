package com.lifelover.dome.core.config;

import java.util.ArrayList;
import java.util.List;

public class AgentConfig {
    private String collectorUrl;

    private List<String> ignoreUrls = new ArrayList<>();

    private List<String> supportMethods = new ArrayList<>();

    public AgentConfig(){
        //先简单支持后缀匹配,后续支持antUrlMatch
        ignoreUrls.add("/error");
        ignoreUrls.add("/swagger-ui");

        supportMethods.add("GET");
        supportMethods.add("POST");
        supportMethods.add("DELETE");
    }

    public String getCollectorUrl() {
        return collectorUrl;
    }

    public void setCollectorUrl(String collectorAddr) {
        this.collectorUrl = collectorAddr;
    }

    public List<String> getIgnoreUrls() {
        return ignoreUrls;
    }

    public List<String> getSupportMethods() {
        return supportMethods;
    }

    
}
