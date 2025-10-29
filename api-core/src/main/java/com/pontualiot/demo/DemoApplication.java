package com.pontualiot.demo;

// ========================================
// IMPORTAÇÕES SPRING BOOT
// ========================================
import org.springframework.boot.SpringApplication; // Classe principal para iniciar aplicação Spring Boot
import org.springframework.boot.autoconfigure.SpringBootApplication; // Anotação que habilita auto-configuração

/**
 * ========================================
 * CLASSE PRINCIPAL - PONTUAL IOT API CORE
 * ========================================
 * 
 * RESPONSABILIDADES:
 * - Inicializar aplicação Spring Boot
 * - Configurar context path: /api (definido em application.yml)
 * - Porta padrão: 8080 (definida em application.yml)
 * - Profile ativo: local (padrão)
 * 
 * DEPENDÊNCIAS PRINCIPAIS:
 * - PostgreSQL (porta 5433 configurada, mas rodando na 5432)
 * - MQTT Broker (porta 1883)
 * - Prometheus Metrics (endpoint /api/actuator/prometheus)
 * - Health Check (endpoint /api/actuator/health)
 * 
 * PROBLEMAS CONHECIDOS:
 * - Inconsistência de porta PostgreSQL (config vs realidade)
 * - API não está rodando (precisa investigar logs)
 */
@SpringBootApplication // Habilita: auto-config + component-scan + configuration
public class DemoApplication {

    /**
     * MÉTODO MAIN - PONTO DE ENTRADA
     * 
     * Este método inicia toda a aplicação Spring Boot.
     * Se houver erro aqui, a API não sobe.
     * 
     * @param args argumentos da linha de comando
     */
    public static void main(String[] args) {
        System.out.println("[STARTUP] Iniciando PontualIoT API Core...");
        System.out.println("[STARTUP] Porta esperada: 8080");
        System.out.println("[STARTUP] Context path: /api");
        System.out.println("[STARTUP] Health check: http://localhost:8080/api/actuator/health");
        
        try {
            // Inicia a aplicação Spring Boot
            SpringApplication.run(DemoApplication.class, args);
            System.out.println("[STARTUP] ✅ Aplicação iniciada com sucesso!");
        } catch (Exception e) {
            System.err.println("[STARTUP] ❌ Erro ao iniciar aplicação: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}