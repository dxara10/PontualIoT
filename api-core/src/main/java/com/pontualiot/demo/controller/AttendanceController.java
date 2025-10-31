package com.pontualiot.demo.controller;

// ========================================
// IMPORTAÇÕES SPRING MVC E MÉTRICAS
// ========================================
import com.pontualiot.demo.config.MetricsConfig; // Configuração de métricas Prometheus
import com.pontualiot.demo.entity.Attendance;    // Entidade de registro de ponto
import com.pontualiot.demo.repository.AttendanceRepository; // Repositório de dados
import io.micrometer.core.instrument.Counter;     // Contador de métricas
import io.swagger.v3.oas.annotations.Operation;   // Documentação OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;    // Agrupamento de endpoints
import org.springframework.beans.factory.annotation.Autowired; // Injeção de dependência
import org.springframework.format.annotation.DateTimeFormat;   // Formatação de data
import org.springframework.http.ResponseEntity;    // Wrapper para respostas HTTP
import org.springframework.web.bind.annotation.*;  // Anotações REST

import java.time.LocalDate; // Data sem horário
import java.util.List;      // Lista de resultados

/**
 * ========================================
 * CONTROLLER ATTENDANCE - CAMADA DE APRESENTAÇÃO
 * ========================================
 * 
 * RESPONSABILIDADES:
 * - Expor endpoints REST para registros de ponto
 * - Validar parâmetros de entrada
 * - Converter dados entre JSON e objetos Java
 * - Retornar códigos HTTP apropriados
 * - Registrar métricas de uso
 * 
 * ENDPOINTS EXPOSTOS:
 * - GET /api/attendances -> Lista todos os registros
 * - GET /api/attendances/{id} -> Busca por ID específico
 * - GET /api/attendances/employee/{employeeId} -> Registros de um funcionário
 * - GET /api/attendances/date/{date} -> Registros de uma data
 * 
 * FLUXO DE REQUISIÇÃO TÍPICO:
 * 1. Cliente faz GET /api/attendances/employee/1
 * 2. Spring MVC roteia para getAttendancesByEmployee(1)
 * 3. Método valida parâmetros
 * 4. Chama attendanceRepository.findByEmployeeId(1)
 * 5. JPA executa SQL no PostgreSQL
 * 6. Resultados são mapeados para List<Attendance>
 * 7. Jackson serializa para JSON
 * 8. ResponseEntity retorna 200 OK com dados
 * 
 * TRATAMENTO DE ERROS:
 * - 400 Bad Request: parâmetros inválidos
 * - 404 Not Found: registro não encontrado
 * - 500 Internal Server Error: erro no banco/sistema
 * 
 * MÉTRICAS COLETADAS:
 * - attendanceRecordsCounter: número de consultas realizadas
 * - Expostas em /api/actuator/prometheus
 */
@RestController // Spring: combina @Controller + @ResponseBody
@RequestMapping("/attendances") // Base path: /api/attendances
@Tag(name = "Attendances", description = "Attendance record operations") // OpenAPI
public class AttendanceController {

    /**
     * INJEÇÃO DE DEPENDÊNCIA - REPOSITÓRIO
     * 
     * Spring injeta automaticamente a implementação do repositório.
     * Permite acesso aos dados sem acoplamento direto.
     */
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    /**
     * INJEÇÃO DE DEPENDÊNCIA - CONTADOR DE MÉTRICAS
     * 
     * Prometheus counter para monitorar uso dos endpoints.
     * Incrementado a cada consulta realizada.
     */
    @Autowired
    private Counter attendanceRecordsCounter;

    /**
     * ENDPOINT: LISTAR TODOS OS REGISTROS
     * 
     * GET /api/attendances
     * 
     * CASOS DE USO:
     * - Dashboard administrativo geral
     * - Relatórios completos
     * - Debugging e monitoramento
     * 
     * ATENÇÃO: Pode retornar muitos dados!
     * Em produção, considerar paginação.
     * 
     * RESPOSTA:
     * 200 OK + JSON array com todos os registros
     * 
     * @return List<Attendance> todos os registros do sistema
     */
    @GetMapping // Mapeia GET /attendances
    @Operation(summary = "List all attendance records") // Documentação OpenAPI
    public List<Attendance> getAllAttendances() {
        // Incrementa contador de métricas
        attendanceRecordsCounter.increment();
        
        // Busca todos os registros (pode ser lento!)
        return attendanceRepository.findAll();
    }

