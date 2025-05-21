package com.lifelover.dome.core.report;

public class HttpMetricsData{
    private String traceId;
    private Long metricTime;
    private Long reqTime;
    private Long respTime;
    private String httpMethod;
    private String httpStatus;
    private String httpUrl;
    private String queryParams;
    private String requestBody;
    private String responseBody;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(String httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getHttpUrl() {
        return httpUrl;
    }

    public void setHttpUrl(String httpUrl) {
        this.httpUrl = httpUrl;
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

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Long getReqTime() {
        return reqTime;
    }

    public String getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(String queryParams) {
        this.queryParams = queryParams;
    }

    public void setReqTime(Long reqTime) {
        this.reqTime = reqTime;
    }

    public void setRespTime(Long respTime) {
        this.respTime = respTime;
    }

    public Long getRespTime() {
        return respTime;
    }

    public Long getMetricTime() {
        return metricTime;
    }

    public void setMetricTime(Long metricTime) {
        this.metricTime = metricTime;
    }

    // @Override
    // public String jsonStr() {
    //     StringBuilder json = new StringBuilder("{");
    //     String eventId = getEventId();
    //     final long now = System.currentTimeMillis();
    //     json.append("\"eventId\":\"").append(eventId).append("\",");
    //     json.append("\"metricTime\":\"").append(now).append("\",");
    //     json.append("\"reqTime\":\"").append(reqTime).append("\",");
    //     json.append("\"respTime\":\"").append(now).append("\",");
    //     json.append("\"httpMethod\":\"").append(httpMethod == null ? "" : httpMethod).append("\",");
    //     json.append("\"httpStatus\":\"").append(httpStatus == null ? "" : httpStatus).append("\",");
    //     json.append("\"httpUrl\":\"").append(httpUrl == null ? "" : httpUrl).append("\",");
    //     json.append("\"queryParams\":\"").append(queryParams == null ? "" : queryParams).append("\",");
    //     json.append("\"requestBody\":\"").append(requestBody == null ? "" : requestBody.replace("\"", "\\\"")).append("\",");
    //     json.append("\"responseBody\":\"").append(responseBody == null ? "" : responseBody.replace("\"", "\\\"")).append("\",");
    //     json.append("\"traceId\":\"").append(traceId == null ? "" : traceId).append("\"");
    //     json.append("}");
    //     return json.toString();
    // }
}
