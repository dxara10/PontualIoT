package com.pontualiot.demo.repository;

// Importações para repositório JPA
import com.pontualiot.demo.entity.Attendance; // Entidade Attendance
import com.pontualiot.demo.entity.Employee;   // Entidade Employee
import org.springframework.data.jpa.repository.JpaRepository; // Interface base do Spring Data JPA
import org.springframework.stereotype.Repository; // Anotação para marcar como repositório

import java.time.LocalDate; // Classe para data (sem horário)
import java.util.List;      // Lista de registros

/**
 * Repositório para operações de banco de dados da entidade Attendance
 * 
 * Extende JpaRepository que fornece operações CRUD básicas (save, findById, delete, etc.)
 * e adiciona métodos customizados para consultas de registros de ponto por data e período.
 */
@Repository // Marca como componente de repositório Spring
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    /**
     * Busca registros de ponto de um funcionário em uma data específica
     * 
     * Usado para verificar se o funcionário já registrou ponto hoje
     * e para consultas de registros diários.
     * 
     * @param employee Funcionário
     * @param date Data específica
     * @return Lista de registros na data
     */
    List<Attendance> findByEmployeeAndDate(Employee employee, LocalDate date);
    
    /**
     * Busca registros de ponto de um funcionário em um período
     * 
     * Usado para relatórios de frequência e consultas por período
     * (semanal, mensal, etc.).
     * 
     * @param employeeId ID do funcionário
     * @param startDate Data inicial do período
     * @param endDate Data final do período
     * @return Lista de registros no período
     */
    List<Attendance> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Busca registros por ID do funcionário
     */
    List<Attendance> findByEmployeeId(Long employeeId);
    
    /**
     * Busca registros por data
     */
    List<Attendance> findByDate(LocalDate date);
}