package com.pontualiot.simulator;

/**
 * Aplicação principal do simulador IoT
 * 
 * Demonstra o funcionamento dos dispositivos simulados
 */
public class SimulatorApp {
    
    public static void main(String[] args) {
        System.out.println("🚀 PontualIoT - Simulador de Dispositivos IoT");
        System.out.println("==============================================");
        
        // Criar simulador RFID
        RfidSimulator rfidSimulator = new RfidSimulator("DEVICE_001");
        
        // Simular leituras
        System.out.println("\n📱 Simulando leituras RFID:");
        RfidReading reading1 = rfidSimulator.simulateReading("RFID_123456");
        System.out.println("✅ Leitura: " + reading1.getRfidTag() + " em " + reading1.getTimestamp());
        
        // Simular eventos de presença
        System.out.println("\n⏰ Simulando eventos de presença:");
        AttendanceEvent checkIn = rfidSimulator.simulateCheckIn("RFID_123456");
        System.out.println("✅ Check-in: " + checkIn.getRfidTag() + " - " + checkIn.getEventType());
        
        AttendanceEvent checkOut = rfidSimulator.simulateCheckOut("RFID_123456");
        System.out.println("✅ Check-out: " + checkOut.getRfidTag() + " - " + checkOut.getEventType());
        
        // Testar clientes HTTP e MQTT
        System.out.println("\n🌐 Testando clientes:");
        
        AttendanceHttpClient httpClient = new AttendanceHttpClient("http://localhost:8082");
        boolean httpSuccess = httpClient.sendAttendanceEvent(checkIn);
        System.out.println("✅ HTTP Client: " + (httpSuccess ? "Conectado" : "Falhou"));
        
        AttendanceMqttClient mqttClient = new AttendanceMqttClient("tcp://localhost:1883", "simulator-001");
        boolean mqttConnected = mqttClient.connect();
        System.out.println("✅ MQTT Client: " + (mqttConnected ? "Conectado" : "Falhou"));
        
        System.out.println("\n🎯 Simulador IoT funcionando perfeitamente!");
    }
}