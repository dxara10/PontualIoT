package com.pontualiot.demo.tdd;

// ========================================
// IMPORTAÇÕES PARA TESTES TDD
// ========================================
import com.pontualiot.demo.entity.Employee;           // Entidade testada
import com.pontualiot.demo.repository.EmployeeRepository; // Repositório para setup
import org.junit.jupiter.api.BeforeEach;              // Setup antes de cada teste
import org.junit.jupiter.api.Test;                    // Marcação de teste
import org.springframework.beans.factory.annotation.Autowired; // Injeção de dependência
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; // MockMvc
import org.springframework.boot.test.context.SpringBootTest; // Contexto completo Spring
import org.springframework.test.context.ActiveProfiles;      // Profile de teste
import org.springframework.test.web.servlet.MockMvc;         // Simulação de requisições HTTP
import org.springframework.transaction.annotation.Transactional; // Rollback automático
import org.testcontainers.junit.jupiter.Testcontainers;     // Containers Docker para teste

import static org.assertj.core.api.Assertions.assertThat;   // Asserções fluentes
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post; // Builder POST
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;      // Matchers de resposta

/**
 * ========================================
 * TESTE TDD - ATTENDANCE TEST CONTROLLER
 * ========================================
 * 
 * METODOLOGIA TDD (Test-Driven Development):
 * 
 * 1. RED: Escrever teste que falha
 *    - Teste define comportamento esperado
 *    - Execução falha porque código não existe
 * 
 * 2. GREEN: Implementar código mínimo para passar
 *    - Criar AttendanceTestController
 *    - Implementar endpoints básicos
 *    - Fazer teste passar
 * 
 * 3. REFACTOR: Melhorar qualidade do código
 *    - Extrair lógica para services
 *    - Adicionar validações
 *    - Otimizar performance
 * 
 * FLUXO DE TESTE COMPLETO:
 * 1. @BeforeEach cria Employee de teste
 * 2. Cada @Test simula requisição HTTP
 * 3. MockMvc executa controller sem servidor
 * 4. Asserções validam resposta JSON
 * 5. @Transactional faz rollback automático
 * 
 * VALIDAÇÕES TESTADAS:
 * - Check-in cria novo registro
 * - Check-out atualiza registro existente
 * - Employee inexistente lança exceção
 * - JSON response tem estrutura correta
 * 
 * INFRAESTRUTURA DE TESTE:
 * - @SpringBootTest: contexto completo da aplicação
 * - @AutoConfigureMockMvc: MockMvc configurado automaticamente
 * - @Testcontainers: PostgreSQL em container Docker
 * - @ActiveProfiles("test"): usa application-test.yml
 * - @Transactional: rollback após cada teste
 */
@SpringBootTest // Carrega contexto completo Spring Boot
@AutoConfigureMockMvc // Configura MockMvc para testes HTTP
@Testcontainers // Habilita containers Docker para teste
@ActiveProfiles("test") // Usa profile de teste (PostgreSQL em container)
@Transactional // Rollback automático após cada teste
class AttendanceTestControllerTDDTest {

    /**
     * MOCK MVC - SIMULAÇÃO DE REQUISIÇÕES HTTP
     * 
     * Permite testar controllers sem subir servidor HTTP real.
     * Simula requisições, executa controllers e valida respostas.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * REPOSITÓRIO PARA SETUP DE DADOS DE TESTE
     * 
     * Usado no @BeforeEach para criar Employee de teste.
     * Dados são persistidos no PostgreSQL de teste.
     */
    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * EMPLOYEE DE TESTE
     * 
     * Criado antes de cada teste e usado nas requisições.
     * Garante isolamento entre testes.
     */
    private Employee testEmployee;

    /**
     * SETUP EXECUTADO ANTES DE CADA TESTE
     * 
     * FLUXO:
     * 1. Cria Employee com dados de teste
     * 2. Persiste no banco via repository.save()
     * 3. Employee fica disponível para uso nos testes
     * 4. @Transactional garante rollback após teste
     * 
     * DADOS DE TESTE:
     * - name: "TDD Test Employee"
     * - email: "tdd.test@controller.com" (deve ser único)
     * - rfidTag: "TDD_CTRL_001" (deve ser único)
     * - active: true
     */
    @BeforeEach
    void setUp() {
        // Cria Employee usando Builder pattern
        testEmployee = Employee.builder()
                .name("TDD Test Employee")
                .email("tdd.test@controller.com")
                .rfidTag("TDD_CTRL_001")
                .active(true)
                .build();
        
        // Persiste no banco e obtém ID gerado
        testEmployee = employeeRepository.save(testEmployee);
    }

