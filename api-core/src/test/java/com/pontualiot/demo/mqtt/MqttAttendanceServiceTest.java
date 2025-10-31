package com.pontualiot.demo.mqtt;

// ========================================
// IMPORTAÇÕES PARA TESTES TDD MQTT
// ========================================
import com.pontualiot.demo.entity.Attendance; // Entidade de registro de ponto
import com.pontualiot.demo.entity.Employee;   // Entidade de funcionário
import com.pontualiot.demo.repository.AttendanceRepository; // Repositório de registros
import com.pontualiot.demo.repository.EmployeeRepository;   // Repositório de funcionários
import org.junit.jupiter.api.BeforeEach; // Setup antes de cada teste
import org.junit.jupiter.api.Test;       // Marca método como teste
import org.springframework.beans.factory.annotation.Autowired; // Injeção de dependência
import org.springframework.boot.test.context.SpringBootTest;   // Teste de integração Spring
import org.springframework.test.context.ActiveProfiles;       // Profile de teste
import org.springframework.transaction.annotation.Transactional; // Transação para rollback
import org.testcontainers.junit.jupiter.Testcontainers; // Testcontainers

import java.time.LocalDate;     // Data local
import java.time.LocalDateTime; // Data e hora local
import java.util.List;          // Lista de registros

import static org.assertj.core.api.Assertions.assertThat; // Assertions fluentes
import static org.junit.jupiter.api.Assertions.assertThrows; // Verificação de exceções

/**
 * Testes TDD para serviço MQTT de registros de ponto
 * 
 * Testa o processamento de mensagens MQTT vindas de dispositivos IoT
 * para registrar entrada e saída de funcionários via RFID.
 */
@SpringBootTest // Carrega contexto completo do Spring
@Testcontainers // Habilita Testcontainers
@ActiveProfiles("test") // Usa profile de teste
@Transactional // Rollback automático após cada teste
class MqttAttendanceServiceTest {

    // Injeção dos repositórios para setup de dados
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    // Serviço que será implementado (agora existe - TDD Green)
    @Autowired
    private MqttAttendanceService mqttAttendanceService;
    
    // Funcionário de teste
    private Employee testEmployee;

    /**
     * Setup executado antes de cada teste
     * Cria funcionário de teste no banco
     */
    @BeforeEach
    void setUp() {
        // Cria funcionário para testes
        testEmployee = Employee.builder()
                .name("João MQTT Test")           // Nome do funcionário
                .email("joao.mqtt@test.com")     // Email único
                .rfidTag("MQTT_RFID_001")        // Tag RFID para dispositivo IoT
                .active(true)                    // Funcionário ativo
                .build();
        
        // Salva no banco via repositório
        testEmployee = employeeRepository.save(testEmployee);
        
        // MqttAttendanceService agora é injetado automaticamente pelo Spring
        // Não precisa instanciar manualmente
    }

