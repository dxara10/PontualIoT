package com.pontualiot.demo.controller;

import com.pontualiot.demo.entity.Employee;
import com.pontualiot.demo.repository.EmployeeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
@Tag(name = "Employees", description = "Employee management operations")
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping
    @Operation(summary = "List all employees")
    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return employeeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new employee")
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        System.out.println("[EMPLOYEE] POST /employees chamado");
        System.out.println("[EMPLOYEE] Dados recebidos: " + employee);
        
        try {
            // Definir timestamps se não estiverem definidos
            if (employee.getCreatedAt() == null) {
                employee.setCreatedAt(java.time.LocalDateTime.now());
            }
            if (employee.getUpdatedAt() == null) {
                employee.setUpdatedAt(java.time.LocalDateTime.now());
            }
            
            System.out.println("[EMPLOYEE] Salvando no banco: " + employee);
            Employee saved = employeeRepository.save(employee);
            System.out.println("[EMPLOYEE] ✅ Salvo com sucesso: " + saved);
            
            return ResponseEntity.status(201).body(saved);
        } catch (Exception e) {
            System.err.println("[EMPLOYEE] ❌ Erro ao salvar: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update employee")
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        return employeeRepository.findById(id)
                .map(existing -> {
                    existing.setName(employee.getName());
                    existing.setEmail(employee.getEmail());
                    existing.setRfidTag(employee.getRfidTag());
                    existing.setActive(employee.isActive());
                    return ResponseEntity.ok(employeeRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete employee")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        if (employeeRepository.existsById(id)) {
            employeeRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}