    /**
     * ENDPOINT: BUSCAR REGISTRO POR ID
     * 
     * GET /api/attendances/{id}
     * 
     * CASOS DE USO:
     * - Consulta de registro específico
     * - Detalhes de um ponto registrado
     * - Validação de dados
     * 
     * FLUXO:
     * 1. Spring extrai {id} da URL
     * 2. Converte String para Long automaticamente
     * 3. Chama attendanceRepository.findById(id)
     * 4. Se encontrado: retorna 200 OK + dados
     * 5. Se não encontrado: retorna 404 Not Found
     * 
     * @param id ID do registro de ponto
     * @return ResponseEntity<Attendance> 200 OK ou 404 Not Found
     */
    @GetMapping("/{id}") // Mapeia GET /attendances/123
    @Operation(summary = "Get attendance by ID") // Documentação OpenAPI
    public ResponseEntity<Attendance> getAttendanceById(@PathVariable Long id) {
        return attendanceRepository.findById(id)
                .map(ResponseEntity::ok)           // Se presente: 200 OK
                .orElse(ResponseEntity.notFound().build()); // Se ausente: 404
    }

    /**
     * ENDPOINT: BUSCAR REGISTROS POR FUNCIONÁRIO
     * 
     * GET /api/attendances/employee/{employeeId}
     * 
     * ENDPOINT MAIS USADO DO SISTEMA!
     * 
     * CASOS DE USO:
     * - Histórico de ponto de um funcionário
     * - Relatórios individuais
     * - Validação de frequência
     * - Dashboard pessoal
     * 
     * VALIDAÇÕES IMPLEMENTADAS:
     * - employeeId não pode ser null
     * - Tratamento de exceções do banco
     * - Logs detalhados para debugging
     * 
     * FLUXO COMPLETO:
     * 1. Cliente: GET /api/attendances/employee/1
     * 2. Spring: extrai employeeId = 1
     * 3. Validação: employeeId != null
     * 4. Repository: findByEmployeeId(1)
     * 5. SQL: SELECT * FROM attendances WHERE employee_id = 1
     * 6. JPA: mapeia resultados para List<Attendance>
     * 7. Jackson: serializa para JSON
     * 8. HTTP: 200 OK + JSON response
     * 
     * LOGS GERADOS:
     * - [ATTENDANCE] GET /attendances/employee/1
     * - [ATTENDANCE] ✅ Encontrados 5 registros
     * 
     * @param employeeId ID do funcionário
     * @return ResponseEntity<List<Attendance>> 200 OK com registros ou 400/500 em erro
     */
    @GetMapping("/employee/{employeeId}") // Mapeia GET /attendances/employee/123
    @Operation(summary = "Get attendances by employee ID") // Documentação OpenAPI
    public ResponseEntity<List<Attendance>> getAttendancesByEmployee(@PathVariable Long employeeId) {
        // Log da requisição recebida
        System.out.println("[ATTENDANCE] GET /attendances/employee/" + employeeId);
        
        // Validação básica de parâmetro
        if (employeeId == null) {
            System.err.println("[ATTENDANCE] ❌ Employee ID é null");
            return ResponseEntity.badRequest().build(); // 400 Bad Request
        }
        
        try {
            // Busca registros no banco de dados
            List<Attendance> attendances = attendanceRepository.findByEmployeeId(employeeId);
            
            // Log do resultado
            System.out.println("[ATTENDANCE] ✅ Encontrados " + attendances.size() + " registros");
            
            // Retorna 200 OK com os dados
            return ResponseEntity.ok(attendances);
            
        } catch (Exception e) {
            // Log do erro
            System.err.println("[ATTENDANCE] ❌ Erro: " + e.getMessage());
            
            // Retorna 500 Internal Server Error
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * ENDPOINT: BUSCAR REGISTROS POR DATA
     * 
     * GET /api/attendances/date/{date}
     * 
     * CASOS DE USO:
     * - Relatório diário de presença
     * - "Quem trabalhou hoje?"
     * - Dashboard de monitoramento
     * - Análise de padrões diários
     * 
     * FORMATO DA DATA:
     * - ISO 8601: 2024-10-30
     * - Spring converte automaticamente String -> LocalDate
     * - @DateTimeFormat garante formato correto
     * 
     * EXEMPLO DE USO:
     * GET /api/attendances/date/2024-10-30
     * 
     * RESPOSTA:
     * [
     *   {
     *     "id": 1,
     *     "employee": {"id": 1, "name": "João"},
     *     "checkIn": "2024-10-30T08:00:00",
     *     "checkOut": "2024-10-30T17:00:00",
     *     "date": "2024-10-30"
     *   }
     * ]
     * 
     * @param date Data no formato ISO (2024-10-30)
     * @return List<Attendance> registros da data especificada
     */
    @GetMapping("/date/{date}") // Mapeia GET /attendances/date/2024-10-30
    @Operation(summary = "Get attendances by date") // Documentação OpenAPI
    public List<Attendance> getAttendancesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        // Busca registros da data específica
        return attendanceRepository.findByDate(date);
    }
}