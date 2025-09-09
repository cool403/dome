package com.lifelover.dome.core.config;

import com.lifelover.dome.db.core.DbAccess;
import com.lifelover.dome.db.core.DbConfig;
import com.lifelover.dome.db.core.DefaultDbAccess;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AgentConfig {
    private String collectorUrl;

    private final List<String> ignoreUrls = new ArrayList<>();

    private final List<String> supportMethods = new ArrayList<>();

    private String reporterType = "HTTP";

    private DbAccess dbAccess = null;

    private final List<String> exceptionMonitorPackages = new ArrayList<>();

    public AgentConfig() {
        // 先简单支持后缀匹配,后续支持antUrlMatch
        ignoreUrls.add("/error");
        ignoreUrls.add("/swagger-ui");
        ignoreUrls.add("/actuator/telemetry");

        supportMethods.add("GET");
        supportMethods.add("POST");
        supportMethods.add("DELETE");
    }

    public String getReporterType() {
        return reporterType;
    }

    public void setReporterType(String reporterType) {
        this.reporterType = reporterType;
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

    public void addIgnoreUrls(List<String> ignoreUrls) {
        this.getIgnoreUrls().addAll(ignoreUrls);
    }

    public void initDbAccess(Map<String, String> paramMap) {
        if (paramMap == null) {
            return;
        }
        String jdbcUrl = paramMap.get("jdbcUrl");
        if (jdbcUrl == null) {
            //默认使用临时目录/dome.db
            jdbcUrl = System.getProperty("java.io.tmpdir") + File.separator + "dome.db";
            System.out.println("[dome agent] 未配置jdbcUrl,使用默认临时目录:" + jdbcUrl);
        }
        final DbConfig dbConfig = new DbConfig(jdbcUrl);
        //初始化数据表
        dbConfig.init();
        this.dbAccess = new DefaultDbAccess(dbConfig);
    }

    public DbAccess getDbAccess() {
        return dbAccess;
    }

    public List<String> getExceptionMonitorPackages() {
        return exceptionMonitorPackages;
    }

    public void addExceptionMonitorPackages(List<String> packages) {
        this.exceptionMonitorPackages.addAll(packages);
    }
}
