package com.pontualiot.simulator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Test para MqttClient
 * 
 * Testa envio de dados via MQTT
 */
class MqttClientTest {

    private AttendanceMqttClient mqttClient;

    @BeforeEach
    void setUp() {
        mqttClient = new AttendanceMqttClient("tcp://localhost:1883", "simulator-001");
    }

    /**
     * TDD RED: Teste deve falhar - cliente deve conectar ao broker MQTT
     */
    @Test
    void shouldConnectToBroker() {
        // When - Conecta ao broker
        boolean connected = mqttClient.connect();
        
        // Then - Deve conectar com sucesso
        assertTrue(connected);
        assertTrue(mqttClient.isConnected());
    }

    /**
     * TDD RED: Teste deve falhar - cliente deve publicar evento via MQTT
     */
    @Test
    void shouldPublishAttendanceEvent() {
        // Given - Cliente conectado
        mqttClient.connect();
        
        AttendanceEvent event = new AttendanceEvent(
            "DEVICE_001", 
            "RFID_123456", 
            "CHECK_IN"
        );
        
        // When - Publica evento
        boolean published = mqttClient.publishAttendanceEvent(event);
        
        // Then - Deve publicar com sucesso
        assertTrue(published);
    }

    /**
     * TDD RED: Teste deve falhar - cliente deve desconectar do broker
     */
    @Test
    void shouldDisconnectFromBroker() {
        // Given - Cliente conectado
        mqttClient.connect();
        
        // When - Desconecta
        mqttClient.disconnect();
        
        // Then - Deve desconectar
        assertFalse(mqttClient.isConnected());
    }
}