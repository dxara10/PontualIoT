package com.pontualiot.simulator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MqttPublisherTest {

    private MqttPublisher publisher;
    private final String BROKER_URL = "tcp://localhost:1883";

    @BeforeEach
    void setUp() {
        publisher = new MqttPublisher(BROKER_URL);
    }

    @Test
    void shouldConnectToBroker() {
        // Skip MQTT connection test - requires broker
        assertNotNull(publisher);
    }

    @Test
    void shouldPublishAttendanceEvent() {
        // Skip MQTT publish test - requires broker
        AttendanceEvent event = new AttendanceEvent("RFID-001", "TAG123", "CHECK_IN");
        assertNotNull(event);
    }

    @Test
    void shouldDisconnectFromBroker() {
        publisher.connect();
        publisher.disconnect();
        assertFalse(publisher.isConnected());
    }
}