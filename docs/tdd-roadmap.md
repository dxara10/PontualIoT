# TDD Roadmap - Smart Attendance Platform

## 🎯 Estratégia TDD

### Ciclo Red-Green-Refactor
1. **Red**: Escrever teste que falha
2. **Green**: Implementar código mínimo para passar
3. **Refactor**: Melhorar código mantendo testes passando

## 📋 Ordem de Implementação TDD

### Fase 1: Entidades e Repositórios
```
1. Company Entity Tests
   ├── CompanyRepositoryTest
   ├── Company validation tests
   └── Company business rules

2. Employee Entity Tests
   ├── EmployeeRepositoryTest
   ├── Employee-Company relationship
   └── Unique registration per company

3. Device Entity Tests
   ├── DeviceRepositoryTest
   ├── Device-Company relationship
   └── Serial number uniqueness

4. AttendanceEvent Entity Tests
   ├── AttendanceEventRepositoryTest
   ├── Time-series queries
   └── Event validation rules
```

### Fase 2: Services (Domain Logic)
```
1. CompanyService Tests
   ├── Create company
   ├── Validate CNPJ uniqueness
   └── Soft delete company

2. EmployeeService Tests
   ├── Register employee
   ├── Validate registration uniqueness
   └── Employee activation/deactivation

3. DeviceService Tests
   ├── Register device
   ├── Update device status
   └── Device heartbeat logic

4. AttendanceService Tests
   ├── Process attendance event
   ├── Validate employee-device relationship
   ├── Duplicate event detection
   └── Business rules (work hours, etc.)
```

### Fase 3: Controllers (API Layer)
```
1. CompanyController Tests
   ├── POST /api/companies
   ├── GET /api/companies
   ├── PUT /api/companies/{id}
   └── Authentication/Authorization

2. EmployeeController Tests
   ├── CRUD operations
   ├── Multitenancy validation
   └── Input validation

3. DeviceController Tests
   ├── Device registration
   ├── Status updates
   └── Command sending

4. AttendanceController Tests
   ├── Event ingestion (HTTP)
   ├── Event queries with filters
   └── Report generation
```

### Fase 4: Integration Tests
```
1. MQTT Integration Tests
   ├── Message consumption
   ├── Event processing
   └── Command publishing

2. Database Integration Tests
   ├── TimescaleDB hypertables
   ├── Complex queries
   └── Performance tests

3. Security Integration Tests
   ├── JWT authentication
   ├── Role-based authorization
   └── Multitenancy isolation
```

## 🧪 Exemplos de Testes TDD

### 1. Company Entity Test (Red)
```java
@Test
void shouldCreateCompanyWithValidData() {
    // Given
    Company company = new Company();
    company.setName("Tech Corp");
    company.setDocument("12345678000195");
    
    // When & Then
    assertThat(company.getName()).isEqualTo("Tech Corp");
    assertThat(company.getActive()).isTrue();
    assertThat(company.getId()).isNotNull();
}
```

### 2. Employee Service Test (Red)
```java
@Test
void shouldRegisterEmployeeWithUniqueRegistration() {
    // Given
    Company company = createTestCompany();
    EmployeeRequest request = new EmployeeRequest(
        "João Silva", "12345", "12345678901", "Developer"
    );
    
    // When
    Employee employee = employeeService.register(company.getId(), request);
    
    // Then
    assertThat(employee.getRegistration()).isEqualTo("12345");
    assertThat(employee.getCompany()).isEqualTo(company);
}

@Test
void shouldThrowExceptionWhenRegistrationAlreadyExists() {
    // Given
    Company company = createTestCompany();
    createEmployeeWithRegistration(company, "12345");
    
    EmployeeRequest request = new EmployeeRequest(
        "Maria Santos", "12345", "98765432100", "Manager"
    );
    
    // When & Then
    assertThatThrownBy(() -> 
        employeeService.register(company.getId(), request)
    ).isInstanceOf(RegistrationAlreadyExistsException.class);
}
```

### 3. Attendance Controller Test (Red)
```java
@Test
void shouldProcessAttendanceEventSuccessfully() throws Exception {
    // Given
    AttendanceEventRequest request = AttendanceEventRequest.builder()
        .deviceId(device.getId())
        .employeeRegistration("12345")
        .eventType(AttendanceEventType.CHECK_IN)
        .eventTime(OffsetDateTime.now())
        .build();
    
    // When & Then
    mockMvc.perform(post("/api/events")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.eventType").value("CHECK_IN"));
}
```

## 🚀 Próximos Passos

### Implementação Imediata
1. **Setup do ambiente de teste** (TestContainers + PostgreSQL)
2. **Company Entity + Repository** (primeiro ciclo TDD)
3. **CompanyService** (regras de negócio)
4. **CompanyController** (API REST)

### Ferramentas TDD
- **JUnit 5**: Framework de testes
- **AssertJ**: Assertions fluentes
- **Mockito**: Mocks e stubs
- **TestContainers**: Testes de integração
- **WireMock**: Mock de APIs externas

Pronto para começar o primeiro ciclo TDD? 🎯