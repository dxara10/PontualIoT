package com.pontualiot.demo.controller;

import com.pontualiot.demo.entity.Attendance;
import com.pontualiot.demo.entity.Employee;
import com.pontualiot.demo.repository.AttendanceRepository;
import com.pontualiot.demo.repository.EmployeeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/test-attendance")
@Tag(name = "Test Attendance", description = "Temporary endpoints to test attendance creation")
public class AttendanceTestController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @PostMapping("/check-in/{employeeId}")
    @Operation(summary = "Create test check-in for employee")
    public ResponseEntity<Attendance> createCheckIn(@PathVariable Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .checkIn(LocalDateTime.now())
                .date(LocalDate.now())
                .build();

        return ResponseEntity.ok(attendanceRepository.save(attendance));
    }

    @PostMapping("/check-out/{employeeId}")
    @Operation(summary = "Create test check-out for employee")
    public ResponseEntity<Attendance> createCheckOut(@PathVariable Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        // Find today's attendance without check-out
        Attendance attendance = attendanceRepository.findByEmployeeAndDate(employee, LocalDate.now())
                .stream()
                .filter(a -> a.getCheckOut() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No check-in found for today"));

        attendance.setCheckOut(LocalDateTime.now());
        return ResponseEntity.ok(attendanceRepository.save(attendance));
    }
}