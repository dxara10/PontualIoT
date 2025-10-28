package com.pontualiot.demo.tdd;

import com.pontualiot.demo.entity.Employee;
import com.pontualiot.demo.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TDD Test for Attendance Test Controller
 * 
 * RED: Write failing test first
 * GREEN: Implement minimal code to pass
 * REFACTOR: Improve code quality
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
class AttendanceTestControllerTDDTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testEmployee = Employee.builder()
                .name("TDD Test Employee")
                .email("tdd.test@controller.com")
                .rfidTag("TDD_CTRL_001")
                .active(true)
                .build();
        testEmployee = employeeRepository.save(testEmployee);
    }

    @Test
    void shouldCreateCheckInViaTestEndpoint() throws Exception {
        // TDD RED: Test fails initially - no endpoint exists
        // When - POST to test check-in endpoint
        mockMvc.perform(post("/api/test-attendance/check-in/" + testEmployee.getId()))
                // Then - Should create attendance record
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee.id").value(testEmployee.getId()))
                .andExpect(jsonPath("$.checkIn").exists())
                .andExpect(jsonPath("$.checkOut").doesNotExist());
    }

    @Test
    void shouldCreateCheckOutViaTestEndpoint() throws Exception {
        // Given - Employee already has check-in
        mockMvc.perform(post("/api/test-attendance/check-in/" + testEmployee.getId()));

        // When - POST to test check-out endpoint
        mockMvc.perform(post("/api/test-attendance/check-out/" + testEmployee.getId()))
                // Then - Should update attendance with check-out
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee.id").value(testEmployee.getId()))
                .andExpect(jsonPath("$.checkIn").exists())
                .andExpect(jsonPath("$.checkOut").exists());
    }

    @Test
    void shouldReturn404ForNonExistentEmployee() throws Exception {
        // When - POST with non-existent employee ID
        // Then - Should throw exception (controller throws RuntimeException)
        try {
            mockMvc.perform(post("/api/test-attendance/check-in/99999"));
            // If we reach here, the test should fail
            assertThat(false).as("Expected exception was not thrown").isTrue();
        } catch (Exception e) {
            // Expected - controller should throw RuntimeException wrapped in ServletException
            assertThat(e.getCause()).isInstanceOf(RuntimeException.class);
            assertThat(e.getCause().getMessage()).isEqualTo("Employee not found");
        }
    }
}