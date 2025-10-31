package com.pontualiot.demo.entity;

// ========================================
// IMPORTAÇÕES JPA E LOMBOK
// ========================================
import jakarta.persistence.*; // Anotações JPA para mapeamento objeto-relacional
import lombok.AllArgsConstructor; // Gera construtor com todos os parâmetros
import lombok.Builder;           // Gera padrão Builder para criação fluente
import lombok.Data;              // Gera getters, setters, toString, equals, hashCode
import lombok.NoArgsConstructor; // Gera construtor vazio (obrigatório para JPA)

import java.time.LocalDate;     // Data sem horário (2024-10-30)
import java.time.LocalDateTime; // Data com horário (2024-10-30T14:30:00)

/**
 * ========================================
 * ENTIDADE ATTENDANCE - REGISTRO DE PONTO
 * ========================================
 * 
 * RESPONSABILIDADES:
 * - Armazenar registros de entrada/saída dos funcionários
 * - Relacionar com Employee via chave estrangeira
 * - Permitir consultas por data, funcionário, período
 * - Suportar check-in obrigatório e check-out opcional
 * 
 * RELACIONAMENTOS:
 * - ManyToOne com Employee (muitos registros para um funcionário)
 * - Chave estrangeira: employee_id (NOT NULL)
 * 
 * FLUXO DE CRIAÇÃO VIA RFID:
 * 1. Dispositivo IoT lê tag RFID
 * 2. Envia mensagem MQTT: {"rfidTag": "RFID001", "action": "check-in"}
 * 3. MqttListener recebe mensagem
 * 4. MqttAttendanceService processa:
 *    - Busca Employee por rfidTag
 *    - Verifica se já tem registro hoje
 *    - Se não: cria check-in
 *    - Se sim: atualiza check-out
 * 5. AttendanceRepository.save() persiste no banco
 * 
 * FLUXO DE CONSULTA:
 * 1. GET /api/attendances/employee/{id}
 * 2. AttendanceController.getAttendancesByEmployee()
 * 3. AttendanceRepository.findByEmployeeId()
 * 4. JPA executa: SELECT * FROM attendances WHERE employee_id = ?
 * 5. Retorna List<Attendance> com Employee carregado (EAGER)
 * 
 * VALIDAÇÕES IMPLEMENTADAS:
 * - employee_id obrigatório (constraint FK)
 * - check_in obrigatório
 * - date obrigatório
 * - check_out opcional (permite jornada em aberto)
 */
@Entity // JPA: marca como entidade persistente
@Table(name = "attendances") // JPA: define nome da tabela no banco
@Data // Lombok: gera getters, setters, toString, equals, hashCode automaticamente
@Builder // Lombok: permite Attendance.builder().checkIn(now).build()
@NoArgsConstructor // Lombok: construtor vazio (JPA precisa para instanciar)
@AllArgsConstructor // Lombok: construtor com todos os campos
public class Attendance {

    /**
     * CHAVE PRIMÁRIA - ID ÚNICO DO REGISTRO
     * 
     * Gerada automaticamente pelo PostgreSQL usando SERIAL.
     * Usada para referênciar registros específicos.
     */
    @Id // JPA: marca como chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PostgreSQL auto-incrementa
    private Long id;

    /**
     * RELACIONAMENTO COM FUNCIONÁRIO
     * 
     * Relacionamento Many-to-One:
     * - Muitos registros de ponto (Attendance)
     * - Para um funcionário (Employee)
     * 
     * FETCH EAGER: Employee é carregado junto com Attendance
     * Evita LazyInitializationException na serialização JSON.
     * 
     * CHAVE ESTRANGEIRA: employee_id na tabela attendances
     * CONSTRAINT: NOT NULL (todo registro deve ter funcionário)
     */
    @ManyToOne(fetch = FetchType.EAGER) // JPA: carregamento imediato
    @JoinColumn(name = "employee_id", nullable = false) // FK obrigatória
    private Employee employee;

    /**
     * HORÁRIO DE ENTRADA (CHECK-IN)
     * 
     * Timestamp completo da entrada do funcionário.
     * Sempre obrigatório - todo registro começa com entrada.
     * 
     * FORMATO: 2024-10-30T08:00:00 (ISO LocalDateTime)
     * ORIGEM: Dispositivo RFID ou interface manual
     */
    @Column(name = "check_in", nullable = false) // Banco: check_in NOT NULL
    private LocalDateTime checkIn;

    /**
     * HORÁRIO DE SAÍDA (CHECK-OUT)
     * 
     * Timestamp da saída do funcionário.
     * OPCIONAL - permite jornadas em aberto.
     * 
     * CASOS DE USO:
     * - null: funcionário ainda não saiu
     * - preenchido: jornada completa
     * 
     * FORMATO: 2024-10-30T17:00:00 (ISO LocalDateTime)
     */
    @Column(name = "check_out") // Banco: check_out (pode ser NULL)
    private LocalDateTime checkOut;

    /**
     * DATA DO REGISTRO (SEM HORÁRIO)
     * 
     * Data isolada para facilitar consultas por período.
     * Derivada do checkIn, mas armazenada separadamente
     * para otimizar queries.
     * 
     * FORMATO: 2024-10-30 (ISO LocalDate)
     * USO: WHERE date BETWEEN '2024-10-01' AND '2024-10-31'
     */
    @Column(nullable = false) // Banco: date NOT NULL
    private LocalDate date;

    /**
     * TIMESTAMP DE CRIAÇÃO DO REGISTRO
     * 
     * Auditoria: quando o registro foi criado no sistema.
     * Diferente do checkIn (que é quando o funcionário entrou).
     * 
     * EXEMPLO:
     * - checkIn: 08:00 (quando o funcionário chegou)
     * - createdAt: 08:01 (quando o sistema processou)
     */
    @Column(name = "created_at") // Banco: created_at
    @Builder.Default // Lombok: valor padrão no Builder
    private LocalDateTime createdAt = LocalDateTime.now();
}