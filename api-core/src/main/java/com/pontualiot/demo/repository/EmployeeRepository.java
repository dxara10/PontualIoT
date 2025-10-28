package com.pontualiot.demo.repository;

// Importações para repositório JPA
import com.pontualiot.demo.entity.Employee; // Entidade Employee
import org.springframework.data.jpa.repository.JpaRepository; // Interface base do Spring Data JPA
import org.springframework.stereotype.Repository; // Anotação para marcar como repositório

import java.util.Optional; // Wrapper para valores que podem ser nulos

/**
 * Repositório para operações de banco de dados da entidade Employee
 * 
 * Extende JpaRepository que fornece operações CRUD básicas (save, findById, delete, etc.)
 * e adiciona métodos customizados para busca por RFID e email.
 */
@Repository // Marca como componente de repositório Spring
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    /**
     * Busca funcionário por tag RFID
     * 
     * Método usado pelos dispositivos IoT para validar RFID
     * e identificar o funcionário no momento do registro de ponto.
     * 
     * @param rfidTag Tag RFID do funcionário
     * @return Optional contendo o funcionário se encontrado
     */
    Optional<Employee> findByRfidTag(String rfidTag);
    
    /**
     * Busca funcionário por email
     * 
     * Método usado para validações de unicidade e
     * autenticação na interface administrativa.
     * 
     * @param email Email do funcionário
     * @return Optional contendo o funcionário se encontrado
     */
    Optional<Employee> findByEmail(String email);
}