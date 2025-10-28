package com.pontualiot.demo.tdd;

import com.pontualiot.demo.entity.Attendance;
import com.pontualiot.demo.entity.Employee;
import com.pontualiot.demo.mqtt.MqttAttendanceService;
import com.pontualiot.demo.repository.AttendanceRepository;
import com.pontualiot.demo.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * TDD Test for MQTT Communication
 * 
 * Demonstrates Test-Driven Development approach for IoT device communication:
 * 1. RED: Write failing test first
 * 2. GREEN: Implement minimal code to pass
 * 3. REFACTOR: Improve code quality
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
class MqttTDDTest {

    @Autowired
    private MqttAttendanceService mqttService;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testEmployee = Employee.builder()
                .name("TDD MQTT Employee")
                .email("tdd.mqtt@test.com")
                .rfidTag("TDD_MQTT_001")
                .active(true)
                .build();
        testEmployee = employeeRepository.save(testEmployee);
    }

    @Test
    void shouldProcessMqttCheckInFromIoTDevice() {
        // TDD RED: Test fails initially - no implementation
        // Given - MQTT message from IoT device
        String mqttPayload = """
            {
                "deviceId": "IOT_DEVICE_001",
                "rfidTag": "TDD_MQTT_001",
                "eventType": "CHECK_IN",
                "timestamp": "2024-01-15T08:30:00"
            }
            """;

        // When - Process MQTT message (GREEN: implement to pass)
        Attendance result = mqttService.processMqttMessage("devices/attendance", mqttPayload);

        // Then - Verify IoT device communication worked
        assertThat(result).isNotNull();
        assertThat(result.getEmployee().getRfidTag()).isEqualTo("TDD_MQTT_001");
        assertThat(result.getCheckIn()).isEqualTo(LocalDateTime.of(2024, 1, 15, 8, 30));
        assertThat(result.getCheckOut()).isNull();
        assertThat(result.getDate()).isEqualTo(LocalDate.of(2024, 1, 15));
    }

    @Test
    void shouldRejectInvalidMqttPayloadFromIoTDevice() {
        // TDD RED: Test validation logic
        // Given - Invalid MQTT payload from IoT device
        String invalidPayload = """
            {
                "deviceId": "IOT_DEVICE_001",
                "eventType": "CHECK_IN"
            }
            """;

        // When & Then - Should reject invalid IoT message
        assertThrows(IllegalArgumentException.class, () -> {
            mqttService.processMqttMessage("devices/attendance", invalidPayload);
        });
    }

    @Test
    void shouldHandleMqttCheckOutFromIoTDevice() {
        // Given - Employee already checked in via IoT
        Attendance checkIn = Attendance.builder()
                .employee(testEmployee)
                .checkIn(LocalDateTime.of(2024, 1, 15, 8, 30))
                .date(LocalDate.of(2024, 1, 15))
                .build();
        attendanceRepository.save(checkIn);

        // MQTT check-out message from IoT device
        String mqttPayload = """
            {
                "deviceId": "IOT_DEVICE_001",
                "rfidTag": "TDD_MQTT_001",
                "eventType": "CHECK_OUT",
                "timestamp": "2024-01-15T17:30:00"
            }
            """;

        // When - Process IoT check-out
        Attendance result = mqttService.processMqttMessage("devices/attendance", mqttPayload);

        // Then - Verify IoT check-out processed
        assertThat(result.getCheckOut()).isEqualTo(LocalDateTime.of(2024, 1, 15, 17, 30));
        assertThat(result.getCheckIn()).isNotNull();
    }
}