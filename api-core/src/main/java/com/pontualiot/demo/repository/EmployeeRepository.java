package com.pontualiot.demo.repository;

// ========================================
// IMPORTAÇÕES SPRING DATA JPA
// ========================================
import com.pontualiot.demo.entity.Employee; // Entidade Employee mapeada
import org.springframework.data.jpa.repository.JpaRepository; // Interface base com CRUD
import org.springframework.stereotype.Repository; // Anotação de componente

import java.util.Optional; // Container para valores que podem ser null

/**
 * ========================================
 * REPOSITORY EMPLOYEE - CAMADA DE ACESSO A DADOS
 * ========================================
 * 
 * RESPONSABILIDADES:
 * - Fornecer operações CRUD para Employee
 * - Implementar consultas customizadas
 * - Abstrair acesso ao PostgreSQL
 * - Gerenciar transações automaticamente
 * 
 * HERANÇA JpaRepository<Employee, Long>:
 * - Employee: tipo da entidade
 * - Long: tipo da chave primária (id)
 * 
 * MÉTODOS HERDADOS AUTOMATICAMENTE:
 * - save(Employee) -> INSERT/UPDATE
 * - findById(Long) -> SELECT por ID
 * - findAll() -> SELECT * FROM employees
 * - deleteById(Long) -> DELETE por ID
 * - count() -> COUNT(*)
 * - existsById(Long) -> EXISTS
 * 
 * CONSULTAS SQL GERADAS AUTOMATICAMENTE:
 * - findByRfidTag(String) -> SELECT * FROM employees WHERE rfid_tag = ?
 * - findByEmail(String) -> SELECT * FROM employees WHERE email = ?
 * 
 * FLUXO DE USO TÍPICO:
 * 1. @Autowired EmployeeRepository employeeRepository
 * 2. employeeRepository.findByRfidTag("RFID001")
 * 3. Spring Data JPA gera SQL automaticamente
 * 4. Hibernate executa no PostgreSQL
 * 5. Resultado é mapeado para Employee
 * 6. Retorna Optional<Employee>
 * 
 * VANTAGENS DO OPTIONAL:
 * - Evita NullPointerException
 * - Força tratamento de "não encontrado"
 * - API mais expressiva: .isPresent(), .orElse(), .map()
 */
@Repository // Spring: marca como componente de acesso a dados
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    /**
     * BUSCA FUNCIONÁRIO POR TAG RFID
     * 
     * Método crítico para o fluxo IoT:
     * 1. Dispositivo RFID lê tag
     * 2. Envia via MQTT
     * 3. MqttAttendanceService chama este método
     * 4. Se encontrado: processa registro de ponto
     * 5. Se não: rejeita com erro
     * 
     * SQL GERADO:
     * SELECT * FROM employees WHERE rfid_tag = ? AND active = true
     * 
     * CASOS DE USO:
     * - Validação de acesso em dispositivos
     * - Registro automático de ponto
     * - Identificação rápida por hardware
     * 
     * @param rfidTag Tag RFID lida pelo dispositivo (ex: "RFID001")
     * @return Optional<Employee> - presente se encontrado, vazio se não
     */
    Optional<Employee> findByRfidTag(String rfidTag);
    
    /**
     * BUSCA FUNCIONÁRIO POR EMAIL
     * 
     * Método usado para:
     * - Validação de unicidade no cadastro
     * - Login futuro no sistema web
     * - Recuperação de senha
     * - Consultas administrativas
     * 
     * SQL GERADO:
     * SELECT * FROM employees WHERE email = ?
     * 
     * CONSTRAINT NO BANCO:
     * - UNIQUE em email impede duplicatas
     * - Se tentar inserir email duplicado: SQLException
     * 
     * FLUXO DE VALIDAÇÃO:
     * 1. POST /api/employees com email
     * 2. EmployeeController chama findByEmail()
     * 3. Se Optional.isPresent(): retorna 409 Conflict
     * 4. Se Optional.isEmpty(): prossegue com save()
     * 
     * @param email Email do funcionário (ex: "joao@empresa.com")
     * @return Optional<Employee> - presente se email já existe
     */
    Optional<Employee> findByEmail(String email);
}