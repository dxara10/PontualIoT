package com.pontualiot.demo.entity;

// ========================================
// IMPORTAÇÕES JPA E LOMBOK
// ========================================
import jakarta.persistence.*; // Anotações JPA para mapeamento objeto-relacional
import lombok.AllArgsConstructor; // Gera construtor com todos os parâmetros
import lombok.Builder;           // Gera padrão Builder para criação fluente
import lombok.Data;              // Gera getters, setters, toString, equals, hashCode
import lombok.NoArgsConstructor; // Gera construtor vazio (obrigatório para JPA)

import java.time.LocalDateTime; // Classe para data e hora sem timezone

/**
 * ========================================
 * ENTIDADE EMPLOYEE - FUNCIONÁRIO
 * ========================================
 * 
 * RESPONSABILIDADES:
 * - Armazenar dados básicos dos funcionários
 * - Fornecer identificação única via RFID
 * - Controlar status ativo/inativo
 * - Manter auditoria de criação/atualização
 * 
 * RELACIONAMENTOS:
 * - Um Employee pode ter muitos Attendance (1:N)
 * - Chave estrangeira: employee_id na tabela attendances
 * 
 * VALIDAÇÕES IMPLEMENTADAS:
 * - Email único (constraint no banco)
 * - RFID Tag único (constraint no banco)
 * - Campos obrigatórios: name, email, rfidTag, active
 * 
 * FLUXO DE CRIAÇÃO:
 * 1. POST /api/employees recebe JSON
 * 2. EmployeeController valida dados
 * 3. JPA persiste no PostgreSQL
 * 4. Constraints são verificadas
 * 5. Se sucesso: retorna Employee com ID
 * 6. Se erro: retorna 400/409 com mensagem
 */
@Entity // JPA: marca como entidade persistente
@Table(name = "employees") // JPA: define nome da tabela no banco
@Data // Lombok: gera getters, setters, toString, equals, hashCode automaticamente
@Builder // Lombok: permite Employee.builder().name("João").build()
@NoArgsConstructor // Lombok: construtor vazio (JPA precisa para instanciar)
@AllArgsConstructor // Lombok: construtor com todos os campos
public class Employee {

    /**
     * CHAVE PRIMÁRIA - ID ÚNICO DO FUNCIONÁRIO
     * 
     * Gerada automaticamente pelo PostgreSQL usando SERIAL.
     * Usado como referência em outras tabelas.
     */
    @Id // JPA: marca como chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY) // PostgreSQL auto-incrementa
    private Long id;

    /**
     * NOME COMPLETO DO FUNCIONÁRIO
     * 
     * Campo obrigatório usado para identificação humana.
     * Não precisa ser único (pode haver homônimos).
     */
    @Column(nullable = false) // Banco: NOT NULL
    private String name;

    /**
     * EMAIL ÚNICO DO FUNCIONÁRIO
     * 
     * Usado para:
     * - Login futuro no sistema
     * - Notificações por email
     * - Identificação única alternativa
     * 
     * CONSTRAINT: UNIQUE no banco impede duplicatas
     */
    @Column(nullable = false, unique = true) // Banco: NOT NULL UNIQUE
    private String email;

    /**
     * TAG RFID ÚNICA PARA DISPOSITIVOS IOT
     * 
     * Identificador usado pelos dispositivos RFID para:
     * - Registrar entrada/saída
     * - Validar acesso
     * - Processar via MQTT
     * 
     * FORMATO: Geralmente alfanumérico (ex: "RFID001", "ABC123")
     * CONSTRAINT: UNIQUE no banco impede duplicatas
     */
    @Column(name = "rfid_tag", nullable = false, unique = true) // Banco: rfid_tag NOT NULL UNIQUE
    private String rfidTag;

    /**
     * STATUS ATIVO/INATIVO DO FUNCIONÁRIO
     * 
     * Controla se o funcionário pode:
     * - Registrar ponto
     * - Acessar sistema
     * - Aparecer em relatórios ativos
     * 
     * DEFAULT: true (ativo por padrão)
     */
    @Column(nullable = false) // Banco: NOT NULL
    @Builder.Default // Lombok: valor padrão no Builder pattern
    private boolean active = true;

    /**
     * TIMESTAMP DE CRIAÇÃO DO REGISTRO
     * 
     * Auditoria: quando o funcionário foi cadastrado.
     * Usado para relatórios e controle.
     */
    @Column(name = "created_at") // Banco: created_at
    @Builder.Default // Lombok: valor padrão no Builder
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * TIMESTAMP DA ÚLTIMA ATUALIZAÇÃO
     * 
     * Auditoria: quando o funcionário foi modificado pela última vez.
     * Atualizado automaticamente pelo @PreUpdate.
     */
    @Column(name = "updated_at") // Banco: updated_at
    @Builder.Default // Lombok: valor padrão no Builder
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * CALLBACK JPA - EXECUTADO ANTES DE ATUALIZAR
     * 
     * Sempre que um Employee é atualizado (UPDATE SQL),
     * este método é chamado automaticamente para atualizar
     * o timestamp de modificação.
     * 
     * FLUXO:
     * 1. employeeRepository.save(employee) é chamado
     * 2. JPA detecta que é UPDATE (não INSERT)
     * 3. @PreUpdate executa este método
     * 4. updatedAt é atualizado para agora
     * 5. UPDATE SQL é executado com novo timestamp
     */
    @PreUpdate // JPA: callback executado antes de UPDATE
    void preUpdate() {
        this.updatedAt = LocalDateTime.now(); // Atualiza para data/hora atual
    }
}