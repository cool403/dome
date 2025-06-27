package com.lifelover.dome.db;

import java.util.Date;

public class ApiConfigs {
    private Long id;
    private String httpUrl;
    private String httpMethod;
    private String isMockEnabled;
    private String mockType;
    private String staticResponse;
    private String dynamicRule;
    private String replayRecordId;
    private String delay;
    private String description;
    private Date createdAt;
    private Date updatedAt;

    private ApiRecords replayApiRecords;

    public ApiRecords getReplayApiRecords() {
        return replayApiRecords;
    }

    public void setReplayApiRecords(ApiRecords replayApiRecords) {
        this.replayApiRecords = replayApiRecords;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHttpUrl() {
        return httpUrl;
    }

    public void setHttpUrl(String httpUrl) {
        this.httpUrl = httpUrl;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getIsMockEnabled() {
        return isMockEnabled;
    }

    public void setIsMockEnabled(String isMockEnabled) {
        this.isMockEnabled = isMockEnabled;
    }

    public String getMockType() {
        return mockType;
    }

    public void setMockType(String mockType) {
        this.mockType = mockType;
    }

    public String getStaticResponse() {
        return staticResponse;
    }

    public void setStaticResponse(String staticResponse) {
        this.staticResponse = staticResponse;
    }

    public String getDynamicRule() {
        return dynamicRule;
    }

    public void setDynamicRule(String dynamicRule) {
        this.dynamicRule = dynamicRule;
    }

    public String getReplayRecordId() {
        return replayRecordId;
    }

    public void setReplayRecordId(String replayRecordId) {
        this.replayRecordId = replayRecordId;
    }

    public String getDelay() {
        return delay;
    }

    public void setDelay(String delay) {
        this.delay = delay;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
