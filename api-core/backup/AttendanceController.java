package com.pontualiot.demo.controller;

// Importações necessárias para o controller de ponto
import com.pontualiot.demo.entity.Attendance; // Entidade de registro de ponto
import com.pontualiot.demo.service.AttendanceService; // Serviço de lógica de negócio
import org.springframework.beans.factory.annotation.Autowired; // Injeção de dependência
import org.springframework.http.ResponseEntity; // Resposta HTTP padronizada
import org.springframework.web.bind.annotation.*; // Anotações REST

import java.time.LocalDate; // Data local
import java.util.List;       // Lista de registros
import java.util.Map;        // Mapa para respostas JSON

/**
 * Controller REST para gerenciar registros de ponto
 * Expõe endpoints para check-in, check-out e consultas
 */
@RestController // Marca como controller REST que retorna JSON
@RequestMapping("/api/attendance") // Define prefixo da URL para todos os endpoints
@CrossOrigin(origins = "*") // Permite CORS para frontend (desenvolvimento)
public class AttendanceController {

    // Injeção do serviço de ponto
    @Autowired // Spring injeta automaticamente a implementação
    private AttendanceService attendanceService;

    /**
     * Endpoint para registrar entrada (check-in)
     * POST /api/attendance/check-in
     */
    @PostMapping("/check-in") // Mapeia requisições POST para /check-in
    public ResponseEntity<?> checkIn(@RequestBody Map<String, String> request) {
        try {
            // Extrai RFID tag do corpo da requisição JSON
            String rfidTag = request.get("rfidTag");
            
            // Valida se RFID foi informado
            if (rfidTag == null || rfidTag.trim().isEmpty()) {
                // Retorna erro 400 (Bad Request) se RFID não informado
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "RFID tag é obrigatório"));
            }
            
            // Chama serviço para processar check-in
            Attendance attendance = attendanceService.checkIn(rfidTag);
            
            // Retorna sucesso 200 com dados do registro criado
            return ResponseEntity.ok(Map.of(
                "message", "Check-in realizado com sucesso",
                "attendance", attendance,
                "employee", attendance.getEmployee().getName(),
                "checkIn", attendance.getCheckIn()
            ));
            
        } catch (IllegalArgumentException e) {
            // Captura erros de validação e retorna 400 (Bad Request)
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Captura erros inesperados e retorna 500 (Internal Server Error)
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    /**
     * Endpoint para registrar saída (check-out)
     * POST /api/attendance/check-out
     */
    @PostMapping("/check-out") // Mapeia requisições POST para /check-out
    public ResponseEntity<?> checkOut(@RequestBody Map<String, String> request) {
        try {
            // Extrai RFID tag do corpo da requisição JSON
            String rfidTag = request.get("rfidTag");
            
            // Valida se RFID foi informado
            if (rfidTag == null || rfidTag.trim().isEmpty()) {
                // Retorna erro 400 (Bad Request) se RFID não informado
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "RFID tag é obrigatório"));
            }
            
            // Chama serviço para processar check-out
            Attendance attendance = attendanceService.checkOut(rfidTag);
            
            // Retorna sucesso 200 com dados do registro atualizado
            return ResponseEntity.ok(Map.of(
                "message", "Check-out realizado com sucesso",
                "attendance", attendance,
                "employee", attendance.getEmployee().getName(),
                "checkIn", attendance.getCheckIn(),
                "checkOut", attendance.getCheckOut()
            ));
            
        } catch (IllegalArgumentException e) {
            // Captura erros de validação e retorna 400 (Bad Request)
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Captura erros inesperados e retorna 500 (Internal Server Error)
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    /**
     * Endpoint para consultar registros por período
     * GET /api/attendance/employee/{employeeId}?startDate=2024-01-01&endDate=2024-01-31
     */
    @GetMapping("/employee/{employeeId}") // Mapeia GET com parâmetro de path
    public ResponseEntity<?> getAttendanceByPeriod(
            @PathVariable Long employeeId, // Extrai ID do funcionário da URL
            @RequestParam LocalDate startDate, // Parâmetro query obrigatório
            @RequestParam LocalDate endDate    // Parâmetro query obrigatório
    ) {
        try {
            // Chama serviço para buscar registros no período
            List<Attendance> attendances = attendanceService.getAttendanceByPeriod(
                employeeId, startDate, endDate
            );
            
            // Retorna sucesso 200 com lista de registros
            return ResponseEntity.ok(Map.of(
                "employeeId", employeeId,
                "startDate", startDate,
                "endDate", endDate,
                "totalRecords", attendances.size(),
                "attendances", attendances
            ));
            
        } catch (IllegalArgumentException e) {
            // Captura erros de validação e retorna 400 (Bad Request)
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Captura erros inesperados e retorna 500 (Internal Server Error)
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    /**
     * Endpoint para consultar registros de uma data específica
     * GET /api/attendance/employee/{employeeId}/date/{date}
     */
    @GetMapping("/employee/{employeeId}/date/{date}") // Mapeia GET com dois parâmetros de path
    public ResponseEntity<?> getAttendanceByDate(
            @PathVariable Long employeeId, // Extrai ID do funcionário da URL
            @PathVariable LocalDate date   // Extrai data da URL
    ) {
        try {
            // Chama serviço para buscar registros da data específica
            List<Attendance> attendances = attendanceService.getAttendanceByDate(employeeId, date);
            
            // Retorna sucesso 200 com lista de registros
            return ResponseEntity.ok(Map.of(
                "employeeId", employeeId,
                "date", date,
                "totalRecords", attendances.size(),
                "attendances", attendances
            ));
            
        } catch (IllegalArgumentException e) {
            // Captura erros de validação e retorna 400 (Bad Request)
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Captura erros inesperados e retorna 500 (Internal Server Error)
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    /**
     * Endpoint para consultar registros de hoje de um funcionário
     * GET /api/attendance/employee/{employeeId}/today
     */
    @GetMapping("/employee/{employeeId}/today") // Mapeia GET para registros de hoje
    public ResponseEntity<?> getTodayAttendance(@PathVariable Long employeeId) {
        try {
            // Usa data atual para buscar registros de hoje
            LocalDate today = LocalDate.now();
            
            // Chama serviço para buscar registros de hoje
            List<Attendance> attendances = attendanceService.getAttendanceByDate(employeeId, today);
            
            // Retorna sucesso 200 com lista de registros de hoje
            return ResponseEntity.ok(Map.of(
                "employeeId", employeeId,
                "date", today,
                "totalRecords", attendances.size(),
                "attendances", attendances
            ));
            
        } catch (IllegalArgumentException e) {
            // Captura erros de validação e retorna 400 (Bad Request)
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Captura erros inesperados e retorna 500 (Internal Server Error)
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Erro interno do servidor"));
        }
    }
}