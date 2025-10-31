package com.pontualiot.demo.controller;

// ========================================
// IMPORTAÇÕES PARA CONTROLLER DE TESTES
// ========================================
import com.pontualiot.demo.entity.Attendance;         // Entidade de registro de ponto
import com.pontualiot.demo.entity.Employee;           // Entidade de funcionário
import com.pontualiot.demo.repository.AttendanceRepository; // Repositório de registros
import com.pontualiot.demo.repository.EmployeeRepository;   // Repositório de funcionários
import io.swagger.v3.oas.annotations.Operation;       // Documentação OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;        // Agrupamento de endpoints
import org.springframework.beans.factory.annotation.Autowired; // Injeção de dependência
import org.springframework.http.ResponseEntity;        // Wrapper para respostas HTTP
import org.springframework.web.bind.annotation.*;      // Anotações REST

import java.time.LocalDate;     // Data sem horário
import java.time.LocalDateTime; // Data com horário

/**
 * ========================================
 * CONTROLLER DE TESTES - ATTENDANCE TEST
 * ========================================
 * 
 * PROPÓSITO:
 * - Endpoints temporários para testes de desenvolvimento
 * - Simulação de registros de ponto sem MQTT
 * - Validação de lógica de negócio
 * - Suporte aos testes TDD automatizados
 * 
 * IMPORTANTE: APENAS PARA DESENVOLVIMENTO!
 * Em produção, estes endpoints devem ser removidos
 * ou protegidos com autenticação administrativa.
 * 
 * ENDPOINTS EXPOSTOS:
 * - POST /api/test-attendance/check-in/{employeeId}
 * - POST /api/test-attendance/check-out/{employeeId}
 * 
 * CASOS DE USO:
 * - Testes manuais da API
 * - Validação de regras de negócio
 * - Criação de dados de teste
 * - Debugging de problemas
 * - Testes TDD automatizados
 * 
 * FLUXO TÍPICO DE TESTE:
 * 1. Criar Employee via POST /api/employees
 * 2. Fazer check-in via POST /api/test-attendance/check-in/{id}
 * 3. Verificar registro via GET /api/attendances/employee/{id}
 * 4. Fazer check-out via POST /api/test-attendance/check-out/{id}
 * 5. Validar jornada completa
 * 
 * DIFERENÇAS DO FLUXO MQTT:
 * - Não usa RFID tags
 * - Não valida mensagens MQTT
 * - Usa timestamp do servidor (não do dispositivo)
 * - Acesso direto por ID (não por RFID)
 * 
 * SEGURANÇA:
 * - Sem autenticação (apenas desenvolvimento)
 * - Valida existência de Employee
 * - Aplica mesmas regras de negócio
 * - Lança exceções em caso de erro
 */
@RestController // Spring: combina @Controller + @ResponseBody
@RequestMapping("/api/test-attendance") // Base path: /api/test-attendance
@Tag(name = "Test Attendance", description = "Temporary endpoints to test attendance creation") // OpenAPI
public class AttendanceTestController {

    /**
     * REPOSITÓRIO DE REGISTROS DE PONTO
     * 
     * Usado para:
     * - Criar novos registros de check-in
     * - Buscar registros existentes para check-out
     * - Atualizar registros com horário de saída
     */
    @Autowired
    private AttendanceRepository attendanceRepository;

