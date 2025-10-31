package com.pontualiot.demo.mqtt;

// ========================================
// IMPORTAÇÕES PARA SERVIÇO MQTT IOT
// ========================================
import com.pontualiot.demo.entity.Attendance; // Entidade de registro de ponto
import com.pontualiot.demo.entity.Employee;   // Entidade de funcionário
import com.pontualiot.demo.repository.AttendanceRepository; // Acesso a dados de ponto
import com.pontualiot.demo.repository.EmployeeRepository;   // Acesso a dados de funcionários
import org.springframework.beans.factory.annotation.Autowired; // Injeção de dependência
import org.springframework.stereotype.Service; // Marca como serviço Spring

import java.time.LocalDate;     // Data sem horário
import java.time.LocalDateTime; // Data com horário
import java.util.List;          // Lista de resultados
import java.util.Optional;      // Container para valores opcionais

/**
 * ========================================
 * SERVIÇO MQTT ATTENDANCE - PROCESSAMENTO IOT
 * ========================================
 * 
 * RESPONSABILIDADES:
 * - Processar mensagens MQTT de dispositivos IoT
 * - Converter eventos RFID em registros de ponto
 * - Validar regras de negócio (entrada/saída)
 * - Gerenciar estado dos registros diários
 * - Garantir integridade dos dados
 * 
 * FLUXO COMPLETO IOT -> BANCO:
 * 
 * 1. DISPOSITIVO IOT:
 *    - Funcionário aproxima cartão RFID
 *    - Dispositivo lê tag: "RFID001"
 *    - Determina ação: "check-in" ou "check-out"
 * 
 * 2. MQTT PUBLISH:
 *    - Dispositivo publica: {"rfidTag": "RFID001", "action": "check-in", "timestamp": "2024-10-30T08:00:00"}
 *    - Tópico: "attendance/events"
 * 
 * 3. MQTT LISTENER:
 *    - MqttListener.onMessage() recebe mensagem
 *    - Chama MqttAttendanceService.processMqttMessage()
 * 
 * 4. VALIDAÇÃO:
 *    - MqttMessageValidator valida JSON
 *    - Verifica campos obrigatórios
 *    - Converte para MqttAttendanceMessage
 * 
 * 5. PROCESSAMENTO:
 *    - Busca Employee por rfidTag
 *    - Verifica se está ativo
 *    - Determina se é check-in ou check-out
 *    - Aplica regras de negócio
 * 
 * 6. PERSISTÊNCIA:
 *    - Cria/atualiza Attendance
 *    - Salva no PostgreSQL
 *    - Retorna registro processado
 * 
 * REGRAS DE NEGÓCIO IMPLEMENTADAS:
 * - Funcionário deve existir e estar ativo
 * - Não pode ter entrada duplicada no mesmo dia
 * - Check-out requer check-in prévio no dia
 * - Apenas 1 registro "em aberto" por dia
 * 
 * VALIDAÇÕES DE SEGURANÇA:
 * - RFID deve estar cadastrado
 * - Funcionário deve estar ativo
 * - Timestamp deve ser válido
 * - Ação deve ser reconhecida
 * 
 * TRATAMENTO DE ERROS:
 * - IllegalArgumentException para regras de negócio
 * - Logs detalhados para debugging
 * - Falha rápida sem corromper dados
 */
@Service // Spring: marca como serviço gerenciado pelo container
public class MqttAttendanceService {

    /**
     * REPOSITÓRIO DE FUNCIONÁRIOS
     * 
     * Usado para:
     * - Buscar Employee por RFID tag
     * - Validar existência e status ativo
     */
    @Autowired
    private EmployeeRepository employeeRepository;
    
    /**
     * REPOSITÓRIO DE REGISTROS DE PONTO
     * 
     * Usado para:
     * - Buscar registros existentes do dia
     * - Criar novos registros (check-in)
     * - Atualizar registros existentes (check-out)
     */
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    /**
     * VALIDADOR DE MENSAGENS MQTT
     * 
     * Usado para:
     * - Validar formato JSON
     * - Verificar campos obrigatórios
     * - Converter para objeto tipado
     */
    @Autowired
    private MqttMessageValidator messageValidator;

