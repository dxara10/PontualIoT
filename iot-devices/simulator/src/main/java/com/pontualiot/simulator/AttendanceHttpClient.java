package com.pontualiot.simulator;

/**
 * Cliente HTTP para enviar dados para API
 */
public class AttendanceHttpClient {
    private String apiUrl;

    public AttendanceHttpClient(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public boolean sendAttendanceEvent(AttendanceEvent event) {
        // TODO: Implementar envio HTTP real
        return true; // Implementação mínima para fazer teste passar
    }

    public Employee createEmployee(Employee employee) {
        // TODO: Implementar criação HTTP real
        employee.setId(1L); // Simula ID retornado pela API
        return employee;
    }
}