package com.pontualiot.demo.service;

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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private Counter attendanceRecordsCounter;

    @InjectMocks
    private AttendanceService attendanceService;

    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        testEmployee = Employee.builder()
                .id(1L)
                .name("Test Employee")
                .email("test@test.com")
                .rfidTag("TEST001")
                .active(true)
                .build();
    }

    @Test
    void shouldCreateCheckIn() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> i.getArgument(0));

        Attendance result = attendanceService.createCheckIn(1L);

        assertNotNull(result);
        assertEquals(testEmployee, result.getEmployee());
        assertNotNull(result.getCheckIn());
        assertEquals(LocalDate.now(), result.getDate());
        verify(attendanceRecordsCounter).increment();
    }

    @Test
    void shouldCreateCheckOut() {
        Attendance existingAttendance = Attendance.builder()
                .id(1L)
                .employee(testEmployee)
                .checkIn(LocalDateTime.now().minusHours(8))
                .date(LocalDate.now())
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findByEmployeeAndDate(testEmployee, LocalDate.now()))
                .thenReturn(List.of(existingAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(i -> i.getArgument(0));

        Attendance result = attendanceService.createCheckOut(1L);

        assertNotNull(result.getCheckOut());
        verify(attendanceRecordsCounter).increment();
    }

    @Test
    void shouldThrowExceptionWhenEmployeeNotFound() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> attendanceService.createCheckIn(999L));
    }

    @Test
    void shouldGetEmployeeAttendances() {
        List<Attendance> expectedAttendances = List.of(
                Attendance.builder().employee(testEmployee).build()
        );
        when(attendanceRepository.findByEmployeeId(1L)).thenReturn(expectedAttendances);

        List<Attendance> result = attendanceService.getEmployeeAttendances(1L);

        assertEquals(expectedAttendances, result);
    }
}