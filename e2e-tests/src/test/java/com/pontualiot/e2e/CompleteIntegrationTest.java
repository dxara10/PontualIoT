package com.pontualiot.e2e;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Testes de Integração Completa - Todos os Módulos")
class CompleteIntegrationTest {

    private static final String API_BASE_URL = "http://localhost:8080/api";
    private static Long testEmployeeId;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = API_BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    private void waitForApi() {
        for (int i = 0; i < 20; i++) {
            try {
                given().when().get("/actuator/health").then().statusCode(200);
                return;
            } catch (Exception e) {
                try { Thread.sleep(2000); } catch (InterruptedException ie) {}
            }
        }
    }

    @Test
    @Order(1)
    @DisplayName("1. Infraestrutura - Todos os serviços devem estar rodando")
    void shouldHaveAllServicesRunning() {
        waitForApi();
        
        // API Core
        given()
            .when().get("/actuator/health")
            .then()
                .statusCode(200)
                .body("status", equalTo("UP"));
        
        // PostgreSQL (através da API)
        given()
            .when().get("/employees")
            .then()
                .statusCode(200);
    }

    @Test
    @Order(2)
    @DisplayName("2. CRUD Completo - Employee")
    void shouldPerformCompleteCrudOperations() {
        waitForApi();
        
        // CREATE
        Map<String, Object> employee = new HashMap<>();
        employee.put("name", "Integration Test User");
        employee.put("email", "integration@test.com");
        employee.put("rfidTag", "INT001");
        employee.put("active", true);

        Response createResponse = given()
            .contentType("application/json")
            .body(employee)
            .when().post("/employees")
            .then()
                .statusCode(201)
                .body("name", equalTo("Integration Test User"))
                .extract().response();

        testEmployeeId = createResponse.jsonPath().getLong("id");
        
        // READ
        given()
            .when().get("/employees/" + testEmployeeId)
            .then()
                .statusCode(200)
                .body("email", equalTo("integration@test.com"));
        
        // UPDATE
        employee.put("name", "Updated Integration User");
        given()
            .contentType("application/json")
            .body(employee)
            .when().put("/employees/" + testEmployeeId)
            .then()
                .statusCode(200)
                .body("name", equalTo("Updated Integration User"));
    }

    @Test
    @Order(3)
    @DisplayName("3. Fluxo de Attendance Completo")
    void shouldProcessCompleteAttendanceFlow() {
        waitForApi();
        assertNotNull(testEmployeeId, "Employee deve ter sido criado");
        
        // Check-in
        given()
            .when().post("/test-attendance/check-in/" + testEmployeeId)
            .then()
                .statusCode(200)
                .body("message", containsString("Check-in"));
        
        // Verificar attendance criada
        given()
            .when().get("/attendances")
            .then()
                .statusCode(200)
                .body("size()", greaterThan(0));
        
        // Check-out
        given()
            .when().post("/test-attendance/check-out/" + testEmployeeId)
            .then()
                .statusCode(200)
                .body("message", containsString("Check-out"));
    }

    @Test
    @Order(4)
    @DisplayName("4. Métricas e Monitoramento")
    void shouldHaveMetricsAndMonitoring() {
        waitForApi();
        
        // Prometheus metrics
        String metricsResponse = given()
            .when().get("/actuator/prometheus")
            .then()
                .statusCode(200)
                .extract().asString();

        assertTrue(metricsResponse.contains("jvm_"), "Deve conter métricas JVM");
        assertTrue(metricsResponse.contains("http_"), "Deve conter métricas HTTP");
    }

    @Test
    @Order(5)
    @DisplayName("5. Validação de Dados e Regras de Negócio")
    void shouldValidateBusinessRules() {
        waitForApi();
        
        // Tentar criar employee com dados inválidos
        Map<String, Object> invalidEmployee = new HashMap<>();
        invalidEmployee.put("name", "");
        invalidEmployee.put("email", "invalid-email");
        
        given()
            .contentType("application/json")
            .body(invalidEmployee)
            .when().post("/employees")
            .then()
                .statusCode(400);
        
        // Tentar buscar employee inexistente
        given()
            .when().get("/employees/99999")
            .then()
                .statusCode(404);
    }

    @Test
    @Order(6)
    @DisplayName("6. Performance e Responsividade")
    void shouldHaveGoodPerformance() {
        waitForApi();
        
        long startTime = System.currentTimeMillis();
        
        // Múltiplas requisições
        for (int i = 0; i < 5; i++) {
            given()
                .when().get("/employees")
                .then()
                    .statusCode(200)
                    .time(lessThan(2000L)); // Menos de 2 segundos
        }
        
        long totalTime = System.currentTimeMillis() - startTime;
        assertTrue(totalTime < 10000, "5 requisições devem levar menos de 10 segundos");
    }

    @Test
    @Order(7)
    @DisplayName("7. Integração Externa - Serviços Auxiliares")
    void shouldHaveExternalServicesIntegration() {
        // Verificar se Prometheus está acessível (se rodando)
        try {
            given()
                .baseUri("http://localhost:9090")
                .when().get("/api/v1/query?query=up")
                .then()
                    .statusCode(200);
        } catch (Exception e) {
            System.out.println("⚠️ Prometheus não está rodando - OK para testes básicos");
        }
        
        // Verificar se Grafana está acessível (se rodando)
        try {
            given()
                .baseUri("http://localhost:3000")
                .when().get("/api/health")
                .then()
                    .statusCode(200);
        } catch (Exception e) {
            System.out.println("⚠️ Grafana não está rodando - OK para testes básicos");
        }
    }

    @AfterAll
    static void cleanup() {
        // Limpar dados de teste
        if (testEmployeeId != null) {
            try {
                given()
                    .when().delete("/employees/" + testEmployeeId)
                    .then()
                        .statusCode(anyOf(equalTo(200), equalTo(204), equalTo(404)));
            } catch (Exception e) {
                System.out.println("⚠️ Erro ao limpar dados de teste: " + e.getMessage());
            }
        }
    }
}