    /**
     * REPOSITÓRIO DE FUNCIONÁRIOS
     * 
     * Usado para:
     * - Validar existência de Employee por ID
     * - Buscar dados completos do funcionário
     */
    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * ENDPOINT DE TESTE: CHECK-IN
     * 
     * POST /api/test-attendance/check-in/{employeeId}
     * 
     * SIMULA ENTRADA DE FUNCIONÁRIO SEM MQTT
     * 
     * CASOS DE USO:
     * - Testes manuais de check-in
     * - Criação de dados de teste
     * - Validação de lógica de entrada
     * - Testes TDD automatizados
     * 
     * FLUXO INTERNO:
     * 1. Recebe employeeId da URL
     * 2. Busca Employee no banco por ID
     * 3. Se não encontrado: lança RuntimeException
     * 4. Se encontrado: cria novo Attendance
     * 5. Define checkIn = agora (servidor)
     * 6. Define date = hoje
     * 7. Salva no banco
     * 8. Retorna 200 OK + JSON do registro
     * 
     * DIFERENÇAS DO MQTT:
     * - Usa ID ao invés de RFID
     * - Timestamp do servidor (não do dispositivo)
     * - Não valida duplicatas (pode criar múltiplas entradas)
     * - Não verifica status ativo do funcionário
     * 
     * EXEMPLO DE RESPOSTA:
     * {
     *   "id": 123,
     *   "employee": {
     *     "id": 1,
     *     "name": "João Silva",
     *     "email": "joao@empresa.com",
     *     "rfidTag": "RFID001",
     *     "active": true
     *   },
     *   "checkIn": "2024-10-30T08:00:00",
     *   "checkOut": null,
     *   "date": "2024-10-30",
     *   "createdAt": "2024-10-30T08:00:01"
     * }
     * 
     * @param employeeId ID do funcionário (extraido da URL)
     * @return ResponseEntity<Attendance> 200 OK + registro criado
     * @throws RuntimeException se Employee não encontrado
     */
    @PostMapping("/check-in/{employeeId}") // Mapeia POST /api/test-attendance/check-in/123
    @Operation(summary = "Create test check-in for employee") // Documentação OpenAPI
    public ResponseEntity<Attendance> createCheckIn(@PathVariable Long employeeId) {
        // ETAPA 1: Busca e valida Employee
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // ETAPA 2: Cria novo registro de entrada
        // Usa Builder pattern para construção limpa
        Attendance attendance = Attendance.builder()
                .employee(employee)              // Associa ao funcionário
                .checkIn(LocalDateTime.now())    // Timestamp do servidor
                .date(LocalDate.now())           // Data atual
                .build(); // checkOut fica null automaticamente

        // ETAPA 3: Persiste no banco e retorna
        return ResponseEntity.ok(attendanceRepository.save(attendance));
    }

    /**
     * ENDPOINT DE TESTE: CHECK-OUT
     * 
     * POST /api/test-attendance/check-out/{employeeId}
     * 
     * SIMULA SAÍDA DE FUNCIONÁRIO SEM MQTT
     * 
     * CASOS DE USO:
     * - Testes manuais de check-out
     * - Finalização de jornadas de teste
     * - Validação de lógica de saída
     * - Testes TDD de fluxo completo
     * 
     * FLUXO INTERNO:
     * 1. Recebe employeeId da URL
     * 2. Busca Employee no banco por ID
     * 3. Se não encontrado: lança RuntimeException
     * 4. Busca registros do funcionário hoje
     * 5. Filtra registros "em aberto" (checkOut == null)
     * 6. Se não encontrado: lança RuntimeException
     * 7. Se encontrado: atualiza checkOut = agora
     * 8. Salva alteração no banco
     * 9. Retorna 200 OK + JSON atualizado
     * 
     * REGRA DE NEGÓCIO APLICADA:
     * - Deve existir check-in prévio no mesmo dia
     * - Apenas registros "em aberto" podem receber check-out
     * - Um check-out por check-in
     * 
     * FLUXO DE BUSCA:
     * 1. attendanceRepository.findByEmployeeAndDate(employee, hoje)
     * 2. .stream().filter(a -> a.getCheckOut() == null)
     * 3. .findFirst() -> pega primeiro registro em aberto
     * 4. .orElseThrow() -> erro se nenhum encontrado
     * 
     * EXEMPLO DE RESPOSTA:
     * {
     *   "id": 123,
     *   "employee": {...},
     *   "checkIn": "2024-10-30T08:00:00",
     *   "checkOut": "2024-10-30T17:00:00",  // <- Recém preenchido
     *   "date": "2024-10-30",
     *   "createdAt": "2024-10-30T08:00:01"
     * }
     * 
     * @param employeeId ID do funcionário (extraido da URL)
     * @return ResponseEntity<Attendance> 200 OK + registro atualizado
     * @throws RuntimeException se Employee ou check-in não encontrado
     */
    @PostMapping("/check-out/{employeeId}") // Mapeia POST /api/test-attendance/check-out/123
    @Operation(summary = "Create test check-out for employee") // Documentação OpenAPI
    public ResponseEntity<Attendance> createCheckOut(@PathVariable Long employeeId) {
        // ETAPA 1: Busca e valida Employee
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // ETAPA 2: Busca registro "em aberto" do dia
        // Combina repository query + stream processing
        Attendance attendance = attendanceRepository.findByEmployeeAndDate(employee, LocalDate.now())
                .stream()                           // Converte List para Stream
                .filter(a -> a.getCheckOut() == null) // Filtra registros sem check-out
                .findFirst()                        // Pega primeiro (deve ser único)
                .orElseThrow(() -> new RuntimeException("No check-in found for today"));

        // ETAPA 3: Atualiza com horário de saída
        attendance.setCheckOut(LocalDateTime.now()); // Timestamp do servidor

        // ETAPA 4: Persiste alteração e retorna
        return ResponseEntity.ok(attendanceRepository.save(attendance));
    }
}