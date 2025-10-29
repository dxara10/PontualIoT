package com.pontualiot.demo.service;

import com.pontualiot.demo.config.MetricsConfig;
import com.pontualiot.demo.entity.Attendance;
import com.pontualiot.demo.entity.Employee;
import com.pontualiot.demo.repository.AttendanceRepository;
import com.pontualiot.demo.repository.EmployeeRepository;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MqttAttendanceProcessorTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private Counter attendanceRecordsCounter;

    @Mock
    private MetricsConfig metricsConfig;

    @InjectMocks
    private MqttAttendanceProcessor processor;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testEmployee = Employee.builder()
                .id(1L)
                .name("Test Employee")
                .rfidTag("TEST001")
                .build();
    }

    @Test
    void shouldProcessCheckInEvent() {
        when(employeeRepository.findByRfidTag("TEST001")).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndDate(testEmployee, LocalDate.now()))
                .thenReturn(List.of());
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> i.getArgument(0));

        Attendance result = processor.processAttendanceEvent("TEST001", "CHECK_IN", "DEVICE001");

        assertNotNull(result);
        assertEquals(testEmployee, result.getEmployee());
        assertNotNull(result.getCheckIn());
        verify(attendanceRecordsCounter).increment();
        verify(metricsConfig).incrementActiveDevices();
    }

    @Test
    void shouldProcessCheckOutEvent() {
        Attendance existingAttendance = Attendance.builder()
                .employee(testEmployee)
                .date(LocalDate.now())
                .build();

        when(employeeRepository.findByRfidTag("TEST001")).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndDate(testEmployee, LocalDate.now()))
                .thenReturn(List.of(existingAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> i.getArgument(0));

        Attendance result = processor.processAttendanceEvent("TEST001", "CHECK_OUT", "DEVICE001");

        assertNotNull(result.getCheckOut());
        verify(attendanceRecordsCounter).increment();
    }

    @Test
    void shouldThrowExceptionForUnknownEmployee() {
        when(employeeRepository.findByRfidTag("UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, 
                () -> processor.processAttendanceEvent("UNKNOWN", "CHECK_IN", "DEVICE001"));
    }
}