package com.pontualiot.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Controller para verificar saúde da aplicação
 * 
 * Fornece endpoint simples para monitoramento e verificação
 * se a API está funcionando corretamente.
 */
@RestController
@Tag(name = "Health", description = "Application health check")
public class HealthController {

    /**
     * Endpoint de saúde da aplicação
     * 
     * Retorna informações básicas sobre o status da API,
     * usado por ferramentas de monitoramento e load balancers.
     * 
     * @return ResponseEntity com status da aplicação
     */
    @GetMapping("/health")
    @Operation(summary = "Check application health")
    public ResponseEntity<Map<String, Object>> health() {
        // Retorna resposta 200 OK com informações da aplicação
        return ResponseEntity.ok(Map.of(
            "status", "UP",                    // Status da aplicação
            "timestamp", OffsetDateTime.now(), // Timestamp atual
            "service", "pontual-iot-api-core", // Nome do serviço
            "version", "1.0.0"                 // Versão da aplicação
        ));
    }
}