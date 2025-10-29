package com.pontualiot.simulator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TDD Test para HttpClient
 * 
 * Testa envio de dados via HTTP para API
 */
class HttpClientTest {

    private AttendanceHttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = new AttendanceHttpClient("http://localhost:8082");
    }

    /**
     * TDD RED: Teste deve falhar - cliente deve enviar check-in via HTTP
     */
    @Test
    void shouldSendCheckInViaHttp() {
        // Given - Evento de check-in
        AttendanceEvent event = new AttendanceEvent(
            "DEVICE_001", 
            "RFID_123456", 
            "CHECK_IN"
        );
        
        // When - Envia via HTTP
        boolean success = httpClient.sendAttendanceEvent(event);
        
        // Then - Deve enviar com sucesso
        assertTrue(success);
    }

    /**
     * TDD RED: Teste deve falhar - cliente deve criar employee via HTTP
     */
    @Test
    void shouldCreateEmployeeViaHttp() {
        // Given - Dados do employee
        Employee employee = new Employee(
            "João Silva", 
            "joao@test.com", 
            "RFID_123456"
        );
        
        // When - Cria employee via HTTP
        Employee created = httpClient.createEmployee(employee);
        
        // Then - Deve retornar employee criado
        assertNotNull(created);
        assertNotNull(created.getId());
        assertEquals("João Silva", created.getName());
    }
}