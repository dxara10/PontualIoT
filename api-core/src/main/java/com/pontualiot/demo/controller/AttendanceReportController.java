package com.pontualiot.demo.controller;

import com.pontualiot.demo.entity.Attendance;
import com.pontualiot.demo.repository.AttendanceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "Attendance reports and analytics")
public class AttendanceReportController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @GetMapping("/daily/{date}")
    @Operation(summary = "Get daily attendance report")
    public Map<String, Object> getDailyReport(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<Attendance> attendances = attendanceRepository.findByDate(date);
        
        long totalEmployees = attendances.size();
        long checkedIn = attendances.stream()
                .filter(a -> a.getCheckIn() != null)
                .count();
        long checkedOut = attendances.stream()
                .filter(a -> a.getCheckOut() != null)
                .count();
        
        return Map.of(
            "date", date,
            "totalEmployees", totalEmployees,
            "checkedIn", checkedIn,
            "checkedOut", checkedOut,
            "stillWorking", checkedIn - checkedOut,
            "attendances", attendances
        );
    }

    @GetMapping("/employee/{employeeId}/period")
    @Operation(summary = "Get employee attendance for period")
    public List<Attendance> getEmployeePeriodReport(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        return attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, startDate, endDate);
    }
}