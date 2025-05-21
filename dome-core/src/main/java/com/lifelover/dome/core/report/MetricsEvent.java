package com.lifelover.dome.core.report;

import java.util.UUID;

public class MetricsEvent<T> {
    
    private String eventId = UUID.randomUUID().toString();


    private String eventType = "http";


    private T eventData;


    public String getEventId() {
        return eventId;
    }


    public void setEventId(String eventId) {
        this.eventId = eventId;
    }


    public String getEventType() {
        return eventType;
    }


    public void setEventType(String eventType) {
        this.eventType = eventType;
    }


    public T getEventData() {
        return eventData;
    }


    public void setEventData(T eventData) {
        this.eventData = eventData;
    }

    
}
