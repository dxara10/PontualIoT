package com.pontualiot.simulator;

import java.time.LocalDateTime;

public class AttendanceEvent {
    private final String deviceId;
    private final String rfidTag;
    private final String eventType;
    private final LocalDateTime timestamp;

    public AttendanceEvent(String deviceId, String rfidTag, String eventType) {
        this.deviceId = deviceId;
        this.rfidTag = rfidTag;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getRfidTag() {
        return rfidTag;
    }

    public String getEventType() {
        return eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}