    /**
     * TDD RED: Teste que deve falhar - processamento de mensagem MQTT de check-in
     * 
     * Testa se o serviço consegue processar uma mensagem MQTT
     * de entrada (check-in) vinda de um dispositivo IoT.
     */
    @Test
    void shouldProcessMqttCheckInMessage() {
        // Given - Mensagem MQTT simulada de dispositivo IoT
        String mqttTopic = "devices/DEVICE_001/attendance";     // Tópico MQTT
        String mqttPayload = """
            {
                "deviceId": "DEVICE_001",
                "rfidTag": "MQTT_RFID_001",
                "eventType": "CHECK_IN",
                "timestamp": "2024-01-15T08:30:00"
            }
            """; // Payload JSON da mensagem MQTT
        
        // When - Processa mensagem MQTT (método agora existe)
        Attendance result = mqttAttendanceService.processMqttMessage(mqttTopic, mqttPayload);
        
        // Then - Verifica se registro foi criado corretamente
        assertThat(result).isNotNull();
        assertThat(result.getEmployee().getId()).isEqualTo(testEmployee.getId());
        assertThat(result.getCheckIn()).isNotNull();
        assertThat(result.getCheckOut()).isNull(); // Ainda não fez check-out
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2024, 1, 15));
    }

    /**
     * TDD RED: Teste que deve falhar - processamento de mensagem MQTT de check-out
     * 
     * Testa se o serviço consegue processar uma mensagem MQTT
     * de saída (check-out) vinda de um dispositivo IoT.
     */
    @Test
    void shouldProcessMqttCheckOutMessage() {
        // Given - Funcionário já fez check-in hoje
        Attendance existingCheckIn = Attendance.builder()
                .employee(testEmployee)                           // Funcionário
                .checkIn(LocalDateTime.of(2024, 1, 15, 8, 30))   // Horário de entrada
                .date(LocalDate.of(2024, 1, 15))                 // Data
                .build();
        attendanceRepository.save(existingCheckIn); // Salva check-in existente
        
        // Mensagem MQTT de check-out
        String mqttTopic = "devices/DEVICE_001/attendance";
        String mqttPayload = """
            {
                "deviceId": "DEVICE_001",
                "rfidTag": "MQTT_RFID_001",
                "eventType": "CHECK_OUT",
                "timestamp": "2024-01-15T17:30:00"
            }
            """; // Payload JSON da mensagem MQTT
        
        // When - Processa mensagem MQTT (método agora existe)
        Attendance result = mqttAttendanceService.processMqttMessage(mqttTopic, mqttPayload);
        
        // Then - Verifica se check-out foi registrado
        assertThat(result).isNotNull();
        assertThat(result.getCheckIn()).isNotNull();  // Mantém check-in original
        assertThat(result.getCheckOut()).isNotNull(); // Agora tem check-out
        assertThat(result.getCheckOut()).isEqualTo(LocalDateTime.of(2024, 1, 15, 17, 30));
    }

    /**
     * TDD RED: Teste que deve falhar - validação de RFID inválido
     * 
     * Testa se o serviço rejeita mensagens MQTT com RFID
     * de funcionário inexistente ou inativo.
     */
    @Test
    void shouldRejectInvalidRfidInMqttMessage() {
        // Given - Mensagem MQTT com RFID inexistente
        String mqttTopic = "devices/DEVICE_001/attendance";
        String mqttPayload = """
            {
                "deviceId": "DEVICE_001",
                "rfidTag": "RFID_INEXISTENTE",
                "eventType": "CHECK_IN",
                "timestamp": "2024-01-15T08:30:00"
            }
            """; // RFID que não existe no banco
        
        // When & Then - Deve lançar exceção para RFID inválido
        assertThrows(IllegalArgumentException.class, () -> {
            mqttAttendanceService.processMqttMessage(mqttTopic, mqttPayload);
        });
    }

    /**
     * TDD RED: Teste que deve falhar - validação de payload MQTT malformado
     * 
     * Testa se o serviço rejeita mensagens MQTT com
     * JSON inválido ou campos obrigatórios faltando.
     */
    @Test
    void shouldRejectMalformedMqttPayload() {
        // Given - Payload MQTT com JSON inválido
        String mqttTopic = "devices/DEVICE_001/attendance";
        String invalidPayload = """
            {
                "deviceId": "DEVICE_001",
                // JSON inválido - campo rfidTag faltando
                "eventType": "CHECK_IN"
            }
            """; // JSON malformado
        
        // When & Then - Deve lançar exceção para payload inválido
        assertThrows(IllegalArgumentException.class, () -> {
            mqttAttendanceService.processMqttMessage(mqttTopic, invalidPayload);
        });
    }

    /**
     * TDD RED: Teste que deve falhar - prevenção de check-in duplicado
     * 
     * Testa se o serviço impede múltiplos check-ins
     * do mesmo funcionário no mesmo dia.
     */
    @Test
    void shouldPreventDuplicateCheckIn() {
        // Given - Funcionário já fez check-in hoje
        Attendance existingCheckIn = Attendance.builder()
                .employee(testEmployee)                           // Funcionário
                .checkIn(LocalDateTime.of(2024, 1, 15, 8, 30))   // Horário de entrada
                .date(LocalDate.of(2024, 1, 15))                 // Data
                .build();
        attendanceRepository.save(existingCheckIn); // Salva check-in existente
        
        // Tentativa de segundo check-in no mesmo dia
        String mqttTopic = "devices/DEVICE_001/attendance";
        String mqttPayload = """
            {
                "deviceId": "DEVICE_001",
                "rfidTag": "MQTT_RFID_001",
                "eventType": "CHECK_IN",
                "timestamp": "2024-01-15T09:00:00"
            }
            """; // Segundo check-in no mesmo dia
        
        // When & Then - Deve lançar exceção para check-in duplicado
        assertThrows(IllegalArgumentException.class, () -> {
            mqttAttendanceService.processMqttMessage(mqttTopic, mqttPayload);
        });
    }

    /**
     * TDD RED: Teste que deve falhar - check-out sem check-in
     * 
     * Testa se o serviço rejeita tentativa de check-out
     * quando não há check-in prévio no dia.
     */
    @Test
    void shouldRejectCheckOutWithoutCheckIn() {
        // Given - Nenhum check-in prévio hoje
        String mqttTopic = "devices/DEVICE_001/attendance";
        String mqttPayload = """
            {
                "deviceId": "DEVICE_001",
                "rfidTag": "MQTT_RFID_001",
                "eventType": "CHECK_OUT",
                "timestamp": "2024-01-15T17:30:00"
            }
            """; // Check-out sem check-in prévio
        
        // When & Then - Deve lançar exceção para check-out sem check-in
        assertThrows(IllegalArgumentException.class, () -> {
            mqttAttendanceService.processMqttMessage(mqttTopic, mqttPayload);
        });
    }
}