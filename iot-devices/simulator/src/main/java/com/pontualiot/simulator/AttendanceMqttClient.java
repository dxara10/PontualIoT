package com.pontualiot.simulator;

/**
 * Cliente MQTT para enviar dados via MQTT
 */
public class AttendanceMqttClient {
    private String brokerUrl;
    private String clientId;
    private boolean connected = false;

    public AttendanceMqttClient(String brokerUrl, String clientId) {
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
    }

    public boolean connect() {
        // TODO: Implementar conexão MQTT real
        connected = true; // Implementação mínima
        return true;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean publishAttendanceEvent(AttendanceEvent event) {
        // TODO: Implementar publicação MQTT real
        return connected; // Só publica se conectado
    }

    public void disconnect() {
        // TODO: Implementar desconexão MQTT real
        connected = false;
    }
}