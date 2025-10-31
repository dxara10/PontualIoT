package com.pontualiot.demo.mqtt;

// ========================================
// IMPORTAÇÕES MQTT E SPRING INTEGRATION
// ========================================
import com.pontualiot.demo.service.MqttAttendanceProcessor; // Processador de eventos
import org.slf4j.Logger;                    // Interface de logging SLF4J
import org.slf4j.LoggerFactory;             // Factory para criar loggers
import org.springframework.beans.factory.annotation.Autowired; // Injeção de dependência
import org.springframework.integration.annotation.ServiceActivator; // Ativador de serviço
import org.springframework.messaging.Message;            // Interface de mensagem Spring
import org.springframework.stereotype.Component;         // Componente Spring

/**
 * ========================================
 * MQTT LISTENER - RECEPTOR DE MENSAGENS IOT
 * ========================================
 * 
 * RESPONSABILIDADES:
 * - Receber mensagens MQTT de dispositivos IoT
 * - Filtrar mensagens por tópico
 * - Rotear para processadores apropriados
 * - Tratar erros sem quebrar o sistema
 * - Registrar logs para auditoria
 * 
 * FLUXO COMPLETO MQTT:
 * 
 * 1. DISPOSITIVO IOT:
 *    - Funcionário aproxima cartão RFID
 *    - Dispositivo lê tag: "RFID001"
 *    - Determina evento: "check-in" ou "check-out"
 * 
 * 2. PUBLICAÇÃO MQTT:
 *    - Tópico: "attendance/device001/check-in"
 *    - Payload: {"rfidTag": "RFID001", "deviceId": "device001", "eventType": "check-in"}
 * 
 * 3. MQTT BROKER:
 *    - Eclipse Mosquitto recebe mensagem
 *    - Distribui para subscribers
 * 
 * 4. SPRING INTEGRATION:
 *    - MqttPahoMessageDrivenChannelAdapter recebe
 *    - Converte para Spring Message
 *    - Envia para mqttInputChannel
 * 
 * 5. MQTT LISTENER (esta classe):
 *    - @ServiceActivator processa mensagem
 *    - Extrai tópico e payload
 *    - Filtra por tópico de attendance
 *    - Roteia para processador
 * 
 * 6. PROCESSAMENTO:
 *    - MqttAttendanceProcessor.processAttendanceEvent()
 *    - Valida RFID, cria/atualiza Attendance
 *    - Persiste no PostgreSQL
 * 
 * 7. RESPOSTA:
 *    - Log de sucesso/erro
 *    - Sistema continua ouvindo
 * 
 * PADRÕES DE TÓPICO SUPORTADOS:
 * - attendance/device001/check-in
 * - attendance/device002/check-out
 * - attendance/{deviceId}/{eventType}
 * 
 * TRATAMENTO DE ERROS:
 * - Erros de validação: log WARNING (não crítico)
 * - Erros inesperados: log ERROR (investigação necessária)
 * - Sistema nunca para por erro em mensagem
 * 
 * AUDITORIA:
 * - Todas as mensagens são logadas
 * - Sucessos e falhas são registrados
 * - Logs incluem tópico, payload e resultado
 */
@Component // Spring: marca como componente gerenciado
public class MqttListener {

    /**
     * LOGGER PARA AUDITORIA E DEBUGGING
     * 
     * Registra todas as atividades MQTT:
     * - Mensagens recebidas
     * - Processamentos realizados
     * - Erros encontrados
     * - Métricas de performance
     */
    private static final Logger logger = LoggerFactory.getLogger(MqttListener.class);
    
    /**
     * SERVIÇO DE PROCESSAMENTO DE ATTENDANCE
     * 
     * Responsável por:
     * - Validar mensagens MQTT
     * - Processar lógica de check-in/check-out
     * - Interagir com repositórios
     */
    @Autowired
    private MqttAttendanceService attendanceService;
    
    /**
     * PROCESSADOR DE EVENTOS DE ATTENDANCE
     * 
     * Camada adicional de processamento:
     * - Orquestração de serviços
     * - Lógica de negócio complexa
     * - Integrações externas
     */
    @Autowired
    private MqttAttendanceProcessor attendanceProcessor;

