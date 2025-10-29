package com.pontualiot.simulator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RfidSimulatorTest {

    private RfidSimulator simulator;
    private final String DEVICE_ID = "RFID-001";
    private final String RFID_TAG = "TAG123456";

    @BeforeEach
    void setUp() {
        simulator = new RfidSimulator(DEVICE_ID);
    }

    @Test
    void shouldCreateRfidReading() {
        RfidReading reading = simulator.simulateReading(RFID_TAG);
        
        assertNotNull(reading);
        assertEquals(DEVICE_ID, reading.getDeviceId());
        assertEquals(RFID_TAG, reading.getRfidTag());
        assertNotNull(reading.getTimestamp());
    }

    @Test
    void shouldCreateCheckInEvent() {
        AttendanceEvent event = simulator.simulateCheckIn(RFID_TAG);
        
        assertNotNull(event);
        assertEquals(DEVICE_ID, event.getDeviceId());
        assertEquals(RFID_TAG, event.getRfidTag());
        assertEquals("CHECK_IN", event.getEventType());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void shouldCreateCheckOutEvent() {
        AttendanceEvent event = simulator.simulateCheckOut(RFID_TAG);
        
        assertNotNull(event);
        assertEquals(DEVICE_ID, event.getDeviceId());
        assertEquals(RFID_TAG, event.getRfidTag());
        assertEquals("CHECK_OUT", event.getEventType());
        assertNotNull(event.getTimestamp());
    }
}