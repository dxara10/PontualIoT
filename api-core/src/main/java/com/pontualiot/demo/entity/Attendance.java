package com.pontualiot.demo.entity;

// Importações JPA para mapeamento objeto-relacional
import jakarta.persistence.*; // Anotações JPA (Entity, Table, Column, etc.)
import lombok.AllArgsConstructor; // Gera construtor com todos os parâmetros
import lombok.Builder;           // Gera padrão Builder para criação de objetos
import lombok.Data;              // Gera getters, setters, toString, equals, hashCode
import lombok.NoArgsConstructor; // Gera construtor sem parâmetros

import java.time.LocalDate;     // Classe para data (sem horário)
import java.time.LocalDateTime; // Classe para data e hora local

/**
 * Entidade Attendance - Representa um registro de ponto
 * 
 * Esta entidade armazena os registros de entrada e saída dos funcionários,
 * capturados via dispositivos IoT com RFID ou outras interfaces.
 */
@Entity // Marca como entidade JPA
@Table(name = "attendances") // Define nome da tabela no banco
@Data // Lombok: gera getters, setters, toString, equals, hashCode
@Builder // Lombok: gera padrão Builder para criação fluente de objetos
@NoArgsConstructor // Lombok: gera construtor vazio (necessário para JPA)
@AllArgsConstructor // Lombok: gera construtor com todos os campos
public class Attendance {

    // Chave primária auto-incrementada
    @Id // Marca como chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incremento no banco
    private Long id;

    // Relacionamento com funcionário (muitos registros para um funcionário)
    @ManyToOne(fetch = FetchType.EAGER) // Carregamento eager para evitar problemas de serialização
    @JoinColumn(name = "employee_id", nullable = false) // Chave estrangeira obrigatória
    private Employee employee;

    // Horário de entrada (check-in)
    @Column(name = "check_in", nullable = false) // Campo obrigatório
    private LocalDateTime checkIn;

    // Horário de saída (check-out) - opcional
    @Column(name = "check_out") // Campo opcional (pode ser null)
    private LocalDateTime checkOut;

    // Data do registro (para facilitar consultas por período)
    @Column(nullable = false) // Campo obrigatório
    private LocalDate date;

    // Timestamp de criação do registro
    @Column(name = "created_at") // Nome da coluna no banco
    @Builder.Default // Lombok: valor padrão no Builder
    private LocalDateTime createdAt = LocalDateTime.now(); // Data/hora atual
}