    /**
     * MÉTODO PRINCIPAL - PROCESSA MENSAGENS MQTT
     * 
     * Ponto de entrada para todas as mensagens MQTT.
     * Ativado automaticamente pelo Spring Integration.
     * 
     * FLUXO INTERNO:
     * 1. Extrai tópico dos headers
     * 2. Extrai payload da mensagem
     * 3. Registra log de auditoria
     * 4. Filtra por tópico de attendance
     * 5. Roteia para processador específico
     * 6. Trata erros sem quebrar sistema
     * 
     * SPRING INTEGRATION:
     * - @ServiceActivator conecta ao canal MQTT
     * - inputChannel="mqttInputChannel" define origem
     * - Message<?> contém payload + headers
     * 
     * HEADERS IMPORTANTES:
     * - mqtt_receivedTopic: tópico original
     * - mqtt_receivedQos: qualidade de serviço
     * - mqtt_receivedRetained: mensagem retida
     * 
     * TRATAMENTO DE ERROS:
     * - Try-catch global impede quebra do listener
     * - Erros são logados mas sistema continua
     * - Mensagens problemáticas são descartadas
     * 
     * @param message Mensagem Spring Integration (payload + headers)
     */
    @ServiceActivator(inputChannel = "mqttInputChannel") // Canal configurado no Spring Integration
    public void onMessage(Message<?> message) {
        try {
            // ETAPA 1: Extração de metadados da mensagem
            String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
            String payload = message.getPayload().toString();
            
            // ETAPA 2: Log de auditoria (todas as mensagens)
            logger.info("[MQTT] Mensagem recebida - Tópico: {}, Payload: {}", topic, payload);
            
            // ETAPA 3: Filtro por tópico
            if (isAttendanceTopic(topic)) {
                // Roteia para processador de attendance
                onAttendanceMessage(topic, payload);
            } else {
                // Log de tópico ignorado (não é erro)
                logger.debug("[MQTT] Tópico ignorado (não é attendance): {}", topic);
            }
            
        } catch (Exception e) {
            // TRATAMENTO GLOBAL DE ERROS
            // Sistema nunca para por erro em mensagem MQTT
            logger.error("[MQTT] Erro ao processar mensagem: {}", e.getMessage(), e);
        }
    }

    /**
     * PROCESSADOR ESPECÍFICO - MENSAGENS DE ATTENDANCE
     * 
     * Método dedicado para processar mensagens de ponto eletrônico.
     * Separado do método principal para clareza e manutenção.
     * 
     * FORMATO ESPERADO DO PAYLOAD:
     * {
     *   "deviceId": "device001",
     *   "rfidTag": "RFID001",
     *   "eventType": "check-in",
     *   "timestamp": "2024-10-30T08:00:00"
     * }
     * 
     * FLUXO DE PROCESSAMENTO:
     * 1. Parse do JSON payload
     * 2. Extração de campos obrigatórios
     * 3. Chamada do processador de eventos
     * 4. Log de resultado (sucesso/erro)
     * 
     * VALIDAÇÕES REALIZADAS:
     * - JSON válido
     * - Campos obrigatórios presentes
     * - RFID cadastrado no sistema
     * - Evento válido (check-in/check-out)
     * 
     * TIPOS DE ERRO:
     * - IllegalArgumentException: erro de validação (WARNING)
     * - Exception: erro inesperado (ERROR)
     * 
     * @param topic Tópico MQTT original
     * @param payload JSON com dados do evento
     */
    public void onAttendanceMessage(String topic, String payload) {
        try {
            // Log do início do processamento
            logger.debug("[MQTT] Processando attendance - Tópico: {}", topic);
            
            // ETAPA 1: Parse do JSON payload
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode json = mapper.readTree(payload);
            
            // ETAPA 2: Extração de campos obrigatórios
            String deviceId = json.get("deviceId").asText();   // ID do dispositivo
            String rfidTag = json.get("rfidTag").asText();     // Tag RFID lida
            String eventType = json.get("eventType").asText(); // check-in ou check-out
            
            // ETAPA 3: Processamento do evento
            var attendance = attendanceProcessor.processAttendanceEvent(rfidTag, eventType, deviceId);
            
            // ETAPA 4: Log de sucesso com detalhes
            logger.info("[MQTT] ✅ Ponto processado - ID: {}, Funcionário: {}, Evento: {}", 
                       attendance.getId(), 
                       attendance.getEmployee().getName(),
                       eventType);
            
        } catch (IllegalArgumentException e) {
            // ERRO DE VALIDAÇÃO (não crítico)
            // Exemplos: RFID não encontrado, evento inválido
            logger.warn("[MQTT] ⚠️ Validação falhou: {}", e.getMessage());
            
        } catch (Exception e) {
            // ERRO INESPERADO (requer investigação)
            // Exemplos: erro de banco, JSON malformado
            logger.error("[MQTT] ❌ Erro inesperado: {}", e.getMessage(), e);
        }
    }

    /**
     * FILTRO DE TÓPICOS - IDENTIFICA MENSAGENS DE ATTENDANCE
     * 
     * Método auxiliar para determinar se uma mensagem MQTT
     * deve ser processada como evento de ponto eletrônico.
     * 
     * PADRÕES ACEITOS:
     * - attendance/device001/check-in
     * - attendance/device002/check-out
     * - attendance/{qualquer-coisa}
     * 
     * PADRÕES REJEITADOS:
     * - system/health
     * - config/update
     * - null ou vazio
     * 
     * LÓGICA:
     * - Tópico deve existir (não null)
     * - Deve começar com "attendance/"
     * - Case-sensitive
     * 
     * @param topic Tópico MQTT a verificar
     * @return true se deve processar como attendance, false caso contrário
     */
    private boolean isAttendanceTopic(String topic) {
        // Validação simples: começa com "attendance/"
        return topic != null && topic.startsWith("attendance/");
    }
}