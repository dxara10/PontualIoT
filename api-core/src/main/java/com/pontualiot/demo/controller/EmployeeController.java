package com.pontualiot.demo.controller;

// ========================================
// IMPORTAÇÕES SPRING MVC E ENTIDADES
// ========================================
import com.pontualiot.demo.entity.Employee;           // Entidade de funcionário
import com.pontualiot.demo.repository.EmployeeRepository; // Repositório de dados
import io.swagger.v3.oas.annotations.Operation;       // Documentação OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;        // Agrupamento de endpoints
import org.springframework.beans.factory.annotation.Autowired; // Injeção de dependência
import org.springframework.http.ResponseEntity;        // Wrapper para respostas HTTP
import org.springframework.web.bind.annotation.*;      // Anotações REST

import java.util.List; // Lista de resultados

/**
 * ========================================
 * CONTROLLER EMPLOYEE - GESTÃO DE FUNCIONÁRIOS
 * ========================================
 * 
 * RESPONSABILIDADES:
 * - Expor endpoints CRUD para funcionários
 * - Validar dados de entrada (nome, email, RFID)
 * - Gerenciar constraints de unicidade (email, RFID)
 * - Controlar status ativo/inativo
 * - Manter auditoria de criação/atualização
 * 
 * ENDPOINTS EXPOSTOS:
 * - GET /api/employees -> Lista todos os funcionários
 * - GET /api/employees/{id} -> Busca por ID específico
 * - POST /api/employees -> Cria novo funcionário
 * - PUT /api/employees/{id} -> Atualiza funcionário existente
 * - DELETE /api/employees/{id} -> Remove funcionário
 * 
 * FLUXO DE CRIAÇÃO TÍPICO:
 * 1. Cliente: POST /api/employees + JSON
 * 2. Spring: deserializa JSON para Employee
 * 3. Controller: valida dados e timestamps
 * 4. Repository: save() executa INSERT
 * 5. PostgreSQL: verifica constraints UNIQUE
 * 6. Se sucesso: retorna 201 Created + Employee com ID
 * 7. Se erro: retorna 409 Conflict ou 400 Bad Request
 * 
 * VALIDAÇÕES IMPLEMENTADAS:
 * - Email único (constraint no banco)
 * - RFID único (constraint no banco)
 * - Campos obrigatórios: name, email, rfidTag
 * - Timestamps automáticos: createdAt, updatedAt
 * 
 * REGRAS DE NEGÓCIO:
 * - Funcionário inativo não pode registrar ponto
 * - Email e RFID devem ser únicos no sistema
 * - Soft delete: marcar como inativo ao invés de deletar
 * - Auditoria completa de mudanças
 */
@RestController // Spring: combina @Controller + @ResponseBody
@RequestMapping("/employees") // Base path: /api/employees
@Tag(name = "Employees", description = "Employee management operations") // OpenAPI
public class EmployeeController {

    /**
     * INJEÇÃO DE DEPENDÊNCIA - REPOSITÓRIO DE FUNCIONÁRIOS
     * 
     * Spring injeta automaticamente a implementação.
     * Fornece acesso aos dados sem acoplamento direto.
     */
    @Autowired
    private EmployeeRepository employeeRepository;

    /**
     * ENDPOINT: LISTAR TODOS OS FUNCIONÁRIOS
     * 
     * GET /api/employees
     * 
     * CASOS DE USO:
     * - Dashboard administrativo
     * - Seleção de funcionário em formulários
     * - Relatórios gerais
     * - Sincronização com sistemas externos
     * 
     * RESPOSTA:
     * [
     *   {
     *     "id": 1,
     *     "name": "João Silva",
     *     "email": "joao@empresa.com",
     *     "rfidTag": "RFID001",
     *     "active": true,
     *     "createdAt": "2024-10-30T10:00:00",
     *     "updatedAt": "2024-10-30T10:00:00"
     *   }
     * ]
     * 
     * @return List<Employee> todos os funcionários cadastrados
     */
    @GetMapping // Mapeia GET /employees
    @Operation(summary = "List all employees") // Documentação OpenAPI
    public List<Employee> getAllEmployees() {
        // Busca todos os funcionários (incluindo inativos)
        return employeeRepository.findAll();
    }

