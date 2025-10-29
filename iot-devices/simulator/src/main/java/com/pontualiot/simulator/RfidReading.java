package com.pontualiot.simulator;

import java.time.LocalDateTime;

public class RfidReading {
    private final String deviceId;
    private final String rfidTag;
    private final LocalDateTime timestamp;

    public RfidReading(String deviceId, String rfidTag) {
        this.deviceId = deviceId;
        this.rfidTag = rfidTag;
        this.timestamp = LocalDateTime.now();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getRfidTag() {
        return rfidTag;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}