    /**
     * TESTE TDD: CHECK-IN VIA ENDPOINT DE TESTE
     * 
     * CENÁRIO: Funcionário faz check-in pela primeira vez no dia
     * 
     * FLUXO TESTADO:
     * 1. POST /api/test-attendance/check-in/{employeeId}
     * 2. Controller busca Employee por ID
     * 3. Verifica se já tem registro hoje
     * 4. Como não tem, cria novo Attendance
     * 5. Define checkIn = agora, checkOut = null
     * 6. Retorna 200 OK + JSON do Attendance
     * 
     * VALIDAÇÕES:
     * - Status HTTP 200 OK
     * - JSON contém employee.id correto
     * - JSON contém checkIn preenchido
     * - JSON NÃO contém checkOut (deve ser null)
     * 
     * TDD CYCLE:
     * - RED: Teste falha - endpoint não existe
     * - GREEN: Implementa AttendanceTestController.checkIn()
     * - REFACTOR: Extrai lógica para AttendanceService
     */
    @Test
    void shouldCreateCheckInViaTestEndpoint() throws Exception {
        // TDD RED: Teste falha inicialmente - endpoint não existe
        
        // WHEN - Simula POST para endpoint de check-in
        mockMvc.perform(post("/api/test-attendance/check-in/" + testEmployee.getId()))
                
                // THEN - Valida resposta HTTP e JSON
                .andExpect(status().isOk())  // HTTP 200 OK
                .andExpect(jsonPath("$.employee.id").value(testEmployee.getId())) // Employee correto
                .andExpect(jsonPath("$.checkIn").exists())     // CheckIn preenchido
                .andExpect(jsonPath("$.checkOut").doesNotExist()); // CheckOut null
    }

    /**
     * TESTE TDD: CHECK-OUT VIA ENDPOINT DE TESTE
     * 
     * CENÁRIO: Funcionário já fez check-in e agora faz check-out
     * 
     * FLUXO TESTADO:
     * 1. Primeiro: POST check-in (setup)
     * 2. Depois: POST check-out (teste real)
     * 3. Controller busca registro existente do dia
     * 4. Atualiza checkOut = agora
     * 5. Retorna 200 OK + JSON atualizado
     * 
     * VALIDAÇÕES:
     * - Status HTTP 200 OK
     * - JSON contém employee.id correto
     * - JSON contém checkIn (do check-in anterior)
     * - JSON contém checkOut (recém preenchido)
     * 
     * REGRA DE NEGÓCIO TESTADA:
     * - Um funcionário pode ter apenas 1 registro por dia
     * - Check-out atualiza registro existente
     * - Não cria novo registro
     */
    @Test
    void shouldCreateCheckOutViaTestEndpoint() throws Exception {
        // GIVEN - Funcionário já tem check-in (setup do cenário)
        mockMvc.perform(post("/api/test-attendance/check-in/" + testEmployee.getId()));

        // WHEN - Simula POST para endpoint de check-out
        mockMvc.perform(post("/api/test-attendance/check-out/" + testEmployee.getId()))
                
                // THEN - Valida que registro foi atualizado
                .andExpect(status().isOk())  // HTTP 200 OK
                .andExpect(jsonPath("$.employee.id").value(testEmployee.getId())) // Employee correto
                .andExpect(jsonPath("$.checkIn").exists())  // CheckIn ainda existe
                .andExpect(jsonPath("$.checkOut").exists()); // CheckOut agora existe
    }

    /**
     * TESTE TDD: VALIDAÇÃO DE EMPLOYEE INEXISTENTE
     * 
     * CENÁRIO: Tentativa de check-in com ID inválido
     * 
     * FLUXO TESTADO:
     * 1. POST /api/test-attendance/check-in/99999
     * 2. Controller busca Employee por ID = 99999
     * 3. employeeRepository.findById(99999) retorna Optional.empty()
     * 4. Controller lança RuntimeException("Employee not found")
     * 5. Spring MVC envolve em ServletException
     * 
     * VALIDAÇÕES:
     * - Exceção é lançada
     * - Tipo: RuntimeException
     * - Mensagem: "Employee not found"
     * 
     * TRATAMENTO DE ERRO TESTADO:
     * - Sistema não permite registros para employees inexistentes
     * - Falha rápida com mensagem clara
     * - Não corrompe dados no banco
     * 
     * NOTA: Em produção, seria melhor retornar 404 Not Found
     * ao invés de lançar RuntimeException.
     */
    @Test
    void shouldReturn404ForNonExistentEmployee() throws Exception {
        // WHEN - Simula POST com employee ID inexistente
        // THEN - Deve lançar exceção (controller lança RuntimeException)
        try {
            mockMvc.perform(post("/api/test-attendance/check-in/99999"));
            
            // Se chegou aqui, o teste deve falhar
            assertThat(false).as("Expected exception was not thrown").isTrue();
            
        } catch (Exception e) {
            // EXPECTED - Controller deve lançar RuntimeException envolvida em ServletException
            assertThat(e.getCause()).isInstanceOf(RuntimeException.class);
            assertThat(e.getCause().getMessage()).isEqualTo("Employee not found");
        }
    }
}