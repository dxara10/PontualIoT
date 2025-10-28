package com.pontualiot.demo.mqtt;

// Importações para DTO de mensagem MQTT
import lombok.AllArgsConstructor; // Gera construtor com todos os parâmetros
import lombok.Builder;           // Gera padrão Builder para criação de objetos
import lombok.Data;              // Gera getters, setters, toString, equals, hashCode
import lombok.NoArgsConstructor; // Gera construtor sem parâmetros

import java.time.LocalDateTime; // Classe para data e hora local

/**
 * DTO que representa uma mensagem MQTT de registro de ponto
 * 
 * Esta classe encapsula os dados vindos de dispositivos IoT
 * via MQTT para processamento de registros de entrada/saída.
 */
@Data // Lombok: gera getters, setters, toString, equals, hashCode
@Builder // Lombok: gera padrão Builder para criação fluente de objetos
@NoArgsConstructor // Lombok: gera construtor vazio
@AllArgsConstructor // Lombok: gera construtor com todos os campos
public class MqttAttendanceMessage {

    /**
     * ID único do dispositivo IoT que enviou a mensagem
     * Usado para identificar qual dispositivo registrou o ponto
     */
    private String deviceId;

    /**
     * Tag RFID lida pelo dispositivo
     * Identifica o funcionário que está registrando ponto
     */
    private String rfidTag;

    /**
     * Tipo do evento de ponto
     * Valores válidos: "CHECK_IN" (entrada) ou "CHECK_OUT" (saída)
     */
    private String eventType;

    /**
     * Timestamp do evento no formato ISO 8601
     * Representa quando o evento ocorreu no dispositivo
     */
    private LocalDateTime timestamp;

    /**
     * Dados adicionais opcionais da mensagem MQTT
     * Pode conter informações extras do dispositivo (bateria, sinal, etc.)
     */
    private String metadata;

    /**
     * Verifica se a mensagem representa um evento de entrada
     * @return true se for CHECK_IN, false caso contrário
     */
    public boolean isCheckIn() {
        return "CHECK_IN".equals(eventType); // Compara com valor esperado
    }

    /**
     * Verifica se a mensagem representa um evento de saída
     * @return true se for CHECK_OUT, false caso contrário
     */
    public boolean isCheckOut() {
        return "CHECK_OUT".equals(eventType); // Compara com valor esperado
    }

    /**
     * Valida se todos os campos obrigatórios estão preenchidos
     * @return true se a mensagem é válida, false caso contrário
     */
    public boolean isValid() {
        return deviceId != null && !deviceId.trim().isEmpty() &&    // Device ID obrigatório
               rfidTag != null && !rfidTag.trim().isEmpty() &&      // RFID obrigatório
               eventType != null && !eventType.trim().isEmpty() &&  // Tipo de evento obrigatório
               timestamp != null &&                                  // Timestamp obrigatório
               (isCheckIn() || isCheckOut());                        // Tipo deve ser válido
    }
}