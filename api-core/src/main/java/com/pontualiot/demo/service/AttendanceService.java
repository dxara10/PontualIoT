package com.pontualiot.demo.service;

import com.pontualiot.demo.entity.Attendance;
import com.pontualiot.demo.entity.Employee;
import com.pontualiot.demo.repository.AttendanceRepository;
import com.pontualiot.demo.repository.EmployeeRepository;
import io.micrometer.core.instrument.Counter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private Counter attendanceRecordsCounter;

    public Attendance createCheckIn(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .checkIn(LocalDateTime.now())
                .date(LocalDate.now())
                .build();

        attendanceRecordsCounter.increment();
        return attendanceRepository.save(attendance);
    }

    public Attendance createCheckOut(Long employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        List<Attendance> todayAttendances = attendanceRepository
                .findByEmployeeAndDate(employee, LocalDate.now());

        Attendance attendance = todayAttendances.stream()
                .filter(a -> a.getCheckOut() == null)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No check-in found for today"));

        attendance.setCheckOut(LocalDateTime.now());
        attendanceRecordsCounter.increment();
        return attendanceRepository.save(attendance);
    }

    public List<Attendance> getEmployeeAttendances(Long employeeId) {
        return attendanceRepository.findByEmployeeId(employeeId);
    }

    public List<Attendance> getAttendancesByDate(LocalDate date) {
        return attendanceRepository.findByDate(date);
    }
}