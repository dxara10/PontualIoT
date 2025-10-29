package com.pontualiot.demo.controller;

import com.pontualiot.demo.entity.Attendance;
import com.pontualiot.demo.entity.Employee;
import com.pontualiot.demo.repository.AttendanceRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AttendanceReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttendanceRepository attendanceRepository;

    @Test
    void shouldGetDailyReport() throws Exception {
        Employee employee = Employee.builder()
                .id(1L)
                .name("Test Employee")
                .build();

        Attendance attendance = Attendance.builder()
                .id(1L)
                .employee(employee)
                .checkIn(LocalDateTime.now())
                .date(LocalDate.now())
                .build();

        when(attendanceRepository.findByDate(LocalDate.now()))
                .thenReturn(List.of(attendance));

        mockMvc.perform(get("/api/reports/daily/" + LocalDate.now()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value(LocalDate.now().toString()))
                .andExpect(jsonPath("$.totalEmployees").value(1))
                .andExpect(jsonPath("$.checkedIn").value(1));
    }

    @Test
    void shouldGetEmployeePeriodReport() throws Exception {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        when(attendanceRepository.findByEmployeeIdAndDateBetween(1L, startDate, endDate))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/reports/employee/1/period")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}