    /**
     * ENDPOINT: BUSCAR FUNCIONÁRIO POR ID
     * 
     * GET /api/employees/{id}
     * 
     * CASOS DE USO:
     * - Consulta de dados específicos
     * - Edição de funcionário
     * - Validação de existência
     * - Detalhes para relatórios
     * 
     * FLUXO:
     * 1. Spring extrai {id} da URL
     * 2. Converte String para Long automaticamente
     * 3. Chama employeeRepository.findById(id)
     * 4. Se encontrado: retorna 200 OK + dados
     * 5. Se não encontrado: retorna 404 Not Found
     * 
     * @param id ID do funcionário
     * @return ResponseEntity<Employee> 200 OK ou 404 Not Found
     */
    @GetMapping("/{id}") // Mapeia GET /employees/123
    @Operation(summary = "Get employee by ID") // Documentação OpenAPI
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        return employeeRepository.findById(id)
                .map(ResponseEntity::ok)           // Se presente: 200 OK
                .orElse(ResponseEntity.notFound().build()); // Se ausente: 404
    }

    /**
     * ENDPOINT: CRIAR NOVO FUNCIONÁRIO
     * 
     * POST /api/employees
     * 
     * ENDPOINT CRÍTICO PARA CADASTRO!
     * 
     * CASOS DE USO:
     * - Cadastro de novos funcionários
     * - Integração com RH
     * - Importação em lote
     * - Onboarding de funcionários
     * 
     * PAYLOAD ESPERADO:
     * {
     *   "name": "João Silva",
     *   "email": "joao@empresa.com",
     *   "rfidTag": "RFID001",
     *   "active": true
     * }
     * 
     * VALIDAÇÕES REALIZADAS:
     * - Campos obrigatórios: name, email, rfidTag
     * - Email único (constraint no banco)
     * - RFID único (constraint no banco)
     * - Timestamps automáticos se não fornecidos
     * 
     * FLUXO COMPLETO:
     * 1. Cliente: POST /api/employees + JSON
     * 2. Jackson: deserializa JSON para Employee
     * 3. Controller: valida e completa timestamps
     * 4. Repository: save() executa INSERT SQL
     * 5. PostgreSQL: verifica constraints UNIQUE
     * 6. Se sucesso: retorna 201 Created + Employee com ID
     * 7. Se constraint violada: SQLException -> 409 Conflict
     * 8. Se dados inválidos: 400 Bad Request
     * 
     * LOGS GERADOS:
     * - [EMPLOYEE] POST /employees chamado
     * - [EMPLOYEE] Dados recebidos: Employee(...)
     * - [EMPLOYEE] ✅ Salvo com sucesso: Employee(id=1, ...)
     * 
     * @param employee Dados do funcionário (JSON -> Object)
     * @return ResponseEntity<Employee> 201 Created ou erro
     */
    @PostMapping // Mapeia POST /employees
    @Operation(summary = "Create new employee") // Documentação OpenAPI
    public ResponseEntity<Employee> createEmployee(@RequestBody Employee employee) {
        // Log da requisição recebida
        System.out.println("[EMPLOYEE] POST /employees chamado");
        System.out.println("[EMPLOYEE] Dados recebidos: " + employee);
        
        try {
            // VALIDAÇÃO E COMPLETUDE DE TIMESTAMPS
            // Se timestamps não foram fornecidos, define valores padrão
            if (employee.getCreatedAt() == null) {
                employee.setCreatedAt(java.time.LocalDateTime.now());
            }
            if (employee.getUpdatedAt() == null) {
                employee.setUpdatedAt(java.time.LocalDateTime.now());
            }
            
            // Log antes de salvar
            System.out.println("[EMPLOYEE] Salvando no banco: " + employee);
            
            // PERSISTÊNCIA NO BANCO
            // save() executa INSERT SQL com validação de constraints
            Employee saved = employeeRepository.save(employee);
            
            // Log de sucesso
            System.out.println("[EMPLOYEE] ✅ Salvo com sucesso: " + saved);
            
            // Retorna 201 Created com Employee incluindo ID gerado
            return ResponseEntity.status(201).body(saved);
            
        } catch (Exception e) {
            // Log de erro detalhado
            System.err.println("[EMPLOYEE] ❌ Erro ao salvar: " + e.getMessage());
            e.printStackTrace();
            
            // Re-lança exceção para tratamento global
            // Spring converte em resposta HTTP apropriada
            throw e;
        }
    }

    /**
     * ENDPOINT: ATUALIZAR FUNCIONÁRIO EXISTENTE
     * 
     * PUT /api/employees/{id}
     * 
     * CASOS DE USO:
     * - Edição de dados pessoais
     * - Mudança de status (ativo/inativo)
     * - Atualização de RFID
     * - Correção de informações
     * 
     * ESTRATÉGIA DE ATUALIZAÇÃO:
     * - Busca funcionário existente por ID
     * - Se encontrado: atualiza campos específicos
     * - Se não encontrado: retorna 404 Not Found
     * - Preserva ID e timestamps de criação
     * - Atualiza updatedAt automaticamente (@PreUpdate)
     * 
     * FLUXO:
     * 1. Cliente: PUT /api/employees/1 + JSON
     * 2. Controller: busca Employee existente
     * 3. Se encontrado: copia novos valores
     * 4. Repository: save() executa UPDATE SQL
     * 5. @PreUpdate: atualiza updatedAt automaticamente
     * 6. Retorna 200 OK + Employee atualizado
     * 
     * CAMPOS ATUALIZÁVEIS:
     * - name: nome completo
     * - email: endereço de email (deve manter unicidade)
     * - rfidTag: tag RFID (deve manter unicidade)
     * - active: status ativo/inativo
     * 
     * @param id ID do funcionário a ser atualizado
     * @param employee Novos dados do funcionário
     * @return ResponseEntity<Employee> 200 OK ou 404 Not Found
     */
    @PutMapping("/{id}") // Mapeia PUT /employees/123
    @Operation(summary = "Update employee") // Documentação OpenAPI
    public ResponseEntity<Employee> updateEmployee(@PathVariable Long id, @RequestBody Employee employee) {
        return employeeRepository.findById(id)
                .map(existing -> {
                    // Atualiza campos específicos (preserva ID e createdAt)
                    existing.setName(employee.getName());
                    existing.setEmail(employee.getEmail());
                    existing.setRfidTag(employee.getRfidTag());
                    existing.setActive(employee.isActive());
                    // updatedAt é atualizado automaticamente por @PreUpdate
                    
                    // Salva alterações e retorna 200 OK
                    return ResponseEntity.ok(employeeRepository.save(existing));
                })
                .orElse(ResponseEntity.notFound().build()); // 404 se não encontrado
    }

    /**
     * ENDPOINT: REMOVER FUNCIONÁRIO
     * 
     * DELETE /api/employees/{id}
     * 
     * CASOS DE USO:
     * - Desligamento de funcionário
     * - Limpeza de dados de teste
     * - Correção de cadastros duplicados
     * - Compliance com LGPD (direito ao esquecimento)
     * 
     * ESTRATÉGIA DE REMOÇÃO:
     * - Hard delete: remove completamente do banco
     * - Verifica existência antes de deletar
     * - Pode falhar se houver registros de ponto relacionados
     * 
     * CONSIDERAÇÕES:
     * - Em produção, considerar soft delete (active = false)
     * - Registros de ponto podem impedir deleção (FK constraint)
     * - Backup dos dados antes de deletar
     * 
     * FLUXO:
     * 1. Cliente: DELETE /api/employees/1
     * 2. Controller: verifica se funcionário existe
     * 3. Se existe: executa DELETE SQL
     * 4. Se não existe: retorna 404 Not Found
     * 5. Se FK constraint: retorna 409 Conflict
     * 6. Se sucesso: retorna 200 OK (sem body)
     * 
     * @param id ID do funcionário a ser removido
     * @return ResponseEntity<Void> 200 OK ou 404 Not Found
     */
    @DeleteMapping("/{id}") // Mapeia DELETE /employees/123
    @Operation(summary = "Delete employee") // Documentação OpenAPI
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        // Verifica se funcionário existe antes de tentar deletar
        if (employeeRepository.existsById(id)) {
            // Executa DELETE SQL
            employeeRepository.deleteById(id);
            
            // Retorna 200 OK sem body
            return ResponseEntity.ok().build();
        }
        
        // Retorna 404 Not Found se funcionário não existe
        return ResponseEntity.notFound().build();
    }
}