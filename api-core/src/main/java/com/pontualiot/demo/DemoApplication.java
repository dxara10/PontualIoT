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
 * FLUXO DE INICIALIZAÇÃO:
 * 1. JVM executa main()
 * 2. SpringApplication.run() inicia o contexto Spring
 * 3. Auto-configuração detecta dependências no classpath
 * 4. Component scan encontra @Controller, @Service, @Repository
 * 5. Configurações são carregadas (application.yml)
 * 6. Tomcat embedded inicia na porta 8080
 * 7. Context path /api é aplicado
 * 8. Endpoints ficam disponíveis
 * 
 * ENDPOINTS PRINCIPAIS:
 * - GET /api/actuator/health - Health check
 * - GET /api/employees - Lista funcionários
 * - POST /api/employees - Cria funcionário
 * - GET /api/attendances - Lista registros de ponto
 * - POST /api/test-attendance/check-in/{id} - Registra entrada
 * 
 * DEPENDÊNCIAS EXTERNAS:
 * - PostgreSQL: jdbc:postgresql://localhost:5432/pontualiot
 * - MQTT Broker: tcp://localhost:1883
 * - Prometheus: /api/actuator/prometheus
 */
@SpringBootApplication // Combina @Configuration + @EnableAutoConfiguration + @ComponentScan
public class DemoApplication {

    /**
     * MÉTODO MAIN - PONTO DE ENTRADA DA APLICAÇÃO
     * 
     * SEQUÊNCIA DE INICIALIZAÇÃO:
     * 1. Logs de startup são exibidos
     * 2. SpringApplication.run() é chamado
     * 3. Se sucesso: aplicação fica disponível
     * 4. Se erro: exceção é lançada e aplicação para
     * 
     * @param args argumentos da linha de comando (profiles, propriedades, etc)
     */
    public static void main(String[] args) {
        System.out.println("[STARTUP] Iniciando PontualIoT API Core...");
        System.out.println("[STARTUP] Porta esperada: 8080");
        System.out.println("[STARTUP] Context path: /api");
        System.out.println("[STARTUP] Health check: http://localhost:8080/api/actuator/health");
        
        try {
            // Inicia o contexto Spring Boot completo
            // Isso inclui: Tomcat, JPA, Security, MQTT, Metrics
            SpringApplication.run(DemoApplication.class, args);
            System.out.println("[STARTUP] ✅ Aplicação iniciada com sucesso!");
        } catch (Exception e) {
            System.err.println("[STARTUP] ❌ Erro ao iniciar aplicação: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-lança para parar a aplicação
        }
    }
}