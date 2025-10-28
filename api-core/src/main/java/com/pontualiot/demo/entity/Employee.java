package com.pontualiot.demo.entity;

// Importações JPA para mapeamento objeto-relacional
import jakarta.persistence.*; // Anotações JPA (Entity, Table, Column, etc.)
import lombok.AllArgsConstructor; // Gera construtor com todos os parâmetros
import lombok.Builder;           // Gera padrão Builder para criação de objetos
import lombok.Data;              // Gera getters, setters, toString, equals, hashCode
import lombok.NoArgsConstructor; // Gera construtor sem parâmetros

import java.time.LocalDateTime; // Classe para data e hora local

/**
 * Entidade Employee - Representa um funcionário no sistema
 * 
 * Esta entidade armazena informações básicas dos funcionários
 * e é usada para controle de acesso via RFID nos dispositivos IoT.
 */
@Entity // Marca como entidade JPA
@Table(name = "employees") // Define nome da tabela no banco
@Data // Lombok: gera getters, setters, toString, equals, hashCode
@Builder // Lombok: gera padrão Builder para criação fluente de objetos
@NoArgsConstructor // Lombok: gera construtor vazio (necessário para JPA)
@AllArgsConstructor // Lombok: gera construtor com todos os campos
public class Employee {

    // Chave primária auto-incrementada
    @Id // Marca como chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incremento no banco
    private Long id;

    // Nome completo do funcionário
    @Column(nullable = false) // Campo obrigatório no banco
    private String name;

    // Email único do funcionário
    @Column(nullable = false, unique = true) // Campo obrigatório e único
    private String email;

    // Tag RFID única para identificação nos dispositivos
    @Column(name = "rfid_tag", nullable = false, unique = true) // Campo obrigatório e único
    private String rfidTag;

    // Status ativo/inativo do funcionário
    @Column(nullable = false) // Campo obrigatório
    @Builder.Default // Lombok: valor padrão no Builder
    private boolean active = true; // Ativo por padrão

    // Timestamp de criação do registro
    @Column(name = "created_at") // Nome da coluna no banco
    @Builder.Default // Lombok: valor padrão no Builder
    private LocalDateTime createdAt = LocalDateTime.now(); // Data/hora atual

    // Timestamp da última atualização
    @Column(name = "updated_at") // Nome da coluna no banco
    @Builder.Default // Lombok: valor padrão no Builder
    private LocalDateTime updatedAt = LocalDateTime.now(); // Data/hora atual

    /**
     * Callback JPA executado antes de atualizar o registro
     * Atualiza automaticamente o campo updatedAt
     */
    @PreUpdate // JPA: executado antes de UPDATE
    void preUpdate() {
        this.updatedAt = LocalDateTime.now(); // Atualiza timestamp
    }
}