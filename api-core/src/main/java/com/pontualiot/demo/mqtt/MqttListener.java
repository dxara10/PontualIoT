package com.pontualiot.demo.mqtt;

// Importações para listener MQTT
import com.pontualiot.demo.service.MqttAttendanceProcessor;
import org.slf4j.Logger;                    // Interface de logging
import org.slf4j.LoggerFactory;             // Factory para criar loggers
import org.springframework.beans.factory.annotation.Autowired; // Injeção de dependência
import org.springframework.integration.annotation.ServiceActivator; // Anotação para ativador de serviço
import org.springframework.messaging.Message;            // Interface de mensagem Spring
import org.springframework.stereotype.Component;         // Marca como componente Spring

/**
 * Listener MQTT para receber mensagens de dispositivos IoT
 * 
 * Recebe mensagens MQTT de dispositivos de ponto e roteia
 * para o serviço apropriado baseado no tópico e conteúdo.
 */
@Component // Marca como componente Spring gerenciado pelo container
public class MqttListener {

    // Logger para registrar eventos e erros
    private static final Logger logger = LoggerFactory.getLogger(MqttListener.class);
    
    // Injeção do serviço de processamento de mensagens
    @Autowired
    private MqttAttendanceService attendanceService;
    
    @Autowired
    private MqttAttendanceProcessor attendanceProcessor;

    /**
     * Processa mensagens MQTT de registro de ponto
     * 
     * Método ativado automaticamente quando mensagem chega
     * no tópico de dispositivos de ponto.
     * 
     * @param message Mensagem Spring Integration com payload e headers
     */
    @ServiceActivator(inputChannel = "mqttInputChannel") // Canal de entrada MQTT
    public void onMessage(Message<?> message) {
        try {
            // Extrai tópico MQTT dos headers da mensagem
            String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
            
            // Extrai payload da mensagem
            String payload = message.getPayload().toString();
            
            // Log da mensagem recebida para auditoria
            logger.info("Mensagem MQTT recebida - Tópico: {}, Payload: {}", topic, payload);
            
            // Verifica se é mensagem de registro de ponto
            if (isAttendanceTopic(topic)) {
                // Processa mensagem de ponto
                onAttendanceMessage(topic, payload);
            } else {
                // Log de tópico ignorado
                logger.debug("Tópico ignorado (não é de ponto): {}", topic);
            }
            
        } catch (Exception e) {
            // Log de erro sem quebrar o sistema
            logger.error("Erro ao processar mensagem MQTT: {}", e.getMessage(), e);
        }
    }

    /**
     * Processa especificamente mensagens de registro de ponto
     * 
     * Método dedicado para processar mensagens vindas
     * de dispositivos de ponto eletrônico.
     * 
     * @param topic Tópico MQTT da mensagem
     * @param payload Payload JSON da mensagem
     */
    public void onAttendanceMessage(String topic, String payload) {
        try {
            // Log do início do processamento
            logger.debug("Processando mensagem de ponto - Tópico: {}", topic);
            
            // Parse do payload JSON
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode json = mapper.readTree(payload);
            
            String deviceId = json.get("deviceId").asText();
            String rfidTag = json.get("rfidTag").asText();
            String eventType = json.get("eventType").asText();
            
            // Chama processador para criar registro
            var attendance = attendanceProcessor.processAttendanceEvent(rfidTag, eventType, deviceId);
            
            // Log de sucesso com informações do registro
            logger.info("Registro de ponto processado com sucesso - ID: {}, Funcionário: {}, Evento: {}", 
                       attendance.getId(), 
                       attendance.getEmployee().getName(),
                       eventType);
            
        } catch (IllegalArgumentException e) {
            // Log de erro de validação (não é erro crítico)
            logger.warn("Erro de validação na mensagem MQTT: {}", e.getMessage());
        } catch (Exception e) {
            // Log de erro inesperado
            logger.error("Erro inesperado ao processar mensagem de ponto: {}", e.getMessage(), e);
        }
    }

    /**
     * Verifica se tópico é relacionado a registro de ponto
     * 
     * Método auxiliar para filtrar mensagens MQTT
     * e processar apenas as de registro de ponto.
     * 
     * @param topic Tópico MQTT a verificar
     * @return true se for tópico de ponto, false caso contrário
     */
    private boolean isAttendanceTopic(String topic) {
        // Verifica se tópico segue padrão: attendance/{deviceId}/{eventType}
        return topic != null && topic.startsWith("attendance/");
    }
}