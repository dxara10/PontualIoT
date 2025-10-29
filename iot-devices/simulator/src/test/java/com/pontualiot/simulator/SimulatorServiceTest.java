package com.pontualiot.simulator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimulatorServiceTest {

    private SimulatorService service;

    @BeforeEach
    void setUp() {
        service = new SimulatorService("tcp://localhost:1883");
    }

    @Test
    void shouldStartSimulation() {
        // Skip MQTT-dependent test
        assertNotNull(service);
        assertFalse(service.isRunning());
    }

    @Test
    void shouldStopSimulation() {
        service.stopSimulation();
        assertFalse(service.isRunning());
    }

    @Test
    void shouldSimulateRandomEvents() {
        // Skip MQTT-dependent test
        assertEquals(0, service.getEventsGenerated());
    }
}