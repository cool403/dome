package com.lifelover.dome.core.report;

public class HttpMetricsEvent implements MetricsEvent{
    private String traceId;
    private String httpStatus;
    private String httpUrl;
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

    @Override
    public String jsonStr() {
        StringBuilder json = new StringBuilder("{");
        String eventId = getEventId();
        json.append("\"eventId\":\"").append(eventId).append("\",");
        json.append("\"httpStatus\":\"").append(httpStatus == null ? "" : httpStatus).append("\",");
        json.append("\"httpUrl\":\"").append(httpUrl == null ? "" : httpUrl).append("\",");
        json.append("\"requestBody\":\"").append(requestBody == null ? "" : requestBody.replace("\"", "\\\"")).append("\",");
        json.append("\"responseBody\":\"").append(responseBody == null ? "" : responseBody.replace("\"", "\\\"")).append("\",");
        json.append("\"traceId\":\"").append(traceId == null ? "" : traceId).append("\"");
        json.append("}");
        return json.toString();
    }
}