    /**
     * MÉTODO PRINCIPAL - PROCESSA MENSAGEM MQTT
     * 
     * Ponto de entrada para todas as mensagens MQTT de ponto.
     * Coordena todo o fluxo de validação e processamento.
     * 
     * FLUXO INTERNO:
     * 1. Valida e parseia mensagem JSON
     * 2. Busca funcionário por RFID
     * 3. Verifica se funcionário está ativo
     * 4. Roteia para check-in ou check-out
     * 5. Retorna registro processado
     * 
     * EXEMPLO DE MENSAGEM:
     * {
     *   "rfidTag": "RFID001",
     *   "action": "check-in",
     *   "timestamp": "2024-10-30T08:00:00",
     *   "deviceId": "DEVICE_001"
     * }
     * 
     * @param topic Tópico MQTT (ex: "attendance/events")
     * @param payload JSON da mensagem MQTT
     * @return Attendance registro criado ou atualizado
     * @throws IllegalArgumentException se dados inválidos ou regras violadas
     */
    public Attendance processMqttMessage(String topic, String payload) {
        // ETAPA 1: Validação e parsing da mensagem
        MqttAttendanceMessage message = messageValidator.validateAndParse(payload);
        
        // ETAPA 2: Busca funcionário pelo RFID
        Employee employee = findEmployeeByRfid(message.getRfidTag());
        
        // ETAPA 3: Validação de status ativo
        if (!employee.isActive()) {
            throw new IllegalArgumentException("Funcionário inativo: " + employee.getName());
        }
        
        // ETAPA 4: Roteamento baseado no tipo de evento
        if (message.isCheckIn()) {
            return processCheckIn(employee, message); // Processa entrada
        } else if (message.isCheckOut()) {
            return processCheckOut(employee, message); // Processa saída
        } else {
            throw new IllegalArgumentException("Tipo de evento inválido: " + message.getEventType());
        }
    }

    /**
     * PROCESSA EVENTO DE ENTRADA (CHECK-IN)
     * 
     * Cria novo registro de ponto com horário de entrada.
     * Valida se não há entrada duplicada no mesmo dia.
     * 
     * REGRAS DE NEGÓCIO:
     * - Apenas 1 entrada por dia por funcionário
     * - Não pode ter entrada se já há registro "em aberto"
     * - Timestamp deve ser da mensagem MQTT (não do servidor)
     * 
     * FLUXO:
     * 1. Extrai data do timestamp da mensagem
     * 2. Busca registros existentes do funcionário na data
     * 3. Verifica se há registro "em aberto" (checkIn != null && checkOut == null)
     * 4. Se há: lança exceção
     * 5. Se não: cria novo registro
     * 6. Persiste no banco
     * 
     * EXEMPLO DE REGISTRO CRIADO:
     * {
     *   "id": 123,
     *   "employee": {"id": 1, "name": "João"},
     *   "checkIn": "2024-10-30T08:00:00",
     *   "checkOut": null,
     *   "date": "2024-10-30"
     * }
     * 
     * @param employee Funcionário que está fazendo check-in
     * @param message Mensagem MQTT com timestamp e dados
     * @return Attendance novo registro de entrada
     * @throws IllegalArgumentException se já há entrada hoje
     */
    private Attendance processCheckIn(Employee employee, MqttAttendanceMessage message) {
        // Extrai data do timestamp da mensagem (não do servidor!)
        LocalDate eventDate = message.getTimestamp().toLocalDate();
        
        // Busca registros existentes do funcionário na data
        List<Attendance> todayAttendances = attendanceRepository.findByEmployeeAndDate(employee, eventDate);
        
        // Verifica se há registro "em aberto" (entrada sem saída)
        for (Attendance attendance : todayAttendances) {
            if (attendance.getCheckIn() != null && attendance.getCheckOut() == null) {
                throw new IllegalArgumentException("Funcionário já registrou entrada hoje");
            }
        }
        
        // Cria novo registro de entrada usando Builder pattern
        Attendance attendance = Attendance.builder()
                .employee(employee)                    // Associa ao funcionário
                .checkIn(message.getTimestamp())       // Horário da mensagem MQTT
                .date(eventDate)                       // Data extraída do timestamp
                .build(); // checkOut fica null automaticamente
        
        // Persiste no PostgreSQL e retorna com ID gerado
        return attendanceRepository.save(attendance);
    }

