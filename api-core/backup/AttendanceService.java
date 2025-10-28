package com.pontualiot.demo.service;

// Importações necessárias para o serviço de ponto
import com.pontualiot.demo.entity.Attendance; // Entidade de registro de ponto
import com.pontualiot.demo.entity.Employee;   // Entidade de funcionário
import com.pontualiot.demo.repository.AttendanceRepository; // Repositório para operações de ponto
import com.pontualiot.demo.repository.EmployeeRepository;   // Repositório para operações de funcionário
import org.springframework.beans.factory.annotation.Autowired; // Injeção de dependência
import org.springframework.stereotype.Service; // Marca como serviço Spring

import java.time.LocalDate;     // Data local (sem horário)
import java.time.LocalDateTime; // Data e hora local
import java.util.List;          // Lista de registros
import java.util.Optional;      // Opcional para valores que podem não existir

/**
 * Serviço responsável pela lógica de negócio dos registros de ponto
 * Gerencia check-in, check-out e consultas de frequência
 */
@Service // Marca esta classe como um serviço Spring gerenciado pelo container
public class AttendanceService {

    // Injeção do repositório de registros de ponto
    @Autowired // Spring injeta automaticamente a implementação
    private AttendanceRepository attendanceRepository;

    // Injeção do repositório de funcionários
    @Autowired // Spring injeta automaticamente a implementação
    private EmployeeRepository employeeRepository;

    /**
     * Registra entrada (check-in) de um funcionário
     * @param rfidTag Tag RFID do funcionário
     * @return Registro de ponto criado
     */
    public Attendance checkIn(String rfidTag) {
        // Busca funcionário pelo tag RFID
        Optional<Employee> employeeOpt = employeeRepository.findByRfidTag(rfidTag);
        
        // Verifica se funcionário existe
        if (employeeOpt.isEmpty()) {
            throw new IllegalArgumentException("Funcionário não encontrado com RFID: " + rfidTag);
        }
        
        Employee employee = employeeOpt.get(); // Obtém o funcionário encontrado
        
        // Verifica se funcionário está ativo
        if (!employee.isActive()) {
            throw new IllegalArgumentException("Funcionário inativo: " + employee.getName());
        }
        
        LocalDate today = LocalDate.now(); // Data atual
        
        // Verifica se já existe registro de entrada hoje
        List<Attendance> todayAttendances = attendanceRepository.findByEmployeeAndDate(employee, today);
        
        // Se já tem registro hoje, verifica se já fez check-in
        for (Attendance attendance : todayAttendances) {
            if (attendance.getCheckIn() != null && attendance.getCheckOut() == null) {
                throw new IllegalArgumentException("Funcionário já registrou entrada hoje");
            }
        }
        
        // Cria novo registro de ponto
        Attendance attendance = Attendance.builder()
                .employee(employee)           // Associa ao funcionário
                .checkIn(LocalDateTime.now()) // Registra horário de entrada atual
                .date(today)                  // Define data do registro
                .build();
        
        // Salva no banco de dados e retorna
        return attendanceRepository.save(attendance);
    }

    /**
     * Registra saída (check-out) de um funcionário
     * @param rfidTag Tag RFID do funcionário
     * @return Registro de ponto atualizado
     */
    public Attendance checkOut(String rfidTag) {
        // Busca funcionário pelo tag RFID
        Optional<Employee> employeeOpt = employeeRepository.findByRfidTag(rfidTag);
        
        // Verifica se funcionário existe
        if (employeeOpt.isEmpty()) {
            throw new IllegalArgumentException("Funcionário não encontrado com RFID: " + rfidTag);
        }
        
        Employee employee = employeeOpt.get(); // Obtém o funcionário encontrado
        LocalDate today = LocalDate.now();    // Data atual
        
        // Busca registros de hoje do funcionário
        List<Attendance> todayAttendances = attendanceRepository.findByEmployeeAndDate(employee, today);
        
        // Procura registro em aberto (com check-in mas sem check-out)
        Attendance openAttendance = null;
        for (Attendance attendance : todayAttendances) {
            // Se tem entrada mas não tem saída, é o registro em aberto
            if (attendance.getCheckIn() != null && attendance.getCheckOut() == null) {
                openAttendance = attendance;
                break; // Para no primeiro encontrado
            }
        }
        
        // Verifica se existe registro em aberto
        if (openAttendance == null) {
            throw new IllegalArgumentException("Nenhum registro de entrada encontrado para hoje");
        }
        
        // Registra horário de saída
        openAttendance.setCheckOut(LocalDateTime.now());
        
        // Salva alteração no banco de dados e retorna
        return attendanceRepository.save(openAttendance);
    }

    /**
     * Busca registros de ponto de um funcionário por período
     * @param employeeId ID do funcionário
     * @param startDate Data inicial
     * @param endDate Data final
     * @return Lista de registros no período
     */
    public List<Attendance> getAttendanceByPeriod(Long employeeId, LocalDate startDate, LocalDate endDate) {
        // Valida se as datas fazem sentido
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Data inicial não pode ser posterior à data final");
        }
        
        // Busca registros no período usando query do repositório
        return attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, startDate, endDate);
    }

    /**
     * Busca registros de ponto de um funcionário em uma data específica
     * @param employeeId ID do funcionário
     * @param date Data específica
     * @return Lista de registros na data
     */
    public List<Attendance> getAttendanceByDate(Long employeeId, LocalDate date) {
        // Busca funcionário pelo ID
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        
        // Verifica se funcionário existe
        if (employeeOpt.isEmpty()) {
            throw new IllegalArgumentException("Funcionário não encontrado com ID: " + employeeId);
        }
        
        // Busca registros da data específica
        return attendanceRepository.findByEmployeeAndDate(employeeOpt.get(), date);
    }
}