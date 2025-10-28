package com.pontualiot.demo.controller;

// Importações necessárias para o controller de funcionários
import com.pontualiot.demo.entity.Employee; // Entidade de funcionário
import com.pontualiot.demo.service.EmployeeService; // Serviço de lógica de negócio
import org.springframework.beans.factory.annotation.Autowired; // Injeção de dependência
import org.springframework.http.ResponseEntity; // Resposta HTTP padronizada
import org.springframework.web.bind.annotation.*; // Anotações REST

import java.util.List; // Lista de funcionários
import java.util.Map;  // Mapa para respostas JSON

/**
 * Controller REST para gerenciar funcionários
 * Expõe endpoints CRUD para funcionários
 */
@RestController // Marca como controller REST que retorna JSON
@RequestMapping("/api/employees") // Define prefixo da URL para todos os endpoints
@CrossOrigin(origins = "*") // Permite CORS para frontend (desenvolvimento)
public class EmployeeController {

    // Injeção do serviço de funcionários
    @Autowired // Spring injeta automaticamente a implementação
    private EmployeeService employeeService;

    /**
     * Endpoint para criar novo funcionário
     * POST /api/employees
     */
    @PostMapping // Mapeia requisições POST para a raiz do controller
    public ResponseEntity<?> createEmployee(@RequestBody Employee employee) {
        try {
            // Chama serviço para criar funcionário com validações
            Employee createdEmployee = employeeService.createEmployee(employee);
            
            // Retorna sucesso 201 (Created) com dados do funcionário criado
            return ResponseEntity.status(201).body(Map.of(
                "message", "Funcionário criado com sucesso",
                "employee", createdEmployee
            ));
            
        } catch (IllegalArgumentException e) {
            // Captura erros de validação e retorna 400 (Bad Request)
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Captura erros inesperados e retorna 500 (Internal Server Error)
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    /**
     * Endpoint para listar todos os funcionários
     * GET /api/employees
     */
    @GetMapping // Mapeia requisições GET para a raiz do controller
    public ResponseEntity<?> getAllEmployees() {
        try {
            // Chama serviço para buscar todos os funcionários
            List<Employee> employees = employeeService.findAll();
            
            // Retorna sucesso 200 com lista de funcionários
            return ResponseEntity.ok(Map.of(
                "totalEmployees", employees.size(),
                "employees", employees
            ));
            
        } catch (Exception e) {
            // Captura erros inesperados e retorna 500 (Internal Server Error)
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    /**
     * Endpoint para buscar funcionário por ID
     * GET /api/employees/{id}
     */
    @GetMapping("/{id}") // Mapeia GET com parâmetro de path
    public ResponseEntity<?> getEmployeeById(@PathVariable Long id) {
        try {
            // Chama serviço para buscar funcionário por ID
            Employee employee = employeeService.findById(id);
            
            // Retorna sucesso 200 com dados do funcionário
            return ResponseEntity.ok(employee);
            
        } catch (IllegalArgumentException e) {
            // Captura erros de validação e retorna 404 (Not Found)
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // Captura erros inesperados e retorna 500 (Internal Server Error)
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    /**
     * Endpoint para buscar funcionário por RFID
     * GET /api/employees/rfid/{rfidTag}
     */
    @GetMapping("/rfid/{rfidTag}") // Mapeia GET com parâmetro de path para RFID
    public ResponseEntity<?> getEmployeeByRfid(@PathVariable String rfidTag) {
        try {
            // Chama serviço para buscar funcionário por RFID
            Employee employee = employeeService.findByRfidTag(rfidTag);
            
            // Retorna sucesso 200 com dados do funcionário
            return ResponseEntity.ok(employee);
            
        } catch (IllegalArgumentException e) {
            // Captura erros de validação e retorna 404 (Not Found)
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // Captura erros inesperados e retorna 500 (Internal Server Error)
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    /**
     * Endpoint para buscar funcionário por email
     * GET /api/employees/email/{email}
     */
    @GetMapping("/email/{email}") // Mapeia GET com parâmetro de path para email
    public ResponseEntity<?> getEmployeeByEmail(@PathVariable String email) {
        try {
            // Chama serviço para buscar funcionário por email
            Employee employee = employeeService.findByEmail(email);
            
            // Retorna sucesso 200 com dados do funcionário
            return ResponseEntity.ok(employee);
            
        } catch (IllegalArgumentException e) {
            // Captura erros de validação e retorna 404 (Not Found)
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // Captura erros inesperados e retorna 500 (Internal Server Error)
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    /**
     * Endpoint para atualizar funcionário
     * PUT /api/employees/{id}
     */
    @PutMapping("/{id}") // Mapeia requisições PUT com parâmetro de path
    public ResponseEntity<?> updateEmployee(
            @PathVariable Long id, // Extrai ID da URL
            @RequestBody Employee employee // Extrai dados do corpo da requisição
    ) {
        try {
            // Chama serviço para atualizar funcionário
            Employee updatedEmployee = employeeService.updateEmployee(id, employee);
            
            // Retorna sucesso 200 com dados do funcionário atualizado
            return ResponseEntity.ok(Map.of(
                "message", "Funcionário atualizado com sucesso",
                "employee", updatedEmployee
            ));
            
        } catch (IllegalArgumentException e) {
            // Captura erros de validação e retorna 400 (Bad Request)
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Captura erros inesperados e retorna 500 (Internal Server Error)
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    /**
     * Endpoint para desativar funcionário (soft delete)
     * PUT /api/employees/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate") // Mapeia PUT para desativação
    public ResponseEntity<?> deactivateEmployee(@PathVariable Long id) {
        try {
            // Chama serviço para desativar funcionário
            Employee deactivatedEmployee = employeeService.deactivateEmployee(id);
            
            // Retorna sucesso 200 com confirmação
            return ResponseEntity.ok(Map.of(
                "message", "Funcionário desativado com sucesso",
                "employee", deactivatedEmployee
            ));
            
        } catch (IllegalArgumentException e) {
            // Captura erros de validação e retorna 404 (Not Found)
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // Captura erros inesperados e retorna 500 (Internal Server Error)
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    /**
     * Endpoint para ativar funcionário
     * PUT /api/employees/{id}/activate
     */
    @PutMapping("/{id}/activate") // Mapeia PUT para ativação
    public ResponseEntity<?> activateEmployee(@PathVariable Long id) {
        try {
            // Chama serviço para ativar funcionário
            Employee activatedEmployee = employeeService.activateEmployee(id);
            
            // Retorna sucesso 200 com confirmação
            return ResponseEntity.ok(Map.of(
                "message", "Funcionário ativado com sucesso",
                "employee", activatedEmployee
            ));
            
        } catch (IllegalArgumentException e) {
            // Captura erros de validação e retorna 404 (Not Found)
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // Captura erros inesperados e retorna 500 (Internal Server Error)
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    /**
     * Endpoint para validar RFID (usado pelos dispositivos IoT)
     * POST /api/employees/validate-rfid
     */
    @PostMapping("/validate-rfid") // Mapeia POST para validação de RFID
    public ResponseEntity<?> validateRfid(@RequestBody Map<String, String> request) {
        try {
            // Extrai RFID tag do corpo da requisição
            String rfidTag = request.get("rfidTag");
            
            // Valida se RFID foi informado
            if (rfidTag == null || rfidTag.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "RFID tag é obrigatório"));
            }
            
            // Busca funcionário pelo RFID
            Employee employee = employeeService.findByRfidTag(rfidTag);
            
            // Verifica se funcionário está ativo
            if (!employee.isActive()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Funcionário inativo"));
            }
            
            // Retorna sucesso com dados básicos do funcionário
            return ResponseEntity.ok(Map.of(
                "valid", true,
                "employeeId", employee.getId(),
                "employeeName", employee.getName(),
                "active", employee.isActive()
            ));
            
        } catch (IllegalArgumentException e) {
            // RFID não encontrado - retorna inválido
            return ResponseEntity.ok(Map.of(
                "valid", false,
                "error", "RFID não encontrado"
            ));
        } catch (Exception e) {
            // Captura erros inesperados e retorna 500 (Internal Server Error)
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erro interno do servidor"));
        }
    }
}