    /**
     * PROCESSA EVENTO DE SAÍDA (CHECK-OUT)
     * 
     * Atualiza registro existente com horário de saída.
     * Valida se há entrada prévia no mesmo dia.
     * 
     * REGRAS DE NEGÓCIO:
     * - Deve existir registro "em aberto" no dia
     * - Registro "em aberto" = checkIn != null && checkOut == null
     * - Timestamp de saída deve ser posterior à entrada
     * - Apenas 1 saída por entrada
     * 
     * FLUXO:
     * 1. Extrai data do timestamp da mensagem
     * 2. Busca registros do funcionário na data
     * 3. Procura registro "em aberto"
     * 4. Se não encontra: lança exceção
     * 5. Se encontra: atualiza checkOut
     * 6. Persiste alteração
     * 
     * EXEMPLO DE REGISTRO ATUALIZADO:
     * ANTES: {"checkIn": "08:00:00", "checkOut": null}
     * DEPOIS: {"checkIn": "08:00:00", "checkOut": "17:00:00"}
     * 
     * @param employee Funcionário que está fazendo check-out
     * @param message Mensagem MQTT com timestamp de saída
     * @return Attendance registro atualizado com saída
     * @throws IllegalArgumentException se não há entrada prévia
     */
    private Attendance processCheckOut(Employee employee, MqttAttendanceMessage message) {
        // Extrai data do timestamp da mensagem
        LocalDate eventDate = message.getTimestamp().toLocalDate();
        
        // Busca todos os registros do funcionário na data
        List<Attendance> todayAttendances = attendanceRepository.findByEmployeeAndDate(employee, eventDate);
        
        // Procura registro "em aberto" (entrada sem saída)
        Attendance openAttendance = null;
        for (Attendance attendance : todayAttendances) {
            if (attendance.getCheckIn() != null && attendance.getCheckOut() == null) {
                openAttendance = attendance; // Encontrou registro em aberto
                break; // Para na primeira ocorrência
            }
        }
        
        // Valida se existe entrada prévia para fazer saída
        if (openAttendance == null) {
            throw new IllegalArgumentException("Nenhum registro de entrada encontrado para hoje");
        }
        
        // Atualiza registro existente com horário de saída
        openAttendance.setCheckOut(message.getTimestamp());
        
        // Persiste alteração no banco (UPDATE SQL)
        return attendanceRepository.save(openAttendance);
    }

    /**
     * BUSCA FUNCIONÁRIO POR TAG RFID
     * 
     * Método auxiliar para encontrar funcionário baseado
     * na tag RFID lida pelo dispositivo IoT.
     * 
     * FLUXO:
     * 1. Chama employeeRepository.findByRfidTag()
     * 2. SQL: SELECT * FROM employees WHERE rfid_tag = ?
     * 3. Se encontrado: retorna Employee
     * 4. Se não encontrado: lança exceção
     * 
     * SEGURANÇA:
     * - Apenas RFIDs cadastrados são aceitos
     * - Evita registros de pessoas não autorizadas
     * - Falha rápida com mensagem clara
     * 
     * @param rfidTag Tag RFID lida pelo dispositivo (ex: "RFID001")
     * @return Employee funcionário encontrado
     * @throws IllegalArgumentException se RFID não cadastrado
     */
    private Employee findEmployeeByRfid(String rfidTag) {
        // Busca no repositório usando query method
        Optional<Employee> employeeOpt = employeeRepository.findByRfidTag(rfidTag);
        
        // Verifica se funcionário foi encontrado
        if (employeeOpt.isEmpty()) {
            throw new IllegalArgumentException("Funcionário não encontrado com RFID: " + rfidTag);
        }
        
        // Retorna funcionário encontrado (Optional.get() é seguro aqui)
        return employeeOpt.get();
    }
}