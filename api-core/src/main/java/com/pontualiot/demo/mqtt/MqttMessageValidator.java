package com.pontualiot.demo.mqtt;

// Importações para validação e parsing JSON
import com.fasterxml.jackson.core.JsonProcessingException; // Exceção de processing JSON
import com.fasterxml.jackson.databind.JsonNode;       // Nó JSON para parsing
import com.fasterxml.jackson.databind.ObjectMapper;   // Mapper JSON do Jackson
import org.springframework.stereotype.Component;       // Marca como componente Spring

import java.time.LocalDateTime; // Classe para data e hora local
import java.time.format.DateTimeFormatter; // Formatador de data/hora
import java.time.format.DateTimeParseException; // Exceção de parsing de data

/**
 * Validador e parser de mensagens MQTT
 * 
 * Responsável por validar formato JSON, campos obrigatórios
 * e converter payload MQTT em objeto MqttAttendanceMessage.
 */
@Component // Marca como componente Spring gerenciado pelo container
public class MqttMessageValidator {

    // Mapper JSON para parsing de payloads MQTT
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Formatador para timestamps ISO 8601 (ex: 2024-01-15T08:30:00)
    private final DateTimeFormatter timestampFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * Valida se uma mensagem MQTT tem formato correto
     * 
     * Verifica sintaxe JSON e presença de campos obrigatórios
     * sem fazer parsing completo (validação rápida).
     * 
     * @param payload Payload JSON da mensagem MQTT
     * @return true se a mensagem é válida, false caso contrário
     */
    public boolean isValidMessage(String payload) {
        try {
            // Tenta fazer parsing básico do JSON
            JsonNode jsonNode = objectMapper.readTree(payload);
            
            // Verifica se campos obrigatórios existem e não são nulos
            return jsonNode.has("deviceId") && !jsonNode.get("deviceId").isNull() &&
                   jsonNode.has("rfidTag") && !jsonNode.get("rfidTag").isNull() &&
                   jsonNode.has("eventType") && !jsonNode.get("eventType").isNull() &&
                   jsonNode.has("timestamp") && !jsonNode.get("timestamp").isNull();
                   
        } catch (JsonProcessingException e) {
            // Se houver erro no parsing JSON, a mensagem é inválida
            return false;
        } catch (Exception e) {
            // Para outros erros, também considera inválida
            return false;
        }
    }

    /**
     * Valida e faz parsing completo de uma mensagem MQTT
     * 
     * Converte payload JSON em objeto MqttAttendanceMessage
     * com todas as validações necessárias.
     * 
     * @param payload Payload JSON da mensagem MQTT
     * @return Objeto MqttAttendanceMessage parseado e validado
     * @throws IllegalArgumentException se a mensagem for inválida
     */
    public MqttAttendanceMessage validateAndParse(String payload) {
        try {
            // Faz parsing do JSON para JsonNode
            JsonNode jsonNode = objectMapper.readTree(payload);
            
            // Extrai e valida campo deviceId
            String deviceId = extractAndValidateString(jsonNode, "deviceId", "Device ID é obrigatório");
            
            // Extrai e valida campo rfidTag
            String rfidTag = extractAndValidateString(jsonNode, "rfidTag", "RFID tag é obrigatório");
            
            // Extrai e valida campo eventType
            String eventType = extractAndValidateString(jsonNode, "eventType", "Tipo de evento é obrigatório");
            
            // Valida se eventType tem valor permitido
            if (!"CHECK_IN".equals(eventType) && !"CHECK_OUT".equals(eventType)) {
                throw new IllegalArgumentException("Tipo de evento deve ser CHECK_IN ou CHECK_OUT");
            }
            
            // Extrai e valida timestamp
            String timestampStr = extractAndValidateString(jsonNode, "timestamp", "Timestamp é obrigatório");
            LocalDateTime timestamp = parseTimestamp(timestampStr);
            
            // Extrai metadata opcional
            String metadata = jsonNode.has("metadata") ? jsonNode.get("metadata").asText() : null;
            
            // Constrói objeto MqttAttendanceMessage
            MqttAttendanceMessage message = MqttAttendanceMessage.builder()
                    .deviceId(deviceId)     // ID do dispositivo
                    .rfidTag(rfidTag)       // Tag RFID
                    .eventType(eventType)   // Tipo de evento
                    .timestamp(timestamp)   // Timestamp parseado
                    .metadata(metadata)     // Metadata opcional
                    .build();
            
            // Validação final usando método do próprio objeto
            if (!message.isValid()) {
                throw new IllegalArgumentException("Mensagem MQTT inválida após parsing");
            }
            
            return message; // Retorna mensagem validada
            
        } catch (JsonProcessingException e) {
            // Erro específico de parsing JSON
            throw new IllegalArgumentException("Payload JSON malformado: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            // Erros de validação, repassa a mensagem original
            throw e;
        } catch (Exception e) {
            // Para outros erros, trata como JSON malformado
            throw new IllegalArgumentException("Erro inesperado no parsing: " + e.getMessage());
        }
    }

    /**
     * Extrai e valida campo string do JSON
     * 
     * Método auxiliar para extrair campos obrigatórios
     * e validar se não estão vazios ou nulos.
     * 
     * @param jsonNode Nó JSON para extrair o campo
     * @param fieldName Nome do campo a extrair
     * @param errorMessage Mensagem de erro se campo inválido
     * @return Valor do campo validado
     * @throws IllegalArgumentException se campo inválido
     */
    private String extractAndValidateString(JsonNode jsonNode, String fieldName, String errorMessage) {
        // Verifica se campo existe no JSON
        if (!jsonNode.has(fieldName)) {
            throw new IllegalArgumentException(errorMessage + " (campo não encontrado)");
        }
        
        JsonNode fieldNode = jsonNode.get(fieldName); // Obtém nó do campo
        
        // Verifica se campo não é nulo
        if (fieldNode.isNull()) {
            throw new IllegalArgumentException(errorMessage + " (campo é nulo)");
        }
        
        String value = fieldNode.asText(); // Converte para string
        
        // Verifica se campo não está vazio
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage + " (campo vazio)");
        }
        
        return value.trim(); // Retorna valor sem espaços extras
    }

    /**
     * Faz parsing de timestamp ISO 8601
     * 
     * Converte string de timestamp no formato ISO 8601
     * para objeto LocalDateTime.
     * 
     * @param timestampStr String do timestamp
     * @return LocalDateTime parseado
     * @throws IllegalArgumentException se formato inválido
     */
    private LocalDateTime parseTimestamp(String timestampStr) {
        try {
            // Tenta fazer parsing usando formatador ISO 8601
            return LocalDateTime.parse(timestampStr, timestampFormatter);
        } catch (DateTimeParseException e) {
            // Se falhar, lança exceção com mensagem clara
            throw new IllegalArgumentException(
                "Timestamp deve estar no formato ISO 8601 (ex: 2024-01-15T08:30:00): " + timestampStr
            );
        }
    }
}