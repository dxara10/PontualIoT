package com.pontualiot.demo.controller;

// ========================================
// IMPORTAÇÕES SPRING WEB + SWAGGER
// ========================================
import io.swagger.v3.oas.annotations.Operation; // Documentação Swagger da operação
import io.swagger.v3.oas.annotations.tags.Tag;   // Tag para agrupar endpoints no Swagger
import org.springframework.http.ResponseEntity;   // Wrapper para resposta HTTP
import org.springframework.web.bind.annotation.GetMapping; // Mapeia requisições GET
import org.springframework.web.bind.annotation.RestController; // Marca como REST controller

import java.time.OffsetDateTime; // Para timestamp com timezone
import java.util.Map; // Para retornar mapa de dados

/**
 * ========================================
 * HEALTH CONTROLLER - MONITORAMENTO
 * ========================================
 * 
 * OBJETIVO: Fornecer endpoint de saúde da aplicação
 * 
 * ENDPOINTS EXPOSTOS:
 * - GET /health -> Status da aplicação (customizado)
 * 
 * NOTA: Spring Boot já tem /actuator/health, mas este é customizado
 * 
 * USADO POR:
 * - Testes E2E
 * - Monitoramento
 * - Load balancers
 * - Health checks
 */
@RestController // Marca como controller REST (retorna JSON)
@Tag(name = "Health", description = "Application health check") // Documentação Swagger
public class HealthController {

    /**
     * ENDPOINT DE SAÚDE CUSTOMIZADO
     * 
     * URL: GET /health (sem /api prefix - verificar se está correto)
     * 
     * IMPORTANTE: Este endpoint pode estar conflitando com /actuator/health
     * ou não estar sendo mapeado corretamente devido ao context-path /api
     * 
     * @return ResponseEntity com status da aplicação
     */
    @GetMapping("/health") // Mapeia GET /health
    @Operation(summary = "Check application health") // Documentação Swagger
    public ResponseEntity<Map<String, Object>> health() {
        System.out.println("[HEALTH] Endpoint /health chamado");
        System.out.println("[HEALTH] Retornando status UP");
        
        // Retorna resposta 200 OK com informações da aplicação
        Map<String, Object> healthData = Map.of(
            "status", "UP",                    // Status da aplicação
            "timestamp", OffsetDateTime.now(), // Timestamp atual
            "service", "pontual-iot-api-core", // Nome do serviço
            "version", "1.0.0",                // Versão da aplicação
            "contextPath", "/api",             // Context path configurado
            "port", "8080"                     // Porta configurada
        );
        
        System.out.println("[HEALTH] Dados retornados: " + healthData);
        return ResponseEntity.ok(healthData);
    }
}