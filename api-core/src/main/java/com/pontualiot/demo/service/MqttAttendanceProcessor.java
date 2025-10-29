package com.pontualiot.demo.service;

import com.pontualiot.demo.config.MetricsConfig;
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
public class MqttAttendanceProcessor {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private Counter attendanceRecordsCounter;

    @Autowired
    private MetricsConfig metricsConfig;

    public Attendance processAttendanceEvent(String rfidTag, String eventType, String deviceId) {
        Employee employee = employeeRepository.findByRfidTag(rfidTag)
                .orElseThrow(() -> new RuntimeException("Employee not found for RFID: " + rfidTag));

        Attendance attendance = findOrCreateTodayAttendance(employee);

        if ("CHECK_IN".equals(eventType)) {
            attendance.setCheckIn(LocalDateTime.now());
        } else if ("CHECK_OUT".equals(eventType)) {
            attendance.setCheckOut(LocalDateTime.now());
        }

        attendance = attendanceRepository.save(attendance);
        attendanceRecordsCounter.increment();
        metricsConfig.incrementActiveDevices();

        return attendance;
    }

    private Attendance findOrCreateTodayAttendance(Employee employee) {
        LocalDate today = LocalDate.now();
        List<Attendance> attendances = attendanceRepository.findByEmployeeAndDate(employee, today);
        
        if (!attendances.isEmpty()) {
            return attendances.get(0);
        }
        
        return Attendance.builder()
                .employee(employee)
                .date(today)
                .build();
    }
}