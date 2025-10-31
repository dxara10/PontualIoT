package com.pontualiot.demo.repository;

// ========================================
// IMPORTAÇÕES SPRING DATA JPA
// ========================================
import com.pontualiot.demo.entity.Attendance; // Entidade Attendance mapeada
import com.pontualiot.demo.entity.Employee;   // Entidade Employee para relacionamento
import org.springframework.data.jpa.repository.JpaRepository; // Interface base com CRUD
import org.springframework.stereotype.Repository; // Anotação de componente

import java.time.LocalDate; // Data sem horário (2024-10-30)
import java.util.List;      // Lista de resultados

/**
 * ========================================
 * REPOSITORY ATTENDANCE - CAMADA DE ACESSO A DADOS
 * ========================================
 * 
 * RESPONSABILIDADES:
 * - Fornecer operações CRUD para Attendance
 * - Implementar consultas por data, funcionário, período
 * - Suportar relatórios e dashboards
 * - Otimizar consultas com índices no banco
 * 
 * CONSULTAS SQL GERADAS AUTOMATICAMENTE:
 * - findByEmployeeAndDate -> WHERE employee_id = ? AND date = ?
 * - findByEmployeeId -> WHERE employee_id = ?
 * - findByDate -> WHERE date = ?
 * - findByEmployeeIdAndDateBetween -> WHERE employee_id = ? AND date BETWEEN ? AND ?
 * 
 * FLUXO TÍPICO DE REGISTRO DE PONTO:
 * 1. MQTT recebe: {"rfidTag": "RFID001", "action": "check-in"}
 * 2. MqttAttendanceService.processAttendance()
 * 3. employeeRepository.findByRfidTag("RFID001")
 * 4. attendanceRepository.findByEmployeeAndDate(employee, today)
 * 5. Se vazio: cria novo Attendance com check-in
 * 6. Se existe: atualiza check-out
 * 7. attendanceRepository.save(attendance)
 * 
 * FLUXO DE CONSULTA WEB:
 * 1. GET /api/attendances/employee/1
 * 2. AttendanceController.getAttendancesByEmployee(1)
 * 3. attendanceRepository.findByEmployeeId(1)
 * 4. Retorna List<Attendance> com Employee carregado (EAGER)
 * 
 * OTIMIZAÇÕES IMPLEMENTADAS:
 * - Índice em (employee_id, date) para consultas rápidas
 * - FETCH EAGER em Employee evita N+1 queries
 * - LocalDate separado otimiza consultas por período
 */
@Repository // Spring: marca como componente de acesso a dados
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    /**
     * BUSCA REGISTROS DE UM FUNCIONÁRIO EM DATA ESPECÍFICA
     * 
     * Método crítico para lógica de check-in/check-out:
     * - Verifica se funcionário já tem registro hoje
     * - Se vazio: cria novo registro (check-in)
     * - Se existe: atualiza existente (check-out)
     * 
     * SQL GERADO:
     * SELECT a.*, e.* FROM attendances a 
     * JOIN employees e ON a.employee_id = e.id 
     * WHERE a.employee_id = ? AND a.date = ?
     * 
     * CASOS DE USO:
     * - Validação antes de registrar ponto
     * - Consulta de registros diários
     * - Verificação de jornada completa
     * 
     * @param employee Objeto Employee completo
     * @param date Data específica (ex: 2024-10-30)
     * @return List<Attendance> - geralmente 0 ou 1 registro por dia
     */
    List<Attendance> findByEmployeeAndDate(Employee employee, LocalDate date);
    
    /**
     * BUSCA REGISTROS POR PERÍODO
     * 
     * Método para relatórios e dashboards:
     * - Frequência mensal
     * - Horas trabalhadas por período
     * - Análise de padrões de ponto
     * 
     * SQL GERADO:
     * SELECT a.*, e.* FROM attendances a 
     * JOIN employees e ON a.employee_id = e.id 
     * WHERE a.employee_id = ? AND a.date BETWEEN ? AND ?
     * ORDER BY a.date DESC
     * 
     * PERFORMANCE:
     * - Índice em (employee_id, date) otimiza esta consulta
     * - BETWEEN é eficiente para ranges de data
     * 
     * @param employeeId ID do funcionário
     * @param startDate Data inicial (inclusiva)
     * @param endDate Data final (inclusiva)
     * @return List<Attendance> ordenada por data
     */
    List<Attendance> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);
    
    /**
     * BUSCA TODOS OS REGISTROS DE UM FUNCIONÁRIO
     * 
     * Método para:
     * - Histórico completo do funcionário
     * - Relatórios gerais
     * - Análise de comportamento
     * 
     * SQL GERADO:
     * SELECT a.*, e.* FROM attendances a 
     * JOIN employees e ON a.employee_id = e.id 
     * WHERE a.employee_id = ?
     * ORDER BY a.date DESC, a.check_in DESC
     * 
     * ATENÇÃO:
     * - Pode retornar muitos registros
     * - Considerar paginação para funcionários antigos
     * 
     * @param employeeId ID do funcionário
     * @return List<Attendance> todos os registros do funcionário
     */
    List<Attendance> findByEmployeeId(Long employeeId);
    
    /**
     * BUSCA REGISTROS POR DATA (TODOS OS FUNCIONÁRIOS)
     * 
     * Método para:
     * - Relatório diário geral
     * - Dashboard de presença
     * - Monitoramento em tempo real
     * 
     * SQL GERADO:
     * SELECT a.*, e.* FROM attendances a 
     * JOIN employees e ON a.employee_id = e.id 
     * WHERE a.date = ?
     * ORDER BY e.name
     * 
     * CASOS DE USO:
     * - "Quem está presente hoje?"
     * - "Quantos registros tivemos hoje?"
     * - Dashboards administrativos
     * 
     * @param date Data específica (ex: 2024-10-30)
     * @return List<Attendance> todos os registros da data
     */
    List<Attendance> findByDate(LocalDate date);
}