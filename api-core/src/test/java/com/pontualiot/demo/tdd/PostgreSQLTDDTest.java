package com.pontualiot.demo.tdd;

import com.pontualiot.demo.entity.Employee;
import com.pontualiot.demo.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TDD Test for PostgreSQL Database Communication using Testcontainers
 * 
 * This test demonstrates:
 * 1. Test-Driven Development approach
 * 2. PostgreSQL container integration
 * 3. Database persistence verification
 */
@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PostgreSQLTDDTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void shouldPersistEmployeeToPostgreSQLContainer() {
        // TDD Step 1: Write failing test first
        // Given - Create employee entity
        Employee employee = Employee.builder()
                .name("TDD PostgreSQL Test")
                .email("tdd@postgresql.test")
                .rfidTag("TDD_PG_001")
                .active(true)
                .build();

        // When - Save to PostgreSQL via Testcontainers
        Employee saved = employeeRepository.save(employee);
        entityManager.flush(); // Force database write

        // Then - Verify PostgreSQL persistence
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("TDD PostgreSQL Test");
        assertThat(saved.getEmail()).isEqualTo("tdd@postgresql.test");
        assertThat(saved.getRfidTag()).isEqualTo("TDD_PG_001");
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldFindEmployeeByRfidTagInPostgreSQL() {
        // Given - Persist employee to PostgreSQL
        Employee employee = Employee.builder()
                .name("RFID PostgreSQL Test")
                .email("rfid@postgresql.test")
                .rfidTag("RFID_PG_123")
                .active(true)
                .build();
        
        entityManager.persistAndFlush(employee);

        // When - Query PostgreSQL by RFID
        var found = employeeRepository.findByRfidTag("RFID_PG_123");

        // Then - Verify PostgreSQL query result
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("RFID PostgreSQL Test");
        assertThat(found.get().getEmail()).isEqualTo("rfid@postgresql.test");
    }

    @Test
    void shouldFindEmployeeByEmailInPostgreSQL() {
        // Given - Persist employee to PostgreSQL
        Employee employee = Employee.builder()
                .name("Email PostgreSQL Test")
                .email("email@postgresql.test")
                .rfidTag("EMAIL_PG_456")
                .active(true)
                .build();
        
        entityManager.persistAndFlush(employee);

        // When - Query PostgreSQL by email
        var found = employeeRepository.findByEmail("email@postgresql.test");

        // Then - Verify PostgreSQL query result
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Email PostgreSQL Test");
        assertThat(found.get().getRfidTag()).isEqualTo("EMAIL_PG_456");
    }
}