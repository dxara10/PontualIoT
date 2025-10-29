package com.pontualiot.simulator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.paho.client.mqttv3.*;

public class MqttPublisher {
    private final String brokerUrl;
    private MqttClient client;
    private final ObjectMapper objectMapper;

    public MqttPublisher(String brokerUrl) {
        this.brokerUrl = brokerUrl;
        this.objectMapper = new ObjectMapper();
    }

    public boolean connect() {
        try {
            client = new MqttClient(brokerUrl, MqttClient.generateClientId());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            client.connect(options);
            return true;
        } catch (MqttException e) {
            return false;
        }
    }

    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    public boolean publishAttendanceEvent(AttendanceEvent event) {
        if (!isConnected()) return false;
        
        try {
            String topic = "attendance/" + event.getDeviceId() + "/" + event.getEventType();
            String payload = objectMapper.writeValueAsString(event);
            MqttMessage message = new MqttMessage(payload.getBytes());
            message.setQos(1);
            client.publish(topic, message);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void disconnect() {
        try {
            if (client != null && client.isConnected()) {
                client.disconnect();
            }
        } catch (MqttException e) {
            // Ignore disconnect errors
        }
    }
}