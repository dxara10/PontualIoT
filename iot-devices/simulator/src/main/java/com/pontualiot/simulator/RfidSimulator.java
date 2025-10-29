package com.pontualiot.simulator;

/**
 * Simulador de dispositivo RFID
 */
public class RfidSimulator {
    private final String deviceId;

    public RfidSimulator(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public RfidReading simulateReading(String rfidTag) {
        return new RfidReading(deviceId, rfidTag);
    }

    public AttendanceEvent simulateCheckIn(String rfidTag) {
        return new AttendanceEvent(deviceId, rfidTag, "CHECK_IN");
    }

    public AttendanceEvent simulateCheckOut(String rfidTag) {
        return new AttendanceEvent(deviceId, rfidTag, "CHECK_OUT");
    }
}