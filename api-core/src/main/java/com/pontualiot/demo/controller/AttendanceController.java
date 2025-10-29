package com.pontualiot.demo.controller;

import com.pontualiot.demo.config.MetricsConfig;
import com.pontualiot.demo.entity.Attendance;
import com.pontualiot.demo.repository.AttendanceRepository;
import io.micrometer.core.instrument.Counter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/attendances")
@Tag(name = "Attendances", description = "Attendance record operations")
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;
    
    @Autowired
    private Counter attendanceRecordsCounter;

    @GetMapping
    @Operation(summary = "List all attendance records")
    public List<Attendance> getAllAttendances() {
        attendanceRecordsCounter.increment();
        return attendanceRepository.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get attendance by ID")
    public ResponseEntity<Attendance> getAttendanceById(@PathVariable Long id) {
        return attendanceRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get attendances by employee ID")
    public List<Attendance> getAttendancesByEmployee(@PathVariable Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId);
    }

    @GetMapping("/date/{date}")
    @Operation(summary = "Get attendances by date")
    public List<Attendance> getAttendancesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return attendanceRepository.findByDate(date);
    }
}