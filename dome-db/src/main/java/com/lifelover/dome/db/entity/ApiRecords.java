package com.lifelover.dome.db.entity;

import java.util.Date;

public class ApiRecords {

    private Long id;
    private String httpUrl;
    private String httpMethod;
    private String queryParams;
    private String requestBody;
    private String responseBody;
    private String traceId;
    private Date reqTime;
    private Date resTime;
    private String httpStatus;
    private String headers;
    private String responseHeaders;
    private Date createdAt;

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

    public String getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(String queryParams) {
        this.queryParams = queryParams;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Date getReqTime() {
        return reqTime;
    }

    public void setReqTime(Date reqTime) {
        this.reqTime = reqTime;
    }

    public Date getResTime() {
        return resTime;
    }

    public void setResTime(Date resTime) {
        this.resTime = resTime;
    }

    public String getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(String httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getHeaders() {
        return headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public String getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(String responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "ApiRecords{" +
                "id=" + id +
                ", httpUrl='" + httpUrl + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", queryParams='" + queryParams + '\'' +
                ", requestBody='" + requestBody + '\'' +
                ", responseBody='" + responseBody + '\'' +
                ", traceId='" + traceId + '\'' +
                ", reqTime=" + reqTime +
                ", resTime=" + resTime +
                ", httpStatus='" + httpStatus + '\'' +
                ", headers='" + headers + '\'' +
                ", responseHeaders='" + responseHeaders + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
