package com.pontualiot.demo.service;

// Importações necessárias para o serviço de funcionários
import com.pontualiot.demo.entity.Employee; // Entidade de funcionário
import com.pontualiot.demo.repository.EmployeeRepository; // Repositório para operações de funcionário
import org.springframework.beans.factory.annotation.Autowired; // Injeção de dependência
import org.springframework.stereotype.Service; // Marca como serviço Spring

import java.util.List;     // Lista de funcionários
import java.util.Optional; // Opcional para valores que podem não existir

/**
 * Serviço responsável pela lógica de negócio dos funcionários
 * Gerencia CRUD e validações de funcionários
 */
@Service // Marca esta classe como um serviço Spring gerenciado pelo container
public class EmployeeService {

    // Injeção do repositório de funcionários
    @Autowired // Spring injeta automaticamente a implementação
    private EmployeeRepository employeeRepository;

    /**
     * Cria um novo funcionário no sistema
     * @param employee Dados do funcionário a ser criado
     * @return Funcionário criado com ID gerado
     */
    public Employee createEmployee(Employee employee) {
        // Valida se nome foi informado
        if (employee.getName() == null || employee.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do funcionário é obrigatório");
        }
        
        // Valida se email foi informado
        if (employee.getEmail() == null || employee.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email do funcionário é obrigatório");
        }
        
        // Valida se RFID tag foi informado
        if (employee.getRfidTag() == null || employee.getRfidTag().trim().isEmpty()) {
            throw new IllegalArgumentException("Tag RFID é obrigatório");
        }
        
        // Verifica se email já existe no sistema
        Optional<Employee> existingByEmail = employeeRepository.findByEmail(employee.getEmail());
        if (existingByEmail.isPresent()) {
            throw new IllegalArgumentException("Email já cadastrado: " + employee.getEmail());
        }
        
        // Verifica se RFID tag já existe no sistema
        Optional<Employee> existingByRfid = employeeRepository.findByRfidTag(employee.getRfidTag());
        if (existingByRfid.isPresent()) {
            throw new IllegalArgumentException("Tag RFID já cadastrado: " + employee.getRfidTag());
        }
        
        // Define funcionário como ativo por padrão se não especificado
        if (employee.isActive() == false && employee.isActive() != true) {
            employee.setActive(true); // Ativo por padrão
        }
        
        // Salva funcionário no banco de dados
        return employeeRepository.save(employee);
    }

    /**
     * Busca funcionário por ID
     * @param id ID do funcionário
     * @return Funcionário encontrado
     */
    public Employee findById(Long id) {
        // Valida se ID foi informado
        if (id == null) {
            throw new IllegalArgumentException("ID do funcionário é obrigatório");
        }
        
        // Busca funcionário no banco
        Optional<Employee> employee = employeeRepository.findById(id);
        
        // Verifica se funcionário foi encontrado
        if (employee.isEmpty()) {
            throw new IllegalArgumentException("Funcionário não encontrado com ID: " + id);
        }
        
        // Retorna funcionário encontrado
        return employee.get();
    }

    /**
     * Busca funcionário por tag RFID
     * @param rfidTag Tag RFID do funcionário
     * @return Funcionário encontrado
     */
    public Employee findByRfidTag(String rfidTag) {
        // Valida se RFID tag foi informado
        if (rfidTag == null || rfidTag.trim().isEmpty()) {
            throw new IllegalArgumentException("Tag RFID é obrigatório");
        }
        
        // Busca funcionário no banco pelo RFID
        Optional<Employee> employee = employeeRepository.findByRfidTag(rfidTag);
        
        // Verifica se funcionário foi encontrado
        if (employee.isEmpty()) {
            throw new IllegalArgumentException("Funcionário não encontrado com RFID: " + rfidTag);
        }
        
        // Retorna funcionário encontrado
        return employee.get();
    }

    /**
     * Busca funcionário por email
     * @param email Email do funcionário
     * @return Funcionário encontrado
     */
    public Employee findByEmail(String email) {
        // Valida se email foi informado
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email é obrigatório");
        }
        
        // Busca funcionário no banco pelo email
        Optional<Employee> employee = employeeRepository.findByEmail(email);
        
        // Verifica se funcionário foi encontrado
        if (employee.isEmpty()) {
            throw new IllegalArgumentException("Funcionário não encontrado com email: " + email);
        }
        
        // Retorna funcionário encontrado
        return employee.get();
    }

    /**
     * Lista todos os funcionários do sistema
     * @return Lista de todos os funcionários
     */
    public List<Employee> findAll() {
        // Busca todos os funcionários no banco
        return employeeRepository.findAll();
    }

    /**
     * Atualiza dados de um funcionário existente
     * @param id ID do funcionário a ser atualizado
     * @param updatedEmployee Novos dados do funcionário
     * @return Funcionário atualizado
     */
    public Employee updateEmployee(Long id, Employee updatedEmployee) {
        // Busca funcionário existente
        Employee existingEmployee = findById(id);
        
        // Atualiza nome se informado
        if (updatedEmployee.getName() != null && !updatedEmployee.getName().trim().isEmpty()) {
            existingEmployee.setName(updatedEmployee.getName());
        }
        
        // Atualiza email se informado e diferente do atual
        if (updatedEmployee.getEmail() != null && !updatedEmployee.getEmail().trim().isEmpty()) {
            // Verifica se novo email já existe em outro funcionário
            if (!existingEmployee.getEmail().equals(updatedEmployee.getEmail())) {
                Optional<Employee> existingByEmail = employeeRepository.findByEmail(updatedEmployee.getEmail());
                if (existingByEmail.isPresent()) {
                    throw new IllegalArgumentException("Email já cadastrado: " + updatedEmployee.getEmail());
                }
                existingEmployee.setEmail(updatedEmployee.getEmail()); // Atualiza email
            }
        }
        
        // Atualiza RFID tag se informado e diferente do atual
        if (updatedEmployee.getRfidTag() != null && !updatedEmployee.getRfidTag().trim().isEmpty()) {
            // Verifica se novo RFID já existe em outro funcionário
            if (!existingEmployee.getRfidTag().equals(updatedEmployee.getRfidTag())) {
                Optional<Employee> existingByRfid = employeeRepository.findByRfidTag(updatedEmployee.getRfidTag());
                if (existingByRfid.isPresent()) {
                    throw new IllegalArgumentException("Tag RFID já cadastrado: " + updatedEmployee.getRfidTag());
                }
                existingEmployee.setRfidTag(updatedEmployee.getRfidTag()); // Atualiza RFID
            }
        }
        
        // Atualiza status ativo
        existingEmployee.setActive(updatedEmployee.isActive());
        
        // Salva alterações no banco de dados
        return employeeRepository.save(existingEmployee);
    }

    /**
     * Desativa um funcionário (soft delete)
     * @param id ID do funcionário a ser desativado
     * @return Funcionário desativado
     */
    public Employee deactivateEmployee(Long id) {
        // Busca funcionário existente
        Employee employee = findById(id);
        
        // Marca como inativo
        employee.setActive(false);
        
        // Salva alteração no banco de dados
        return employeeRepository.save(employee);
    }

    /**
     * Ativa um funcionário
     * @param id ID do funcionário a ser ativado
     * @return Funcionário ativado
     */
    public Employee activateEmployee(Long id) {
        // Busca funcionário existente
        Employee employee = findById(id);
        
        // Marca como ativo
        employee.setActive(true);
        
        // Salva alteração no banco de dados
        return employeeRepository.save(employee);
    }
}